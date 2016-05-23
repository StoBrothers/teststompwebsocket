package org.teststompwebsocket.web;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.teststompwebsocket.domain.User;
import org.teststompwebsocket.domain.UserRepository;
import org.teststompwebsocket.domain.WSToken;
import org.teststompwebsocket.domain.WSTokenRepository;
import org.teststompwebsocket.util.ApplicationProperties;

/**
 * 
 * AuthentificationController.
 * 
 * Check input authentificate messages. Genereate a new token and store it in storage. Resete old
 * token.
 * 
 * 
 * @author Sergey Stotskiy
 *
 */
@Controller
public class AuthentificationController {

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

    @MessageMapping("/{username}/wslogin")
    @SendTo(value = "/topic/{username}/wslogin")
    public Object handle(@Payload AuthentificationMsg message,
        @DestinationVariable("username") String username, Principal principal) {

        if (!message.getType().equalsIgnoreCase(LOGIN_CUSTOMER)) {
            throw new WSAuthentificationException(
                String.format("Wrong type of message:%s.", message.getType()));
        }

        Map<String, String> dataMap = message.getData();

        if (!checkDataSection(dataMap)) {
            throw new WSAuthentificationException("Wrong data section of message.");
        }

        Optional<User> user = userRepository.findOneByEmail(dataMap.get(EMAIL));

        if (!user.isPresent()
            || !user.get().getPassword().equalsIgnoreCase(dataMap.get(PASSWORD))) {
            message.setType(CUSTOMER_ERROR);
            dataMap.clear();
            dataMap.put(ERROR_DESCRIPTION, "Customer not found");
            dataMap.put(ERROR_CODE, "customer.notFound");
        } else {
            message.setType(CUSTOMER_API_TOKEN);
            dataMap.clear();
            String uuid = UUID.randomUUID().toString();
            resetToken(principal.getName());
            Date date = getExpirationDate();
            SimpleDateFormat format1 = new SimpleDateFormat(pattern);
            WSToken token = new WSToken(uuid, date, user.get(), principal.getName());
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
    public void resetToken(String principalName) {
        for (WSToken wsToken : tokenRepository
            .findOneByPrincipalNameAndActive(principalName, true)) {

            wsToken.setActive(false);
            tokenRepository.saveAndFlush(wsToken);
        }
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

        LocalDateTime expirationTime = workDateTime.plusSeconds(expirationSeconds); //

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
    public String handleException(WSAuthentificationException exception) {
        return exception.getMessage();
    }

}