---
name: 🐛 Bug Report
about: Report a bug in the Password Manager backend
title: "[BUG] <short description>"
labels: bug
assignees: ''
---

## 🐛 Bug Description
<!-- A clear and concise description of what the bug is -->

## 🔁 Steps to Reproduce
1. Authenticate and obtain JWT token via Vault Web
2. Call endpoint `...` with payload `...`
3. Observe the error

## ✅ Expected Behavior
<!-- What you expected to happen -->

## ❌ Actual Behavior
<!-- What actually happened. Include HTTP status code and response body if applicable -->
```json
{
  "status": 500,
  "error": "..."
}
```

## 🧱 Affected Area
<!-- Check all that apply -->
- [ ] Password Entry (CRUD)
- [ ] Category Management
- [ ] Vault Setup / Unlock / Rotate / Migrate
- [ ] Password Reveal (master password / vault token)
- [ ] Password Generation
- [ ] Password Breach Check
- [ ] JWT Authentication / Security
- [ ] Encryption / Decryption
- [ ] Other: ___

## ⚠️ Priority
<!-- How urgent is this? -->
- [ ] 🔴 Critical – data loss, security vulnerability, or system crash
- [ ] 🟠 High – core feature broken, no workaround
- [ ] 🟡 Medium – feature partially broken, workaround exists
- [ ] 🟢 Low – minor issue or cosmetic

## 🖥️ Environment
- OS: <!-- e.g. Ubuntu 22.04, macOS 14, Windows 11 -->
- Java Version: <!-- e.g. Java 21 -->
- Spring Boot Version: <!-- e.g. 3.x -->
- Run Mode: <!-- HTTP / HTTPS (dev profile) -->
- Port: <!-- default 8091 -->

## 🔐 Vault State (if relevant)
- Vault initialized: <!-- Yes / No / Unknown -->
- Vault locked: <!-- Yes / No -->
- Using X-Vault-Token: <!-- Yes / No -->
- `vault.requireInitialization` enabled: <!-- Yes / No / Unknown -->

## 📎 Logs / Additional Context
<!-- Paste relevant stack traces, Swagger UI screenshots, or curl commands (redact any secrets!) -->