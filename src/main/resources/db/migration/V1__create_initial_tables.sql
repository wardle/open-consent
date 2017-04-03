CREATE TABLE t_patient (encrypted_email CLOB NOT NULL, encrypted_encryption_key CLOB NOT NULL, encrypted_name CLOB NOT NULL, hashed_email VARCHAR(255) NOT NULL, hashed_password VARCHAR(255) NOT NULL, id SERIAL PRIMARY KEY);

CREATE TABLE t_project (description CLOB NULL, id SERIAL PRIMARY KEY, title VARCHAR(255) NOT NULL, uuid VARCHAR(64) NOT NULL);

CREATE TABLE t_episode (id serial primary key, date_registration date not null, patient_identifier varchar(255) not null, project_fk int not null);
ALTER TABLE t_episode ADD  FOREIGN KEY(project_fk) REFERENCES t_project(id);
