package com.marketplace.servicos.dto;

import lombok.Data;

@Data
public class PushSubscriptionDTO {
    private String endpoint;
    private String p256dh;
    private String auth;
}
