# Yapily Tech Test
### Custom Marvel API
 
<br>

###### Requirements:

 * Public and Private Marvel API keys (free) - http://developer.marvel.com
 * Google Application Credentials API-KEY for a Google Account Project with the Translation API enabled - https://cloud.google.com/translate/docs/setup (How to get one: 1. Create the project (card required), 2. Enable Translate API, 3. Generate API-KEY, 4. Delete project after this demo)

<br>

Dependencies used:
 * Spring Boot (for API architecture)
 * JDBC H2 database (for the auto-generated SQL DB)
 * JSON simple (for parsing JSON)
 * Google Cloud Translate (for live translation)
 * Springfox (for Spring Boot Swagger spec generation)

<br>

###### Set Up:

1. Launch the marvelapi project folder with an IDE and reload the Maven dependencies within pom.xml (In IntelliJ, make sure marvelapi folder is the root folder, then right-click it in the Project viewer, go down to Maven and click 'Reload project')

2. Edit application.properties within the root marvelapi folder and enter your MARVEL public and private api-keys, and GOOGLE Translate-enabled Project api-key without quotations eg. KEY=ADVDV-123
 _----- (Due to time limitations, Spring Cloud Config/vault/Java KeyStore weren't used)_

3. Run the MarvelapiApplication.java in src/main/java/com.yapily.marvelapi/

4. The H2 SQL DB should already be seeded with all the entries but, if needed, you can update it through GET http://localhost:8080/characters/seed

5. Get all character IDs with GET http://localhost:8080/characters

6. Get specific characters using their ID with GET http://localhost:8080/characters/{characterId}  (Do not include {})
_----- (Can also add ?language param with ISO language code to translate the description, eg. ?language=ja)_

<br>
   

###### Swagger Spec
View the Swagger Spec in Swagger UI from this endpoint: http://localhost:8080/swagger-ui.html 

View in Postman by importing the swagger-spec.json as raw text in the marvelapi folder OR view from the endpoint: GET http://localhost:8080/v2/api-docs 

<br>

###### Optional - H2 Interactive SQL Console
View the SQL DB and access SQL queries at http://localhost:8080/h2-console/
_Don't forget to set 'Max rows' at the top to 10,000 to see all of the entries_
1. On the login page change JDBC URL to jdbc:h2:file:./data/characters
2. User Name: sa
3. Password: password
4. Then Connect
5. Click CHARACTER on the left and then click Run above the query box
6. Or type custom queries into the query box, and then click Run
