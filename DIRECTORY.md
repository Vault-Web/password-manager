# Project Structure

## backend

- 📁 **src**
  - 📁 **main**
    - 📁 **java**
      - 📁 **com**
        - 📁 **vaultweb**
          - 📁 **passwordmanager**
            - 📁 **backend**
              - 📄 [BackendApplication.java](backend/src/main/java/com/vaultweb/passwordmanager/backend/BackendApplication.java)
              - 📁 **config**
                - 📄 [OpenApiConfig.java](backend/src/main/java/com/vaultweb/passwordmanager/backend/config/OpenApiConfig.java)
              - 📁 **security**
                - 📄 [JwtAuthFilter.java](backend/src/main/java/com/vaultweb/passwordmanager/backend/security/JwtAuthFilter.java)
                - 📄 [JwtUtil.java](backend/src/main/java/com/vaultweb/passwordmanager/backend/security/JwtUtil.java)
                - 📄 [SecurityConfig.java](backend/src/main/java/com/vaultweb/passwordmanager/backend/security/SecurityConfig.java)
  - 📁 **test**
    - 📁 **java**
      - 📁 **com**
        - 📁 **vaultweb**
          - 📁 **passwordmanager**
            - 📁 **backend**
              - 📄 [BackendApplicationTests.java](backend/src/test/java/com/vaultweb/passwordmanager/backend/BackendApplicationTests.java)
