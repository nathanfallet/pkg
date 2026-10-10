-- The schema as `SchemaUtils.create` left it, before Flyway. A database from that era already has
-- these tables: Flyway records it at version 1 (baselineOnMigrate) and never runs this file there.
CREATE TABLE IF NOT EXISTS Sessions (id VARCHAR(255) PRIMARY KEY, `value` text NOT NULL);

CREATE TABLE IF NOT EXISTS Organizations (id BINARY(16) PRIMARY KEY, `name` text NOT NULL);

CREATE TABLE IF NOT EXISTS Users (id BINARY(16) PRIMARY KEY, organization_id BINARY(16) NOT NULL, email VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL);

CREATE INDEX Users_organization_id ON Users (organization_id);

CREATE INDEX Users_email ON Users (email);

CREATE TABLE IF NOT EXISTS Packages (id BINARY(16) PRIMARY KEY, `name` VARCHAR(255) NOT NULL, format VARCHAR(255) NOT NULL, organization_id BINARY(16) NOT NULL, is_public BOOLEAN NOT NULL, created_at DATETIME(6) NOT NULL);

CREATE INDEX Packages_organization_id ON Packages (organization_id);

ALTER TABLE Packages ADD CONSTRAINT Packages_name_format_unique UNIQUE (`name`, format);

CREATE TABLE IF NOT EXISTS PackageVersions (id BINARY(16) PRIMARY KEY, package_id BINARY(16) NOT NULL, version VARCHAR(255) NOT NULL, published_by BINARY(16) NOT NULL, published_at DATETIME(6) NOT NULL, metadata text NULL, yanked BOOLEAN DEFAULT FALSE NOT NULL);

CREATE INDEX PackageVersions_package_id ON PackageVersions (package_id);

ALTER TABLE PackageVersions ADD CONSTRAINT PackageVersions_package_id_version_unique UNIQUE (package_id, version);

CREATE TABLE IF NOT EXISTS PackageVersionFiles (id BINARY(16) PRIMARY KEY, version_id BINARY(16) NOT NULL, `name` VARCHAR(255) NOT NULL, content_type VARCHAR(255) NOT NULL, `size` BIGINT NOT NULL, `path` VARCHAR(255) NOT NULL);

CREATE INDEX PackageVersionFiles_version_id ON PackageVersionFiles (version_id);

ALTER TABLE PackageVersionFiles ADD CONSTRAINT PackageVersionFiles_version_id_name_unique UNIQUE (version_id, `name`);
