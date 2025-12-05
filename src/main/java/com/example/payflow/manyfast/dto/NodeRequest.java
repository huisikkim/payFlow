package com.example.payflow.manyfast.dto;

import lombok.Data;

@Data
public class NodeRequest {
    private String title;
    private String description;
    private String nodeType;
    private Long parentId;
    private Integer position;
    private Double positionX;
    private Double positionY;
    private String metadata;
}
