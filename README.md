# Password Manager

**Password Manager** is a backend service in the Vault Web ecosystem for securely storing, managing, and retrieving passwords.  
It provides APIs for creating, updating, deleting, and retrieving passwords and categories, similar to a secure digital vault.

This service is designed to integrate seamlessly with Vault Web, **sharing its PostgreSQL database and pgAdmin setup**.

---

## Features

- 🔹 CRUD operations for passwords and categories  
- 🔹 Secure storage of encrypted passwords  
- 🔹 Access via JWT authentication using Vault Web's master key  
- 🔹 REST API for integration with web or mobile apps  

---

## Project Structure

- Backend implemented in **Spring Boot**  
- Uses PostgreSQL from the Vault Web repository for persistent storage  
- See `DIRECTORY.md` for full project structure  

---

## Local Development

Password Manager relies on the **Vault Web Docker environment** for PostgreSQL and pgAdmin.  
**Important:** Make sure Vault Web is running before starting the Password Manager backend.

---

### 1. Clone the Repository

```bash
git clone https://github.com/Vault-Web/password-manager.git
cd password-manager
```

### 2. Configure Environment Variables

For production deployments, set the following environment variables:
```bash
# JWT Secret Key (required in production)
export JWT_SECRET=your_jwt_secret_key_here

# Encryption Secret Key (required in production)
export ENCRYPTION_SECRET=your_encryption_secret_here
```

> ⚠️ **Security Note**: The application.properties file contains default values for local development only. **Never use these default values in production.** Always set JWT_SECRET and ENCRYPTION_SECRET environment variables with secure, randomly generated values in production environments.

> ⚠️ **Make sure PostgreSQL from the Vault Web Docker setup is running** before starting Cloud Page. Run `docker compose up -d` in the Vault Web repository if not already running. The database credentials are inherited from the Vault Web `.env` setup. Do **not** use production secrets during local development.

### 3. Start the backend
The backend runs on port 8091 (can be changed in application.properties). Make sure the Vault Web Docker stack is already running (PostgreSQL & pgAdmin).
```bash
./mvnw spring-boot:run
```

Then visit:
- API Base: http://localhost:8091
- Swagger UI: http://localhost:8091/swagger-ui.html

## Notes
- This service depends on Vault Web for database and authentication.
- JWT tokens must use the same master key as Vault Web.

## Questions?
For any issues, feel free to open an issue in this repository. Integration or usage questions related to Vault Web should reference the main Vault Web documentation.
