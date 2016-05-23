package org.teststompwebsocket.web;

@SuppressWarnings("serial")
public class WSAuthentificationException extends RuntimeException {

    private Error error;

    public WSAuthentificationException(String message) {
        super(message);
    }
}