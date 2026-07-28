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

To publish, you also need a token:

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
