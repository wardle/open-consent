# OpenConsent

OpenConsent is a java-based microservice that acts as a safe trusted intermediary connecting patients with clinical services and research projects.

## For clinical services and research projects

Essentially, clinical services and research projects can lookup patients using three pieces of identification:

- project key
- patient identifier (for example, NHS number for projects based in the UK)
- date of birth

A service or project passes these to the web-service to understand whether the patient has consented as well as the project-specific pseudonym which can be used to lookup the patient in the future, if required. The patient-identifiable information is held transiently in computer memory until they are combined to create, unique for that project or service, a pseudonym.

## For patients

Patients can register for an account simply and easily without any need to use a trusted identity service such as gov.uk Verify. However, at the time a patient links their account to a clinical service or research project, essentially, their identity becomes validated for that registration. 

For example, when a patient attends the multiple sclerosis clinic and they wish their account to be linked to that service, the service takes the information above and uses OpenConsent to generate a project-specific pseudonymous identifier. This identifier is either typed or scanned by the patient in order to link their account to that clinical service. It is expected that this identifier would be a QR code with validation digits.

Each registration is accompanied by encrypted information that provides human-readable information for that patient so that the services and projects to which they are registered can be displayed. It is expected that the encryption key would itself be encrypted by a password.

A patient can directly change the permissions and consent for access for all or any of the services to which they are registered.  Importantly, should a patient remove their consent for a service or project, their registration and therefore their project-specific pseudonym is removed. Thus, if a clinical service or research project attempts to lookup a patient, no information will be found and that service should record that a patient is no longer registered.

## For hackers

Information about registration and consent to different services is sensitive information. If a patient has a registration to a known HIV service by virtue of a join table in a relational database then the OpenConsent service, if hacked, would leak information about all patients known to that service.

However, instead of using a simple relational join-table, each patient has a list of encrypted registrations.

Permissions and consent are related to that registration information. Even if the service is hacked, it is not possible to find all patients known to a particular service.


## For developers

The service is divided into *core* and *server*. The server is built as a microservice using the Bootique framework and standard JAX-RS API for providing REST services. The object relational mapping is provided by Apache Cayenne, with a PostgreSQL backend.
