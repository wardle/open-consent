CREATE TABLE t_patient (encrypted_email text NOT NULL, encrypted_encryption_key text NOT NULL, encrypted_name text NOT NULL, hashed_email VARCHAR(255) NOT NULL unique, hashed_password VARCHAR(255) NOT NULL, id SERIAL PRIMARY KEY, public_key text NOT NULL, encrypted_private_key text NOT NULL);

CREATE TABLE t_authority (id SERIAL PRIMARY KEY, logic VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL UNIQUE, uuid VARCHAR(64) NOT NULL);

CREATE TABLE t_project (description text NULL, id SERIAL PRIMARY KEY, title VARCHAR(255) NOT NULL UNIQUE, uuid VARCHAR(64) NOT NULL, authority_fk INTEGER NOT NULL, public_key text NULL, messaging BOOLEAN NOT NULL);

CREATE TABLE t_episode (id serial primary key, date_registration date not null, patient_pseudonym varchar(255) not null, patient_authority_pseudonym varchar(255) not null, project_fk int not null);

CREATE TABLE t_registration (id serial primary key, encrypted_pseudonym text NOT NULL, patient_fk INTEGER, unique(encrypted_pseudonym, patient_fk));

CREATE TABLE t_endorsement (authority_fk integer NOT NULL, encrypted_authority_pseudonym varchar(255) NOT NULL, id serial PRIMARY KEY, patient_fk integer NOT NULL);

CREATE TABLE t_consent_form (created_date_time TIMESTAMP NOT NULL, final_date_time TIMESTAMP NULL, id SERIAL PRIMARY KEY, information text NULL, project_fk INTEGER NOT NULL, status VARCHAR(64) NOT NULL, title text NOT NULL, version_string VARCHAR(255) NOT NULL);

CREATE TABLE t_consent_item (behaviour VARCHAR(255) NOT NULL, consent_form_fk INTEGER NOT NULL, description text NOT NULL, id SERIAL PRIMARY KEY, name VARCHAR(255) NOT NULL, ordering INTEGER NOT NULL, type VARCHAR(64) not null);

CREATE TABLE t_permission_form (consent_form_fk INTEGER NOT NULL, date_time_created TIMESTAMP NOT NULL, episode_fk INTEGER NOT NULL, id SERIAL primary key);

CREATE TABLE t_permission_item (consent_item_fk INTEGER NOT NULL, id SERIAL NOT NULL, permission_form_fk INTEGER NOT NULL, response VARCHAR(64) NOT NULL);

ALTER TABLE t_episode ADD  FOREIGN KEY(project_fk) REFERENCES t_project(id);
ALTER TABLE t_registration ADD FOREIGN KEY (patient_fk) REFERENCES t_patient (id);
ALTER TABLE t_project ADD FOREIGN KEY (authority_fk) REFERENCES t_authority (id);
ALTER TABLE t_endorsement ADD FOREIGN KEY (authority_fk) REFERENCES t_authority (id);
ALTER TABLE t_endorsement ADD FOREIGN KEY (patient_fk) REFERENCES t_patient (id);
ALTER TABLE t_consent_form ADD FOREIGN KEY (project_fk) REFERENCES t_project (id);
ALTER TABLE t_consent_item ADD FOREIGN KEY (consent_form_fk) REFERENCES t_consent_form (id);
ALTER TABLE t_permission_form ADD FOREIGN KEY (consent_form_fk) REFERENCES t_consent_form (id);
ALTER TABLE t_permission_item ADD FOREIGN KEY (consent_item_fk) REFERENCES t_consent_item (id);
ALTER TABLE t_permission_item ADD FOREIGN KEY (permission_form_fk) REFERENCES t_permission_form (id);