package org.teststompwebsocket.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.teststompwebsocket.service.AuthenticationService;
import org.teststompwebsocket.service.AuthenticationServiceImpl;
import org.teststompwebsocket.util.AuthenticationMsg;
import org.teststompwebsocket.util.WSAuthenticationException;

/**
 * 
 * AuthenticationController.
 * 
 * Check input authentificate messages and authentificate new user in system. Genereate a new token
 * and store it in storage. Reset old tokens.
 * 
 * @author Sergey Stotskiy
 *
 */
@Controller
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Handle messages from user with login {username} and authentificate him. Build output message.
     * 
     * @param message
     *            - input message.
     * @param username
     *            - new user
     * @param principal
     *            principal user
     * @return
     */
    @MessageMapping("/{username}/wslogin")
    @SendTo(value = "/topic/{username}/wslogin")
    public Object handle(@Payload AuthenticationMsg message,
        @DestinationVariable("username") String username,
        @Header("simpSessionId") String simpSessionId) {
        return authenticationService.handleMessage(message, username, simpSessionId);
    }

    @MessageExceptionHandler
    @SendTo(value = "/queue/errors")
    public String handleException(WSAuthenticationException exception) {
        return exception.getMessage();
    }

}