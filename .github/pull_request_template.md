## 📋 Summary
<!-- What does this PR do and why? Be specific. -->

Closes #<!-- issue number -->

## 🔧 Type of Change
- [ ] 🐛 Bug fix
- [ ] 🚀 New feature
- [ ] ♻️ Refactor / code cleanup
- [ ] 📝 Documentation update
- [ ] ✅ Test improvement
- [ ] 🔒 Security fix
- [ ] ⚙️ Configuration / build change

## 🧱 Affected Layer(s)
- [ ] Controller (`/controllers`)
- [ ] Service (`/services`)
- [ ] Repository (`/repositories`)
- [ ] Model / DTO (`/model`, `/model/dtos`)
- [ ] Security (`/security` — JWT, encryption)
- [ ] Exception Handling (`/exceptions`)
- [ ] Config (`/config`)
- [ ] Tests (`/test`)

## 🔐 Security Checklist
<!-- Required for any change touching auth, encryption, or vault logic -->
- [ ] No secrets, tokens, or passwords are logged or exposed
- [ ] JWT validation logic is unchanged or reviewed
- [ ] Vault token (`X-Vault-Token`) handling is not weakened
- [ ] Encryption / decryption behaviour is unchanged or explicitly reviewed
- [ ] No `ENCRYPTION_SECRET` or master password is hardcoded

## ✅ General Checklist
- [ ] I have read the [CONTRIBUTING](../../CONTRIBUTING.md) guide
- [ ] Code follows existing style (Spring Boot conventions, Lombok, etc.)
- [ ] I have tested locally (`./mvnw spring-boot:run` on port 8091)
- [ ] Swagger UI (`/swagger-ui.html`) reflects changes correctly (if API changed)
- [ ] Existing tests pass (`./mvnw test`)
- [ ] New tests added for new behaviour (if applicable)
- [ ] No new compiler warnings introduced

## 🧪 How to Test
<!-- Step-by-step for the reviewer -->
1. Set `ENCRYPTION_SECRET` env variable
2. Start Vault Web Docker (`docker compose up -d`)
3. Run: `cd backend && ./mvnw spring-boot:run`
4. Open Swagger at `http://localhost:8091/swagger-ui.html`
5. ...

## 📎 Additional Notes
<!-- Anything the reviewer should be aware of — breaking changes, DB migrations, config changes, etc. -->