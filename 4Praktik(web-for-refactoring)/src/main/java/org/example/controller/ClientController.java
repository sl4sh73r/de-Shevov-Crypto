package org.example.controller;

import org.example.service.JwsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private JwsService jwsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private boolean clientRunning = false;

    @GetMapping("/start-client")
    @ResponseBody
    public String startClient() {
        if (!clientRunning) {
            clientRunning = true;
            messagingTemplate.convertAndSend("/topic/client-log", "Client started");
        }
        return "Client started";
    }

    @GetMapping("/stop-client")
    @ResponseBody
    public String stopClient() {
        if (clientRunning) {
            clientRunning = false;
            messagingTemplate.convertAndSend("/topic/client-log", "Client stopped");
        }
        return "Client stopped";
    }

    @GetMapping("/send-message")
    @ResponseBody
    public String sendMessage() {
        if (!clientRunning) {
            return "Client is not running";
        }
        try {
            // Запрос сообщения от сервера
            String message = requestMessage();
            // Создание JWS
            String jws = jwsService.createJWS(message);
            // Отправка JWS на сервер для проверки
            String verifyResult = verifyMessage(jws);
            String logMessage = "Received from server: " + message + "\nSent to server: " + jws + "\nServer response: " + verifyResult;
            messagingTemplate.convertAndSend("/topic/client-log", logMessage);
            return logMessage;
        } catch (Exception e) {
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/client-log", "ERROR: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    private String requestMessage() {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = "http://localhost:8080/server/request";
        return restTemplate.getForObject(requestUrl, String.class);
    }

    private String verifyMessage(String jws) {
        RestTemplate restTemplate = new RestTemplate();
        String verifyUrl = "http://localhost:8080/server/verify";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<>(jws, headers);
        return restTemplate.postForObject(verifyUrl, entity, String.class);
    }
}