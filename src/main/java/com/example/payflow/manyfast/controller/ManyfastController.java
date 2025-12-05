package com.example.payflow.manyfast.controller;

import com.example.payflow.manyfast.dto.NodeRequest;
import com.example.payflow.manyfast.dto.NodeResponse;
import com.example.payflow.manyfast.entity.Project;
import com.example.payflow.manyfast.entity.ProjectNode;
import com.example.payflow.manyfast.service.ManyfastService;
import com.example.payflow.manyfast.service.PrdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manyfast")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ManyfastController {
    
    private final ManyfastService manyfastService;
    private final PrdGeneratorService prdGeneratorService;
    
    @PostMapping("/projects")
    public ResponseEntity<Project> createProject(@RequestBody Map<String, String> request) {
        Project project = manyfastService.createProject(
            request.get("name"),
            request.get("description")
        );
        return ResponseEntity.ok(project);
    }
    
    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(manyfastService.getAllProjects());
    }
    
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<Project> getProject(@PathVariable String projectId) {
        return manyfastService.getProject(projectId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/projects/{projectId}/nodes")
    public ResponseEntity<ProjectNode> createNode(
        @PathVariable String projectId,
        @RequestBody NodeRequest request
    ) {
        ProjectNode node = manyfastService.createNode(projectId, request);
        return ResponseEntity.ok(node);
    }
    
    @PutMapping("/nodes/{nodeId}")
    public ResponseEntity<ProjectNode> updateNode(
        @PathVariable Long nodeId,
        @RequestBody NodeRequest request
    ) {
        ProjectNode node = manyfastService.updateNode(nodeId, request);
        return ResponseEntity.ok(node);
    }
    
    @DeleteMapping("/nodes/{nodeId}")
    public ResponseEntity<Void> deleteNode(@PathVariable Long nodeId) {
        manyfastService.deleteNode(nodeId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/projects/{projectId}/tree")
    public ResponseEntity<List<NodeResponse>> getProjectTree(@PathVariable String projectId) {
        return ResponseEntity.ok(manyfastService.getProjectTree(projectId));
    }
    
    @GetMapping("/projects/{projectId}/nodes")
    public ResponseEntity<List<ProjectNode>> getAllNodes(@PathVariable String projectId) {
        return ResponseEntity.ok(manyfastService.getAllNodes(projectId));
    }
    
    @PostMapping("/projects/{projectId}/generate-prd")
    public ResponseEntity<Map<String, String>> generatePRD(@PathVariable String projectId) {
        String prd = prdGeneratorService.generatePrd(projectId);
        return ResponseEntity.ok(Map.of("prd", prd));
    }
}
