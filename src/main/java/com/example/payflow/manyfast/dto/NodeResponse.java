package com.example.payflow.manyfast.dto;

import com.example.payflow.manyfast.entity.ProjectNode;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class NodeResponse {
    private Long id;
    private String title;
    private String description;
    private String nodeType;
    private Long parentId;
    private Integer position;
    private Double positionX;
    private Double positionY;
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NodeResponse> children;
    
    public static NodeResponse from(ProjectNode node) {
        NodeResponse response = new NodeResponse();
        response.setId(node.getId());
        response.setTitle(node.getTitle());
        response.setDescription(node.getDescription());
        response.setNodeType(node.getNodeType());
        response.setParentId(node.getParentId());
        response.setPosition(node.getPosition());
        response.setPositionX(node.getPositionX());
        response.setPositionY(node.getPositionY());
        response.setMetadata(node.getMetadata());
        response.setCreatedAt(node.getCreatedAt());
        response.setUpdatedAt(node.getUpdatedAt());
        return response;
    }
}
