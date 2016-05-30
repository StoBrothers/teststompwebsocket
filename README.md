# teststompwebsocket

WebSocket + STOMP + Spring MVC + REST + JSON API.
    
Demo WebSocket system.
========================

Requirements
------------
Install Java 8.65 version
http://www.oracle.com/ 

Install Apache Maven 
https://maven.apache.org

How run a project?
--------------------------

```
mvn spring-boot:run
```
Project supported some spring profiles. 


You can set application parameters in src/main/resources/application.yml

    expirationtime:
          plusexpirationseconds: 20   - time of expiration session in seconds
          initialDelay:  50    - delay for start service for check expriration token  
          period: 5            - period beetwen start service for check expiration token  



You can to work with application over link:

http://localhost:8080/index.html


Introduction
--------------------------

This is authentication system over STOMP and WebSocket. Application provides the following functions:

1. Authentication user over websocket.
2. Generate and save new token with expiration time of token.
3. Internal service check opened sessions and check expired sessions every period seconds.
4. You can to see storage with token at history page.
5. You can to see opened sessions at sessions page.

 
How work with application?
------------------------------
You can open application in browser: http://localhost:8080
 
Test users
-----------------------------
        Login/password is :  
        "admin" / "admin"
        "app1" / "app1"
        "app2" / "app2"
        "app3" / "app3"
        "app4" / "app4"
        "app5" / "app5"
        "app6" / "app6"


Steps for migration on WildFly server:

1. You have to remove these lines from your configuration (standalone.xml):

    extension module="org.jboss.as.jsf"
    subsystem xmlns="urn:jboss:domain:jsf:1.0"

2. Just exclude dependency from pom.xml:

		<groupId>org.springframework.boot</groupId >
            	<artifactId>spring-boot-starter-web</artifactId>
            	<exclusions>
                	<exclusion>
                    		<groupId>org.springframework.boot</groupId>
                    		<artifactId>spring-boot-starter-tomcat</artifactId>
                	</exclusion>
            	</exclusions>

3. Build a project: mvn -DskipTests clean package 
4. Copy result war file to your wildfly\standalone\deployments folder.
5. Execute standalone.bat


author: Sergey Stotskiy

