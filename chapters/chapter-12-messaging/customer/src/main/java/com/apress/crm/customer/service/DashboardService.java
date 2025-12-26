package com.apress.crm.customer.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final SimpMessagingTemplate messagingTemplate;

    public DashboardService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyFrontend(String message) {
        messagingTemplate.convertAndSend("/topic/updates", message);
    }
}
