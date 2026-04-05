---
name: 🚀 Feature Request
about: Suggest a new feature or improvement for the Password Manager
title: "[FEATURE] <short description>"
labels: enhancement
assignees: ''
---

## 🚀 Feature Description
<!-- A clear description of the feature you'd like to see -->

## 💡 Motivation / Problem
<!-- What problem does this solve? Why is it needed? -->

## 🧱 Affected Area
<!-- Which part of the backend would this touch? -->
- [ ] Password Entry API (`/api/passwords`)
- [ ] Category API (`/api/categories`)
- [ ] Vault API (`/api/vault`)
- [ ] Password Generation (`/api/passwords/generate`)
- [ ] Password Breach Check
- [ ] JWT / Security Layer
- [ ] Encryption / Crypto Service
- [ ] Database / Repository Layer
- [ ] Configuration / Properties
- [ ] Other: ___

## ⚠️ Priority
- [ ] 🔴 Critical – blocking major use case
- [ ] 🟠 High – significant improvement to core functionality
- [ ] 🟡 Medium – useful but not urgent
- [ ] 🟢 Low – nice to have

## 📐 Proposed Solution
<!-- How do you think this could be implemented? Any API design ideas? -->
```http
POST /api/...
Authorization: Bearer <jwt>
{
  "field": "value"
}
```

## 🔄 Alternatives Considered
<!-- Any alternative approaches you've thought of? -->

## 📎 Additional Context
<!-- Links, references, mockups, or anything else helpful -->