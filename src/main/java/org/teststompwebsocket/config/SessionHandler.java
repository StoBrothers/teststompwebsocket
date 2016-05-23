package org.teststompwebsocket.config;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.teststompwebsocket.domain.WSToken;
import org.teststompwebsocket.domain.WSTokenRepository;
import org.teststompwebsocket.util.ApplicationProperties;

/**
 * SessionHandler registering every websocket session and start a thread. This Thread every 5
 * seconds check expiration date for open sessions. If session is expired that updating status token
 * to NOT ACTIVE status in storage.
 * 
 * 
 * @author Sergey Stotskiy
 *
 */

@Service
public class SessionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandler.class);
    /**
     * If needs to kill session set to true.
     */
    private AtomicBoolean killSession = new AtomicBoolean(false);

    private final ScheduledExecutorService scheduler = Executors
        .newSingleThreadScheduledExecutor();

    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Autowired
    private WSTokenRepository tokenRepository;

    public SessionHandler() {

        killSession.set(ApplicationProperties.getKillSession());

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sessionMap.keySet().forEach(k -> {
                    LOGGER.info("Active session: " + k + " user: "
                        + sessionMap.get(k).getPrincipal().getName());

                    LocalDateTime workDateTime = LocalDateTime.now();

                    for (WSToken currentToken : tokenRepository
                        .findOneByPrincipalNameAndActive(
                            sessionMap.get(k).getPrincipal().getName(), true)) {
                        LocalDateTime expirationDateTime = new LocalDateTime(
                            currentToken.getExpirationDate());
                        if (workDateTime.compareTo(expirationDateTime) > 0) {
                            // check expiration for token
                            if (killSession.get()) { // if needs to kill expirated session
                                try {
                                    sessionMap.get(k).close();
                                    sessionMap.remove(k);
                                } catch (IOException e) {
                                    LOGGER.error(
                                        "Error while closing websocket session: {}", e);
                                }
                            }
                            currentToken.setActive(false);
                            tokenRepository.save(currentToken);
                        }
                    }
                });
            }
        }, 10, 5, TimeUnit.SECONDS);

    }

    public void register(WebSocketSession session) {
        sessionMap.put(session.getId(), session);
    }

}
