# OpenConsent

OpenConsent is a java-based microservice that acts as a safe trusted intermediary connecting patients with clinical services and research projects.

For more information about this project, please see my recent [blog post](http://wardle.org/information-governance/2017/04/05/pseudonymous-consent-poc.html).

An updated description of the project is now available here: [http://wardle.org/information-governance/2017/04/08/pseudonymous-consent.html](http://wardle.org/information-governance/2017/04/08/pseudonymous-consent.html)


## How to get started

1. Download the source code. 
```
git clone https://github.com/wardle/openconsent.git
```

2. Compile without unit tests
```
mvn -DskipTests package
```

3. Create a local database - I use PostgreSQL.

4. Create a config.yml and change your jdbc url for the database you have created
```
jdbc:
  postgres:
    url: jdbc:postgresql:openconsent
    driverClassName: org.postgresql.Driver
    initialSize: 10

cayenne:
  datasource: postgres
  createSchema: false

flyway:
  locations:
    - db/migration
  dataSources:
    - postgres
```

5. Create the database using flyway migrations. 

```
java -jar target/openconsent-1.0-SNAPSHOT.jar --config=config.yml --migrate
```

6. You can run the unit tests to check it is all working

```
mvn package
```

7. Otherwise, start the REST server:

```
java -jar target/openconsent-1.0-SNAPSHOT.jar --config=config.yml --server
```

There's lots more work to do, including using asymmetric encryption to encrypt the authority-scoped pseudonyms on a per-patient basis.

Mark