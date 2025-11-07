package com.example.payflow.stage.presentation;

import com.example.payflow.stage.application.StagePaymentService;
import com.example.payflow.stage.application.StagePayoutService;
import com.example.payflow.stage.application.StageService;
import com.example.payflow.stage.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StageController.class)
@AutoConfigureMockMvc
class StageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StageService stageService;

    @MockitoBean
    private StagePaymentService paymentService;

    @MockitoBean
    private StagePayoutService payoutService;

    @Test
    @DisplayName("스테이지 생성 API")
    @WithMockUser
    void createStage() throws Exception {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        
        when(stageService.createStage(anyString(), anyInt(), any(BigDecimal.class), 
                any(BigDecimal.class), anyInt())).thenReturn(stage);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "테스트 계");
        request.put("totalParticipants", 5);
        request.put("monthlyPayment", 100000);
        request.put("interestRate", 0.05);
        request.put("paymentDay", 15);

        // when & then
        mockMvc.perform(post("/api/stages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트 계"))
                .andExpect(jsonPath("$.totalParticipants").value(5))
                .andExpect(jsonPath("$.status").value("RECRUITING"));
    }

    @Test
    @DisplayName("스테이지 참여 API")
    @WithMockUser(username = "user1")
    void joinStage() throws Exception {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        StageParticipant participant = new StageParticipant(stage, "user1", 3);
        
        when(stageService.joinStage(eq(1L), eq("user1"), eq(3))).thenReturn(participant);

        Map<String, Object> request = new HashMap<>();
        request.put("turnNumber", 3);

        // when & then
        mockMvc.perform(post("/api/stages/1/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.turnNumber").value(3));
    }

    @Test
    @DisplayName("스테이지 조회 API")
    @WithMockUser
    void getStage() throws Exception {
        // given
        Stage stage = new Stage(
                "테스트 계",
                5,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(0.05),
                15
        );
        
        when(stageService.getStage(1L)).thenReturn(stage);

        // when & then
        mockMvc.perform(get("/api/stages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 계"))
                .andExpect(jsonPath("$.totalParticipants").value(5));
    }

    @Test
    @DisplayName("모집 중인 스테이지 목록 조회 API")
    @WithMockUser
    void getRecruitingStages() throws Exception {
        // given
        Stage stage1 = new Stage("계1", 5, BigDecimal.valueOf(100000), BigDecimal.valueOf(0.05), 15);
        Stage stage2 = new Stage("계2", 3, BigDecimal.valueOf(50000), BigDecimal.valueOf(0.03), 10);
        
        when(stageService.getStagesByStatus(StageStatus.RECRUITING)).thenReturn(List.of(stage1, stage2));

        // when & then
        mockMvc.perform(get("/api/stages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("계1"))
                .andExpect(jsonPath("$[1].name").value("계2"));
    }
}
