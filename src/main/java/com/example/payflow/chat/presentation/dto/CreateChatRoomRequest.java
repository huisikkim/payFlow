package com.example.payflow.chat.presentation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateChatRoomRequest {
    private String storeId;
    private String distributorId;
}
