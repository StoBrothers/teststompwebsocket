package org.teststompwebsocket.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.teststompwebsocket.domain.WSToken;
import org.teststompwebsocket.domain.WSTokenRepository;

@RestController
public class HistoryController {

    @Autowired
    private WSTokenRepository wsTokenRegistry;

    @RequestMapping(path = "/history", method = RequestMethod.GET)
    public List<String> listUsers() {
        List<String> result = new ArrayList<>();
        for (WSToken user : this.wsTokenRegistry.findAll()) {
            result.add(user.toString());
        }
        return result;
    }

}
