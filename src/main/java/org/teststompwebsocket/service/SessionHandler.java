package org.teststompwebsocket.service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
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
import org.teststompwebsocket.util.WSSessionWrapper;

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

    private final Map<String, WSSessionWrapper> sessionMap = new ConcurrentHashMap<>();

    public Map<String, WSSessionWrapper> getSessions() {
        return sessionMap;
    }

    @Autowired
    private WSTokenRepository tokenRepository;

    public SessionHandler() {

        killSession.set(ApplicationProperties.getKillSession());

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sessionMap.keySet().forEach(k -> {
                    LOGGER.info("Active session: " + k + " user: ");//

                    LocalDateTime workDateTime = LocalDateTime.now();

                    WSSessionWrapper sessionWraper = sessionMap.get(k);

                    LocalDateTime currentTime = sessionWraper.getExpirationTime();

                    if (currentTime == null) {
                        Optional<WSToken> currentToken = tokenRepository
                            .findOneByPrincipalNameAndActive(sessionWraper.getId(), true);
                        if (!currentToken.isPresent()) {
                            return;
                        }
                        WSToken token = currentToken.get();
                        LocalDateTime expirationDateTime = new LocalDateTime(
                            token.getExpirationDate());
                        if (workDateTime.compareTo(expirationDateTime) > 0) {
                            // check expiration for token
                            token.setActive(false);
                            tokenRepository.save(token);
                            if (killSession.get()) { // if needs to kill expirated session
                                try {
                                    sessionWraper.close();
                                    sessionMap.remove(k);
                                } catch (IOException e) {
                                    LOGGER.error(
                                        "Error while closing websocket session: {}", e);
                                }
                            }
                        } else {
                            sessionWraper.setExpirationTime(expirationDateTime);
                        }

                    } else {
                        if (workDateTime
                            .compareTo(sessionWraper.getExpirationTime()) > 0) {
                            // check expiration for token
                            Optional<WSToken> currentToken = tokenRepository
                                .findOneByPrincipalNameAndActive(sessionWraper.getId(),
                                    true);
                            if (!currentToken.isPresent()) {
                                return;
                            }
                            WSToken token = currentToken.get();
                            token.setActive(false);
                            tokenRepository.save(token);

                            if (killSession.get()) { // if needs to kill expirated session
                                try {
                                    sessionWraper.close();
                                    sessionMap.remove(k);
                                } catch (IOException e) {
                                    LOGGER.error(
                                        "Error while closing websocket session: {}", e);
                                }
                            }

                        }
                    }
                });
            }

        }, 10, 5, TimeUnit.SECONDS);

    }

    public void register(WebSocketSession session) {
        sessionMap.put(session.getId(), new WSSessionWrapper(session));
    }

}
