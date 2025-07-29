package com.example.aiworkflowbackend.dto;

import java.util.Map;

/**
 * DTOs (Data Transfer Objects) for API communication.
 * Using records for concise, immutable data carriers.
 */
public class WorkflowDtos {

    // Represents a workflow in API responses.
    public record WorkflowDto(Long id, String name, String nodesJson, String edgesJson) {}

    // Used for creating a new workflow.
    public record CreateWorkflowRequest(String name, String nodesJson, String edgesJson) {}

    // Used for updating an existing workflow.
    public record UpdateWorkflowRequest(String name, String nodesJson, String edgesJson) {}

    // Used for executing a FULL workflow from start to finish.
    public record ExecuteWorkflowRequest(String modelId, Map<String, Object> initialVariables) {}

    // Used for executing a SINGLE node within a workflow.
    public record ExecuteSingleNodeRequest(String modelId, String nodeId, Map<String, Object> variables) {}

    // Represents the result of an execution.
    public record ExecuteResponse(String result) {}

    // Represents an available model provider.
    public record ModelProviderDto(String id, String description) {}
}
