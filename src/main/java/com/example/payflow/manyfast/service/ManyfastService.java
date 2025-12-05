package com.example.payflow.manyfast.service;

import com.example.payflow.manyfast.dto.NodeRequest;
import com.example.payflow.manyfast.dto.NodeResponse;
import com.example.payflow.manyfast.entity.Project;
import com.example.payflow.manyfast.entity.ProjectNode;
import com.example.payflow.manyfast.repository.ProjectNodeRepository;
import com.example.payflow.manyfast.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManyfastService {
    
    private final ProjectRepository projectRepository;
    private final ProjectNodeRepository nodeRepository;
    
    @Transactional
    public Project createProject(String name, String description) {
        Project project = new Project();
        project.setProjectId(UUID.randomUUID().toString());
        project.setName(name);
        project.setDescription(description);
        project.setOwner("demo-user");
        return projectRepository.save(project);
    }
    
    public Optional<Project> getProject(String projectId) {
        return projectRepository.findByProjectId(projectId);
    }
    
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    @Transactional
    public ProjectNode createNode(String projectId, NodeRequest request) {
        ProjectNode node = new ProjectNode();
        node.setProjectId(projectId);
        node.setTitle(request.getTitle());
        node.setDescription(request.getDescription());
        node.setNodeType(request.getNodeType());
        node.setParentId(request.getParentId());
        node.setPosition(request.getPosition());
        node.setPositionX(request.getPositionX());
        node.setPositionY(request.getPositionY());
        node.setMetadata(request.getMetadata());
        return nodeRepository.save(node);
    }
    
    @Transactional
    public ProjectNode updateNode(Long nodeId, NodeRequest request) {
        ProjectNode node = nodeRepository.findById(nodeId)
            .orElseThrow(() -> new RuntimeException("Node not found"));
        
        if (request.getTitle() != null) node.setTitle(request.getTitle());
        if (request.getDescription() != null) node.setDescription(request.getDescription());
        if (request.getNodeType() != null) node.setNodeType(request.getNodeType());
        if (request.getParentId() != null) node.setParentId(request.getParentId());
        if (request.getPosition() != null) node.setPosition(request.getPosition());
        if (request.getPositionX() != null) node.setPositionX(request.getPositionX());
        if (request.getPositionY() != null) node.setPositionY(request.getPositionY());
        if (request.getMetadata() != null) node.setMetadata(request.getMetadata());
        
        return nodeRepository.save(node);
    }
    
    @Transactional
    public void deleteNode(Long nodeId) {
        nodeRepository.deleteById(nodeId);
    }
    
    public List<NodeResponse> getProjectTree(String projectId) {
        List<ProjectNode> allNodes = nodeRepository.findByProjectIdOrderByPositionAsc(projectId);
        Map<Long, NodeResponse> nodeMap = new HashMap<>();
        List<NodeResponse> rootNodes = new ArrayList<>();
        
        // Convert to response objects
        for (ProjectNode node : allNodes) {
            NodeResponse response = NodeResponse.from(node);
            response.setChildren(new ArrayList<>());
            nodeMap.put(node.getId(), response);
        }
        
        // Build tree structure
        for (ProjectNode node : allNodes) {
            NodeResponse response = nodeMap.get(node.getId());
            if (node.getParentId() == null) {
                rootNodes.add(response);
            } else {
                NodeResponse parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(response);
                }
            }
        }
        
        return rootNodes;
    }
    
    public List<ProjectNode> getAllNodes(String projectId) {
        return nodeRepository.findByProjectIdOrderByPositionAsc(projectId);
    }
}
