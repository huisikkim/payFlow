package com.example.payflow.manyfast.repository;

import com.example.payflow.manyfast.entity.ProjectNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectNodeRepository extends JpaRepository<ProjectNode, Long> {
    List<ProjectNode> findByProjectIdOrderByPositionAsc(String projectId);
    List<ProjectNode> findByProjectIdAndParentId(String projectId, Long parentId);
}
