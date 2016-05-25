package org.teststompwebsocket.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.teststompwebsocket.domain.User;
import org.teststompwebsocket.domain.UserRepository;
import org.teststompwebsocket.domain.WSToken;
import org.teststompwebsocket.domain.WSTokenRepository;
import org.teststompwebsocket.service.CurrentUser;
import org.teststompwebsocket.service.CurrentUserDetailsService;
import org.teststompwebsocket.service.SessionHandler;
import org.teststompwebsocket.util.ApplicationProperties;
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

    private static final Logger LOGGER = LoggerFactory
        .getLogger(AuthenticationController.class);

    private static final String LOGIN_CUSTOMER = "LOGIN_CUSTOMER";

    private static final String PASSWORD = "password";

    private static final String EMAIL = "email";

    /** headers of message with errors **/
    private static final String CUSTOMER_ERROR = "CUSTOMER_ERROR";
    /** headers of successful message **/
    private static final String CUSTOMER_API_TOKEN = "CUSTOMER_API_TOKEN";

    private static final String API_TOKEN = "api_token";

    private static final String API_TOKEN_EXPIRATION_DATE = "api_token_expiration_date";

    private static final String ERROR_CODE = "error_code";

    private static final String ERROR_DESCRIPTION = "error_description";

    private static final String pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @Autowired
    private WSTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CurrentUserDetailsService currentUserDetailsService;

    @Autowired
    private SessionHandler sessionHandler;

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

        if (!message.getType().equalsIgnoreCase(LOGIN_CUSTOMER)) {
            LOGGER.error("Wrong type of message:%s.", message.getType());
            throw new WSAuthenticationException(
                String.format("Wrong type of message:%s.", message.getType()));
        }

        Map<String, String> dataMap = message.getData();

        if (!checkDataSection(dataMap)) {
            LOGGER.error("Wrong data section of message.");
            throw new WSAuthenticationException("Wrong data section of message.");
        }

        String logonName = dataMap.get(EMAIL);
        User user = authentificateUser(logonName, dataMap.get(PASSWORD));

        if (user == null) {
            message.setType(CUSTOMER_ERROR);
            dataMap.clear();
            dataMap.put(ERROR_DESCRIPTION, "Customer not found");
            dataMap.put(ERROR_CODE, "customer.notFound");
        } else {
            message.setType(CUSTOMER_API_TOKEN);
            dataMap.clear();
            String uuid = UUID.randomUUID().toString();
            resetToken(simpSessionId);
            Date date = getExpirationDate();
            SimpleDateFormat format1 = new SimpleDateFormat(pattern);

            Optional<WSToken> activeToken = tokenRepository
                .findOneByPrincipalNameAndActive(simpSessionId, true);

            sessionHandler.addNewToken(uuid, date, simpSessionId);// update active sockets

            WSToken token = new WSToken(uuid, date, user, simpSessionId);
            tokenRepository.save(token); // save new token in storage

            dataMap.put(API_TOKEN, String.valueOf(uuid));
            dataMap.put(API_TOKEN_EXPIRATION_DATE, format1.format(date));

        }
        return message;
    }

    /**
     * Reset token.
     * 
     * @param principalName
     */
    public void resetToken(String simpSessionId) {

        Optional<WSToken> wsToken = tokenRepository
            .findOneByPrincipalNameAndActive(simpSessionId, true);
        if (!wsToken.isPresent()) {
            return;
        }
        WSToken token = wsToken.get();
        token.setActive(false);
        tokenRepository.saveAndFlush(token);
    }

    /**
     * Get expiration date for new token. It's (current time + plusSeconds from configuration file
     * (applications.yml)).
     * 
     * @return expiration date
     */
    private Date getExpirationDate() {

        int expirationSeconds = ApplicationProperties.getPlusseconds();

        LocalDateTime workDateTime = LocalDateTime.now();

        LocalDateTime expirationTime = workDateTime.plusSeconds(expirationSeconds);

        return expirationTime.toDate();
    }

    /**
     * Check data section
     * 
     * @param dataMap
     * @return
     */
    private boolean checkDataSection(Map<String, String> dataMap) {
        if (dataMap == null || dataMap.isEmpty() || dataMap.get(EMAIL) == null
            || dataMap.get(PASSWORD) == null || dataMap.get(EMAIL).length() == 0
            || dataMap.get(PASSWORD).length() == 0) {
            return false;
        }
        return true;
    }

    @MessageExceptionHandler
    @SendTo(value = "/queue/errors")
    public String handleException(WSAuthenticationException exception) {
        return exception.getMessage();
    }

    /**
     * Authentication new User.
     * 
     * @param logonName
     *            userName
     * @param password
     *            password
     * @return status Authentication process
     */
    public User authentificateUser(String logonName, String password) {

        Optional<User> user = userRepository.findOneByEmail(logonName);

        if (!user.isPresent() || !user.get().getPassword().equalsIgnoreCase(password)) {
            LOGGER.info("User is not uthentificated.");
            return null;
        }

        // Get context for anonymous user SecurityContext
        SecurityContext context = SecurityContextHolder.getContext();

        // Authentication process for new User
        CurrentUser currentUser = null;

        try {
            currentUser = (CurrentUser) currentUserDetailsService
                .loadUserByUsername(logonName);
        } catch (UsernameNotFoundException uex) {
            LOGGER.info("User not loaded " + uex.getMessage());
            return null;
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            currentUser, AuthorityUtils.createAuthorityList("ADMIN", "USER"),
            currentUser.getAuthorities());

        context.setAuthentication(authentication); // set new authenticated User in security

        return user.get();
    }
}