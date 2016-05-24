# teststompwebsocket

WebSocket + STOMP + Spring MVC + REST + JSON API.
    
Demo vote system.
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
          killsession: 1  -   if "1" - WebSocket session have to close after expiration time, else "0"  


http://localhost:8080/index.html

Introduction
--------------------------

This is authentification system over STOMP and WebSocket. Application provides the following functions:

1. Authentification user over websocket.

 
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




Sergey Stotskiy

