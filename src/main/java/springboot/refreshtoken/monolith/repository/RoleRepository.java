package springboot.refreshtoken.monolith.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import springboot.refreshtoken.monolith.model.ERole;
import springboot.refreshtoken.monolith.model.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
	Optional<Role> findByName(ERole name);

	boolean existsByName(ERole name);

}
