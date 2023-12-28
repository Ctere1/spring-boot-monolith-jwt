<h1 align="center">
  Spring-boot Access-Refresh Token
  
   
  ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
  ![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
  ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white) <br>
  ![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
  ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
  ![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
  <br>
</h1>

<p align="center">
  <a href="#ℹ%EF%B8%8F-introduction">Introduction</a> •
  <a href="#installation-guide">Installation Guide</a> •
  <a href="#api">API Reference</a> •
  <a href="#license">License</a> •
  <a href="#contributors">Contributors</a> 
</p>

<div align="center">

![GitHub Repo stars](https://img.shields.io/github/stars/Ctere1/spring-boot-monolith-jwt)
![GitHub forks](https://img.shields.io/github/forks/Ctere1/spring-boot-monolith-jwt)
![GitHub watchers](https://img.shields.io/github/watchers/Ctere1/spring-boot-monolith-jwt)

</div>

## ℹ️ Introduction
- This project employs a token-based authentication system, utilizing `refresh tokens` and `access tokens` for secure user interactions. The integration follows best practices to enhance security and user experience.        

  - **Access Tokens**: Short-lived tokens issued upon successful authentication, granting access to specific resources. Their limited lifespan enhances security by minimizing exposure.        

  - **Refresh Tokens**: Long-lived tokens designed to refresh access tokens without requiring user credentials. These are securely stored and provide a seamless and secure experience by extending user sessions.      

- This token-based approach improves security by reducing the exposure window for access tokens, and the use of refresh tokens enhances user convenience by eliminating the need for frequent logins. Together, they create a robust authentication mechanism for the CRUD API project.     

> Access-Refresh tokens workflow  
  ![Screenshot](images/ss1.png)   

## 💾Installation Guide

- To clone and run this application, you'll need [Git](https://git-scm.com), [Java](https://www.java.com/en/download/help/download_options.html) and [PostgreSQL](https://www.postgresql.org/download/) installed on your computer.
From your command line:

    ```bash
    # Clone this repository
    $ git clone https://github.com/Ctere1/spring-boot-monolith-jwt
    # Go into the repository
    $ cd spring-boot-monolith-jwt
    # Install dependencies
    $ mvn install
    # Run the app
    $ mvn spring-boot:run
    ```

- You can change the database connection string in the `application.properties` file. And you can change the JWT secret key.
   

## ⚡API

- You can check the swagger documentation after running the server. The swagger documentation is available at `http://localhost:8080/swagger-ui/index.html#/`.
- You can perform CRUD operations with authentication and authorization with the swagger ui.  
  ![Screenshot](images/ss.png)   

> [!Note]   
> Check the postman collection-environments for details.

### **Auth Endpoints**

| HTTP Verb   | Endpoint                    | Description                         | Parameters      | Body (JSON)                             |
| :---------- | :-----------------------    |:----------------------------------  | :-------------  | :-------------------------------------  | 
| `POST`      | `/api/auth/signup`          |  Creates new user for login         | -               | `username`, `email`, `password`, `role` |
| `POST`      | `/api/auth/signin`          |  Returns the accessToken            | -               | `username`, `password`                  |


### **Auth Endpoint Data Example**

> ![GET](https://img.shields.io/badge/-POST-red)    
> http://localhost:8080/api/auth/signin | JSON Body: {"username": "cemil","password": "123456"}

```json
{
    "id": 1,
    "username": "cemil",
    "email": "test@email.com",
    "roles": [
        "ROLE_MODERATOR",
        "ROLE_USER",
        "ROLE_ADMIN"
    ],
    "refreshToken": "0994a0ae-89f2-4dbe-9084-7b37fcab45a1",
    "tokenType": "Bearer",
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjZW1pbCIsImlhdCI6MTcwMzc5NzU0NiwiZXhwIjoxNzAzODAxMTQ2fQ.Z3DLzP4hTLyfOkFj_u5Iw4ptNF42KEBwRRlv6XV8fbjpsMe8p81NawZOiYlIYCmcCZIAxV91iS5Ekug8_vc7gg"
}
```

## ©License
![GitHub](https://img.shields.io/github/license/Ctere1/spring-boot-monolith-jwt?style=flat-square)


## 📌Contributors

<a href="https://github.com/Ctere1/">
  <img src="https://contrib.rocks/image?repo=Ctere1/Ctere1" />
</a>

