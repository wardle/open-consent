CREATE TABLE t_patient (encrypted_email text NOT NULL, encrypted_encryption_key text NOT NULL, encrypted_name text NOT NULL, hashed_email VARCHAR(255) NOT NULL unique, hashed_password VARCHAR(255) NOT NULL, id SERIAL PRIMARY KEY);

CREATE TABLE t_project (description text NULL, id SERIAL PRIMARY KEY, title VARCHAR(255) NOT NULL, uuid VARCHAR(64) NOT NULL);

CREATE TABLE t_episode (id serial primary key, date_registration date not null, patient_identifier varchar(255) not null, project_fk int not null, unique(patient_identifier, project_fk));
CREATE TABLE t_registration (encrypted_identifier text NOT NULL, patient_fk INTEGER PRIMARY KEY, unique(encrypted_identifier, patient_fk));

ALTER TABLE t_episode ADD  FOREIGN KEY(project_fk) REFERENCES t_project(id);
ALTER TABLE t_registration ADD FOREIGN KEY (patient_fk) REFERENCES t_patient (id);


