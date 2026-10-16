# The npm registry protocol

[The official registry API](https://github.com/npm/registry/blob/main/docs/REGISTRY-API.md) documents
only reads — `GET /{package}`, `GET /{package}/{version}` and search. It says nothing about
publishing, tarball downloads, authentication, or how a scope is spelled in a url. What follows was
captured from a real `npm publish` and `npm install` (npm 11.6, node 24) and is what this
implementation is built against.

## A package name is spelled two different ways

The client percent-encodes the scope separator when it asks for a document, and does not when it
follows a tarball url:

```
GET /npm/@guimauve%2ffake-pkg                                  → one segment, decodes to @guimauve/fake-pkg
GET /npm/@guimauve/fake-pkg/-/guimauve-fake-pkg-1.0.0.tgz      → four segments
```

A router splits on the raw `/` before decoding, so the first form arrives as a single segment. Both
have to resolve to the same package, which is what `NpmPath` is for.

## Publishing

`PUT /npm/{name}`, `Authorization: Bearer <token>`, with the whole packument plus the archives
inline, base64 encoded:

```json
{
  "_id": "@guimauve/fake-pkg",
  "name": "@guimauve/fake-pkg",
  "dist-tags": {
    "latest": "1.0.0"
  },
  "versions": {
    "1.0.0": {
      "name": "@guimauve/fake-pkg",
      "version": "1.0.0",
      "dist": {
        "integrity": "sha512-…",
        "shasum": "159d59b2…",
        "tarball": "http://localhost:8080/npm/@guimauve/fake-pkg/-/@guimauve/fake-pkg-1.0.0.tgz"
      }
    }
  },
  "_attachments": {
    "@guimauve/fake-pkg-1.0.0.tgz": {
      "content_type": "application/octet-stream",
      "data": "H4sIAAAAAAA…",
      "length": 355
    }
  }
}
```

Two things to note. The `dist.tarball` the client sends names whatever registry it happens to be
configured with, so it is never served back — the registry rewrites it. And the `_attachments` key
is the name npm chose, which is not the name the archive is served under.

A version manifest carries far more than a registry needs to understand, and npm keeps adding to it,
so it is stored verbatim in `PackageVersions.metadata` and handed back untouched apart from that url.

## Dist-tags

The `dist-tags` of a publish name the tag it goes under: `{"latest": "1.0.0"}` for a plain
`npm publish`, `{"beta": "1.1.0-beta.1"}` for `npm publish --tag beta`. That tag, and only that one,
is pointed at the published version; `latest` does not move for a publish under another tag, so
`npm install pkg` keeps installing the stable version while `npm install pkg@beta` gets the
pre-release.

A package published before tags were recorded has none, and keeps its most recently published
version as `latest`. A package that has tags but no `latest` (published only under other tags) is
served without one: npm then installs its highest stable version, and never a pre-release by default.

A tag must name a version of the same publish; anything else is refused before anything is written.

## Resolving

`npm install` reads the packument, picks a version, then follows `dist.tarball`. The archive is
served under the name npm builds: the scope becomes a prefix, `@guimauve/fake-pkg` at `1.0.0`
becoming `guimauve-fake-pkg-1.0.0.tgz`.

## Authentication

Everything is `Authorization: Bearer <token>`, the token coming from the `_authToken` of the
`.npmrc`. `npm login --auth-type=legacy` obtains one through
`PUT /npm/-/user/org.couchdb.user:{username}`, which answers `{ "_id": …, "token": … }`.

## Not implemented

Changing a tag without publishing (`npm dist-tag add` / `rm`, `PUT` and `DELETE` on
`/-/package/{name}/dist-tags/{tag}`). Also unpublish, deprecate, `GET /-/all` and
`GET /-/v1/search`, and the web login flow (`POST /-/v1/login` answers an empty object, which makes
npm fall back to the legacy flow).
