-- Existing accounts keep their passwords, permissions and sessions.
ALTER TABLE Users ADD admin BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE Users ADD disabled BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE Users ADD password_change_required BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE Users ADD credential_version INT DEFAULT 0 NOT NULL;

CREATE TABLE AdministrationLock (id INT PRIMARY KEY);
INSERT INTO AdministrationLock (id) VALUES (1);
