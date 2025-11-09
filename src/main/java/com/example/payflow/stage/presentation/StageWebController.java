package com.example.payflow.stage.presentation;

import com.example.payflow.stage.application.StageService;
import com.example.payflow.stage.application.StageSettlementService;
import com.example.payflow.stage.domain.Stage;
import com.example.payflow.stage.domain.StageSettlement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/stages")
@RequiredArgsConstructor
public class StageWebController {

    private final StageService stageService;
    private final StageSettlementService settlementService;

    @GetMapping
    public String stageList(Model model) {
        try {
            List<Stage> stages = stageService.getStagesByStatus(null);
            model.addAttribute("stages", stages);
            return "stage-list";
        } catch (Exception e) {
            model.addAttribute("error", "스테이지 목록을 불러올 수 없습니다.");
            return "error";
        }
    }

    @GetMapping("/{stageId}/settlement")
    public String settlementDashboard(@PathVariable Long stageId, Model model) {
        try {
            Stage stage = stageService.getStage(stageId);
            StageSettlement settlement = settlementService.getSettlement(stageId);

            settlement.getParticipantSettlements().size();
            
            model.addAttribute("stage", stage);
            model.addAttribute("settlement", settlement);
            
            return "stage-settlement";
        } catch (Exception e) {
            model.addAttribute("error", "정산 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }
}
