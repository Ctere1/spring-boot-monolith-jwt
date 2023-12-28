package springboot.refreshtoken.monolith.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.validation.Valid;
import springboot.refreshtoken.monolith.exception.TokenRefreshException;
import springboot.refreshtoken.monolith.model.ERole;
import springboot.refreshtoken.monolith.model.RefreshToken;
import springboot.refreshtoken.monolith.model.Role;
import springboot.refreshtoken.monolith.model.User;
import springboot.refreshtoken.monolith.payload.request.LoginRequest;
import springboot.refreshtoken.monolith.payload.request.RefreshTokenRequest;
import springboot.refreshtoken.monolith.payload.request.SignupRequest;
import springboot.refreshtoken.monolith.payload.response.JwtResponse;
import springboot.refreshtoken.monolith.payload.response.MessageResponse;
import springboot.refreshtoken.monolith.payload.response.RefreshTokenResponse;
import springboot.refreshtoken.monolith.repository.RoleRepository;
import springboot.refreshtoken.monolith.repository.UserRepository;
import springboot.refreshtoken.monolith.security.jwt.JwtUtils;
import springboot.refreshtoken.monolith.security.services.RefreshTokenService;
import springboot.refreshtoken.monolith.security.services.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "Authentication Endpoints")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	RefreshTokenService refreshTokenService;

	@Autowired
	JwtUtils jwtUtils;

	@PostPersist
	@PostLoad
	@PostConstruct
	private void initializeRoles() {
		// Check if roles exist, and insert them if not
		if (!roleRepository.existsByName(ERole.ROLE_USER)) {
			roleRepository.save(new Role(ERole.ROLE_USER));
		}

		if (!roleRepository.existsByName(ERole.ROLE_MODERATOR)) {
			roleRepository.save(new Role(ERole.ROLE_MODERATOR));
		}

		if (!roleRepository.existsByName(ERole.ROLE_ADMIN)) {
			roleRepository.save(new Role(ERole.ROLE_ADMIN));
		}
	}

	@PostMapping("/signin")
	@Operation(summary = "Authenticate user", description = "Authenticate user with username and password")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User authenticated successfully!"),
			@ApiResponse(responseCode = "400", description = "Error: Username is not found!"),
			@ApiResponse(responseCode = "400", description = "Error: Password is not correct!") })
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

		return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(),
				userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@PostMapping("/refreshtoken")
	@Operation(summary = "Get new access token", description = "Generate new access token with refresh token")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "New access token registered successfully!"),
			@ApiResponse(responseCode = "403", description = "Refresh token is not in database!"), })
	public ResponseEntity<?> refreshtoken(@Valid @RequestBody RefreshTokenRequest request) {
		String requestRefreshToken = request.getRefreshToken();

		// 1- Check refresh token's expire time
		// 2- If we are good then generate access token
		// 2.a- If we are not, redirect to login for new refresh token create
		return refreshTokenService.findByToken(requestRefreshToken).map(refreshTokenService::verifyExpiration)
				.map(RefreshToken::getUser).map(user -> {
					String accessToken = jwtUtils.generateTokenFromUsername(user.getUsername());
					return ResponseEntity.ok(new RefreshTokenResponse(accessToken, requestRefreshToken));
				})
				.orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));

	}

	@PostMapping("/signup")
	@Operation(summary = "Create authenticated users", description = "Create Authenticated user with username, email and password")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User registered successfully!"),
			@ApiResponse(responseCode = "400", description = "Error: Username is already taken!"),
			@ApiResponse(responseCode = "400", description = "Error: Email is already in use!") })
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
				encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@PostMapping("/signout")
	public ResponseEntity<?> logoutUser() {
		UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal();
		Long userId = userDetails.getId();
		refreshTokenService.deleteByUserId(userId);
		return ResponseEntity.ok(new MessageResponse("Log out successful!"));
	}
}
