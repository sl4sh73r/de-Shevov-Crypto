package org.example.controller;

import com.nimbusds.jose.*;
import org.example.service.JwsService;
import org.example.service.KeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/server")
public class ServerController {

    @Autowired
    private KeyService keyService;

    @Autowired
    private JwsService jwsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private boolean serverRunning = false;

    @GetMapping("/start-server")
    @ResponseBody
    public String startServer() {
        if (!serverRunning) {
            serverRunning = true;
            messagingTemplate.convertAndSend("/topic/server-log", "Server started");
        }
        return "Server started";
    }

    @GetMapping("/stop-server")
    @ResponseBody
    public String stopServer() {
        if (serverRunning) {
            serverRunning = false;
            messagingTemplate.convertAndSend("/topic/server-log", "Server stopped");
        }
        return "Server stopped";
    }

    @GetMapping("/request")
    @ResponseBody
    public String requestMessage() {
        if (serverRunning) {
            String message = "Message" + System.currentTimeMillis();
            messagingTemplate.convertAndSend("/topic/server-log", "Received from server: " + message);
            return message;
        } else {
            return "Server is not running";
        }
    }

    @PostMapping("/verify")
    @ResponseBody
    public String verifyMessage(@RequestBody String jwsString) {
        if (!serverRunning) {
            return "Server is not running";
        }
        try {
            if (jwsService.verifyJWS(jwsString)) {
                String message = JWSObject.parse(jwsString).getPayload().toString();
                messagingTemplate.convertAndSend("/topic/server-log", "VALID: " + message);
                return "VALID: " + message;
            } else {
                messagingTemplate.convertAndSend("/topic/server-log", "INVALID");
                return "INVALID";
            }
        } catch (JOSEException | ParseException e) {
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/server-log", "ERROR: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/keypair")
    @ResponseBody
    public Map<String, Object> getKeyPair() {
        return keyService.getKeyPair();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(Exception e) {
        e.printStackTrace();
        messagingTemplate.convertAndSend("/topic/server-log", "ERROR: " + e.getMessage());
        return "ERROR: " + e.getMessage();
    }
}