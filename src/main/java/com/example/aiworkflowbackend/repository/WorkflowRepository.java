package com.example.aiworkflowbackend.repository;

import com.example.aiworkflowbackend.model.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA Repository for the Workflow entity.
 * @Repository: Indicates that this interface is a "repository", which is a mechanism
 * for encapsulating storage, retrieval, and search behavior.
 * JpaRepository: Provides out-of-the-box CRUD (Create, Read, Update, Delete) operations
 * and pagination/sorting capabilities for the Workflow entity.
 * It takes two generic parameters: the entity type (Workflow) and the type of its primary key (Long).
 */
@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    // Spring Data JPA automatically provides methods like:
    // save(Workflow workflow)
    // findById(Long id)
    // findAll()
    // deleteById(Long id)
    // existsById(Long id)
    // ...and many more!
}
