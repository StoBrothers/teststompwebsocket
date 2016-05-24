package org.teststompwebsocket.util;

/**
 * 
 * @author Sergey Stotskiy
 *
 */
@SuppressWarnings("serial")
public class WSAuthenticationException extends RuntimeException {

    public WSAuthenticationException(String message) {
        super(message);
    }
}