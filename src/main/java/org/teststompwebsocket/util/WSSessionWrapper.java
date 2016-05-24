package org.teststompwebsocket.util;

import java.io.IOException;

import org.joda.time.LocalDateTime;
import org.springframework.web.socket.WebSocketSession;

/**
 * Wrapper for WebSocket Session.
 * 
 * @author Sergey Stotskiy
 *
 */
public class WSSessionWrapper {

    private WebSocketSession session;

    private LocalDateTime expirationTime;

    public WSSessionWrapper(WebSocketSession session) {
        this.session = session;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public WebSocketSession getSession() {
        return session;
    }

    /**
     * Return a unique session identifier.
     */
    public String getId() {
        return session.getId();
    }

    /**
     * Close the WebSocket connection with status 1000, i.e. equivalent to:
     * 
     * <pre class="code">
     * session.close(CloseStatus.NORMAL);
     */
    public void close() throws IOException {
        session.close();
    }

    @Override
    public String toString() {
        return "WSSessionWrapper [session=" + session.getId() + " -  " + session
            + ", expirationTime=" + expirationTime + "]";
    }

}
