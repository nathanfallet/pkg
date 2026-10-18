# pkg

[![License](https://img.shields.io/github/license/nathanfallet/pkg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/nathanfallet/pkg)]()
[![Pull Requests](https://img.shields.io/github/issues-pr/nathanfallet/pkg)]()
[![Code Size](https://img.shields.io/github/languages/code-size/nathanfallet/pkg)]()
[![codecov](https://codecov.io/gh/nathanfallet/pkg/graph/badge.svg?token=XZ7HrShgH3)](https://codecov.io/gh/nathanfallet/pkg)

An open source maven/npm/pypi package manager.

> Warning: This is a work in progress and not ready for production use.

## Motivation

We wanted to publish our packages to import them between our projects easily. But we also wanted them to be private and
not available to the public. Since GitHub does not support python packages, other solutions were very costly, and we
love to make open source software, we decided to create our own package manager!

## Use in your code

### Gradle

Using the Gradle plugin:

```kotlin
plugins {
    id("digital.guimauve.pkg") version "0.1.3"
}

repositories {
    pkg(project) // Default https://pkg.guimauve.digital/maven2 repository
    pkg(project, url = "...") // Custom repository URL
}
```

Note: If you run this repository locally, you need to add `isAllowInsecureProtocol = true` to allow http.

### npm

In your `.npmrc`:

```ini
@organization-name:registry=https://pkg.guimauve.digital/npm/
```

To install private packages or publish, you also need credentials. Either the ones you use for
maven, as `base64(username:password)`, which never expire:

```ini
//pkg.guimauve.digital/npm/:_auth=...
```

The value is only base64, not encrypted: keep that `.npmrc` private.

or a token:

```bash
npm login --auth-type=legacy --registry https://pkg.guimauve.digital/npm/
```

which writes it to your `.npmrc`:

```ini
//pkg.guimauve.digital/npm/:_authToken=...
```

Then `npm publish` and `npm install` work as usual. See [docs/npm.md](docs/npm.md) for what the
registry implements, and what it does not.

### pypi

In your `pip.conf` (to download packages):

```ini
[global]
extra-index-url = https://pkg.guimauve.digital/pypi/simple
```

In your `~/.pypirc` (to publish packages):

```ini
[guimauve-digital]
repository = https://pkg.guimauve.digital/pypi/
```

## Deploy your instance

The easiest way to deploy an instance is using Helm on Kubernetes.

Create a `values.yaml` file with the following content (replace with the desired values):

```yaml
replicaCount: 2
ingress:
  hosts:
    - host: pkg.guimauve.digital
      paths:
        - path: /
          pathType: ImplementationSpecific
  tls:
    - secretName: guimauvedigital-tls
      hosts:
        - pkg.guimauve.digital
jwt:
  secret: 'secret'
s3:
  id: ''
  key: ''
  region: 'eu-west-3'
  name: 'guimauve-pkg'
```

And then, install it:

```bash
helm install pkg path-to-repo/helm/pkg -f values.yaml
```

And you can access it with the chosen domain!

### Accounts and first login

There is no public registration or email delivery. After migrating an empty user database, PKG
creates the `Default` organization and the `admin` account. Its random temporary password is
printed **once**, in the bootstrap log, after the creation transaction commits. Keep this log
private. Concurrent starts create only one account; later starts neither print nor reset its password.

Sign in at `/auth/login` with `admin` and that password. PKG requires a different password of at
least 14 characters (at most 72 UTF-8 bytes, the BCrypt limit) before dashboard or registry access.

Administrators use **Manage users** (`/admin/users`) to create accounts in an organization, grant
or remove administrator access, disable/re-enable users and reset passwords. Administrators manage
users across the instance; package access still follows each account's organization. The last active
administrator cannot be disabled or demoted.

Creation and password reset show a random temporary password only in that response. Copy it and
share it with the person; it cannot be retrieved later. The person must change it before using PKG.
Password changes and resets revoke existing sessions and registry/API tokens. Disabling a user
revokes access immediately; enabling them again does not restore old sessions or tokens.

### Upgrading an existing instance

Existing users retain their identifiers (including email addresses), passwords, organizations and
access. They remain ordinary users, without a forced password change. Existing sessions and tokens
continue to work. No bootstrap account is created when **any user already exists**.

After the migration, promote your chosen existing account manually in your own database. On MySQL,
first confirm the identifier belongs to exactly the account you intend to promote, then run:

```sql
SELECT HEX(id), email, admin FROM Users WHERE email = 'your-existing-login';
-- Substitute the exact HEX(id) returned above:
UPDATE Users SET admin = TRUE WHERE id = UNHEX('your-user-id-as-32-hex-digits');
```

The dashboard reads the role on each request; reload it to see **Manage users**. This does not
change that account's password or invalidate its current sessions. The historical `email` API/DB
field now also stores usernames for new accounts; no mailbox is required.
