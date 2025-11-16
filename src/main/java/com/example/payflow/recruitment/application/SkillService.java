package com.example.payflow.recruitment.application;

import com.example.payflow.recruitment.domain.Skill;
import com.example.payflow.recruitment.domain.SkillCategory;
import com.example.payflow.recruitment.domain.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillService {
    
    private final SkillRepository skillRepository;
    
    @Transactional
    public Skill createSkill(String name, SkillCategory category, String description) {
        skillRepository.findByName(name).ifPresent(s -> {
            throw new IllegalArgumentException("이미 존재하는 기술입니다: " + name);
        });
        
        Skill skill = Skill.create(name, category, description);
        return skillRepository.save(skill);
    }
    
    @Transactional
    public void addSimilarSkill(Long skillId, Long similarSkillId) {
        Skill skill = skillRepository.findById(skillId)
            .orElseThrow(() -> new IllegalArgumentException("기술을 찾을 수 없습니다: " + skillId));
        
        Skill similarSkill = skillRepository.findById(similarSkillId)
            .orElseThrow(() -> new IllegalArgumentException("기술을 찾을 수 없습니다: " + similarSkillId));
        
        skill.addSimilarSkill(similarSkill);
        skillRepository.save(skill);
    }
    
    public Skill getSkill(Long id) {
        return skillRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("기술을 찾을 수 없습니다: " + id));
    }
    
    public Skill getSkillByName(String name) {
        return skillRepository.findByName(name)
            .orElseThrow(() -> new IllegalArgumentException("기술을 찾을 수 없습니다: " + name));
    }
    
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }
    
    public List<Skill> getSkillsByCategory(SkillCategory category) {
        return skillRepository.findByCategory(category);
    }
    
    public List<Skill> searchSkills(String keyword) {
        return skillRepository.searchByKeyword(keyword);
    }
    
    public List<Skill> getSimilarSkills(Long skillId) {
        return skillRepository.findSimilarSkills(skillId);
    }
}
