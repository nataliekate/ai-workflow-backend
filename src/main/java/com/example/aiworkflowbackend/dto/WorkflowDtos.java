package com.example.aiworkflowbackend.dto;

import java.util.Map;

/**
 * DTOs (Data Transfer Objects) for API communication.
 * Using records for concise, immutable data carriers.
 */
public class WorkflowDtos {

    // Represents a workflow in API responses
    public record WorkflowDto(Long id, String name, String promptTemplate) {}

    // Used for creating a new workflow
    public record CreateWorkflowRequest(String name, String promptTemplate) {}

    // Used for updating an existing workflow
    public record UpdateWorkflowRequest(String name, String promptTemplate) {}

    // Used for executing a workflow
    public record ExecuteWorkflowRequest(String modelId, Map<String, Object> variables) {}

    // Represents the result of a workflow execution
    public record ExecuteWorkflowResponse(String result) {}

    // Represents an available model provider
    public record ModelProviderDto(String id, String description) {}
}
