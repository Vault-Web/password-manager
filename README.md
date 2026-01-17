# Password Manager

**Password Manager** is a backend service in the Vault Web ecosystem for securely storing, managing, and retrieving passwords.  
It provides APIs for creating, updating, deleting, and retrieving passwords and categories, similar to a secure digital vault.

This service is designed to integrate seamlessly with Vault Web, **sharing its PostgreSQL database and pgAdmin setup**.

---

## Features

- 🔹 CRUD operations for passwords and categories  
- 🔹 Secure storage of encrypted passwords  
- 🔹 Tenant-aware storage keyed to the authenticated Vault Web user  
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

### 2. Configure Encryption Secret Key

The backend service requires an Encryption Secret Key (as the ENCRYPTION_SECRET environment variable) to securely encrypt and decrypt passwords.

### A. Generate the Key
```bash
openssl rand -base64 32
# Example output: Xl+KB4QGMbXxibMipcajAP3ET8OITa7JLF3v+lSeMts=
```

### B. Set the Key as an Environment Variable
You must set this key in your current shell session. Important: After closing the terminal or restarting your computer, the key will be gone and must be set again.

Linux / macOS:
```bash
export ENCRYPTION_SECRET="<Your-generated-Base64-key>"
```

Windows (CMD):
```bash
set ENCRYPTION_SECRET="<Your-generated-Base64-key>"
```
> ⚠️ Replace <Your-generated-Base64-key> with the value you generated in step A.

> ⚠️ **Make sure PostgreSQL from the Vault Web Docker setup is running** before starting Cloud Page. Run `docker compose up -d` in the Vault Web repository if not already running. The database credentials are inherited from the Vault Web `.env` setup. Do **not** use production secrets during local development.

### 3. Start the backend
The backend runs on port 8091 (can be changed in application.properties). Make sure the Vault Web Docker stack is already running (PostgreSQL & pgAdmin).
```bash
./mvnw spring-boot:run
```

### 4. PBKDF2 Iterations (Vault Master Password)
The vault uses PBKDF2-HMAC-SHA256 to derive a key-encryption-key (KEK) from the master password.
The default iteration count is **210000** (chosen as a reasonable baseline around 2024) and can be
adjusted without code changes via:

- `vault.crypto.pbkdf2.iterations` in `backend/src/main/resources/application.properties`

Increasing this value improves resistance against offline guessing but increases latency for
vault operations like setup/verify/unlock/rotate.

### 5. Vault Initialization Policy (Legacy Compatibility)
By default, the service supports a legacy compatibility mode:

- If a user has **not** initialized a vault yet, passwords can still be stored and revealed.
- In this mode, passwords are protected only by the server-side `AttributeEncryptor` (database at-rest encryption),
  and are **not** additionally protected by the per-user vault master password.

To avoid mixed protection levels (and the "security downgrade" path), you can enforce that every
user must initialize a vault before storing/revealing passwords by setting:

- `vault.requireInitialization=true` in `backend/src/main/resources/application.properties`

### IntelliJ / IDE Note:
If you start the application directly via your IDE (e.g., IntelliJ IDEA), you must add the ENCRYPTION_SECRET key in the Run/Debug Configurations under the Environment Variables section, as the IDE does not automatically use shell variables.

Then visit:
- API Base: http://localhost:8091
- Swagger UI: http://localhost:8091/swagger-ui.html

## Notes
- This service depends on Vault Web for database and authentication.
- JWT tokens must use the same master key as Vault Web.

## Questions?
For any issues, feel free to open an issue in this repository. Integration or usage questions related to Vault Web should reference the main Vault Web documentation.
