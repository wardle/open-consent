CREATE TABLE t_patient (encrypted_email text NOT NULL, encrypted_encryption_key text NOT NULL, encrypted_name text NOT NULL, hashed_email VARCHAR(255) NOT NULL unique, hashed_password VARCHAR(255) NOT NULL, id SERIAL PRIMARY KEY, public_key text NOT NULL, encrypted_private_key text NOT NULL);

CREATE TABLE t_authority (id SERIAL PRIMARY KEY, logic VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL UNIQUE, uuid VARCHAR(64) NOT NULL);

CREATE TABLE t_project (description text NULL, id SERIAL PRIMARY KEY, title VARCHAR(255) NOT NULL UNIQUE, uuid VARCHAR(64) NOT NULL, authority_fk INTEGER NOT NULL, public_key text NULL, messaging BOOLEAN NOT NULL);

CREATE TABLE t_episode (id serial primary key, date_registration date not null, patient_pseudonym varchar(255) not null, patient_authority_pseudonym varchar(255) not null, project_fk int not null);

CREATE TABLE t_registration (id serial primary key, encrypted_pseudonym text NOT NULL, patient_fk INTEGER, unique(encrypted_pseudonym, patient_fk));

CREATE TABLE t_endorsement (authority_fk integer NOT NULL, encrypted_authority_pseudonym varchar(255) NOT NULL, id serial PRIMARY KEY, patient_fk integer NOT NULL);

ALTER TABLE t_episode ADD  FOREIGN KEY(project_fk) REFERENCES t_project(id);
ALTER TABLE t_registration ADD FOREIGN KEY (patient_fk) REFERENCES t_patient (id);
ALTER TABLE t_project ADD FOREIGN KEY (authority_fk) REFERENCES t_authority (id);
ALTER TABLE t_endorsement ADD FOREIGN KEY (authority_fk) REFERENCES t_authority (id);
ALTER TABLE t_endorsement ADD FOREIGN KEY (patient_fk) REFERENCES t_patient (id);

