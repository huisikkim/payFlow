package com.example.payflow.escrow.application.dto;

import com.example.payflow.escrow.domain.Participant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantDto {
    private String userId;
    private String name;
    private String email;
    private String phone;
    
    public Participant toEntity() {
        return new Participant(userId, name, email, phone);
    }
    
    public static ParticipantDto from(Participant participant) {
        return new ParticipantDto(
            participant.getUserId(),
            participant.getName(),
            participant.getEmail(),
            participant.getPhone()
        );
    }
}
