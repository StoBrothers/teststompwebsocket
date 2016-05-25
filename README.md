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
          initialDelay:  50    - delay for start service for check expriration token  
          period: 5            - period beetwen start service for check expiration token  



http://localhost:8080/index.html

Introduction
--------------------------

This is authentication system over STOMP and WebSocket. Application provides the following functions:

1. Authentication user over websocket.
2. Generate and save new token and expiration time of token.
3. Show session expiration time and allow to kill session and close socket.
4. Internal job check opened sessions  and close expired sessions every 5 seconds.
5. You can to see storage with token at history page.
6. You can to see opened sessions at sessions page.

 
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



author: Sergey Stotskiy

