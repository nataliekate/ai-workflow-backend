package com.example.aiworkflowbackend.service;

import com.example.aiworkflowbackend.dto.WorkflowDtos.*;
import com.example.aiworkflowbackend.llm.ChatModelFactory;
import com.example.aiworkflowbackend.model.Workflow;
import com.example.aiworkflowbackend.repository.WorkflowRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class containing the business logic for managing and executing workflows.
 */
@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final ChatModelFactory chatModelFactory;
    private final ObjectMapper objectMapper;

    public WorkflowService(WorkflowRepository workflowRepository, ChatModelFactory chatModelFactory, ObjectMapper objectMapper) {
        this.workflowRepository = workflowRepository;
        this.chatModelFactory = chatModelFactory;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WorkflowDto createWorkflow(CreateWorkflowRequest request) {
        Workflow workflow = new Workflow(request.name(), request.nodesJson(), request.edgesJson());
        Workflow savedWorkflow = workflowRepository.save(workflow);
        return toDto(savedWorkflow);
    }

    @Transactional(readOnly = true)
    public List<WorkflowDto> getAllWorkflows() {
        return workflowRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkflowDto getWorkflowById(Long id) {
        return workflowRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + id));
    }

    @Transactional
    public WorkflowDto updateWorkflow(Long id, UpdateWorkflowRequest request) {
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + id));
        workflow.setName(request.name());
        workflow.setNodesJson(request.nodesJson());
        workflow.setEdgesJson(request.edgesJson());
        Workflow updatedWorkflow = workflowRepository.save(workflow);
        return toDto(updatedWorkflow);
    }

    @Transactional
    public void deleteWorkflow(Long id) {
        if (!workflowRepository.existsById(id)) {
            throw new EntityNotFoundException("Workflow not found with id: " + id);
        }
        workflowRepository.deleteById(id);
    }


    private WorkflowDto toDto(Workflow workflow) {
        return new WorkflowDto(workflow.getId(), workflow.getName(), workflow.getNodesJson(), workflow.getEdgesJson());
    }

    @Transactional(readOnly = true)
    public ExecuteResponse executeWorkflow(Long workflowId, ExecuteWorkflowRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + workflowId));

        try {
            List<Map<String, Object>> nodes = parseJson(workflow.getNodesJson());
            List<Map<String, Object>> edges = parseJson(workflow.getEdgesJson());

            Map<String, Object> executionState = new HashMap<>(request.initialVariables());

            String startNodeId = findStartNode(nodes, edges);
            String currentNodeId = startNodeId;
            String finalResult = "Workflow did not produce a final result.";

            while (currentNodeId != null) {
                Map<String, Object> currentNode = findNodeById(nodes, currentNodeId);
                String nodeType = (String) currentNode.get("type");

                if ("llmNode".equals(nodeType)) {
                    String promptTemplateStr = getPromptFromNodeData(currentNode);
                    String result = callAiModel(request.modelId(), promptTemplateStr, executionState);

                    // The output of this node is now available as a variable for subsequent nodes
                    executionState.put(currentNodeId, result);
                    finalResult = result; // Keep track of the last result
                }
                // Add other node types here (e.g., 'if ("apiNode".equals(nodeType)) { ... }')

                currentNodeId = findNextNodeId(edges, currentNodeId);
            }

            return new ExecuteResponse(finalResult);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse workflow JSON", e);
        }
    }

    @Transactional(readOnly = true)
    public ExecuteResponse executeSingleNode(Long workflowId, ExecuteSingleNodeRequest request) {
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + workflowId));
        String promptTemplateStr = extractPromptTemplateFromNode(workflow.getNodesJson(), request.nodeId());
        String result = callAiModel(request.modelId(), promptTemplateStr, request.variables());
        return new ExecuteResponse(result);
    }

    private String callAiModel(String modelId, String promptTemplateStr, Map<String, Object> variables) {
        ChatClient chatClient = chatModelFactory.getChatClient(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid modelId: " + modelId));
        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateStr);
        var prompt = promptTemplate.create(variables);
        return chatClient.prompt(prompt).call().content();
    }

    private String findStartNode(List<Map<String, Object>> nodes, List<Map<String, Object>> edges) {
        return nodes.stream()
                .map(node -> (String) node.get("id"))
                .filter(nodeId -> edges.stream().noneMatch(edge -> nodeId.equals(edge.get("target"))))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find a start node in the workflow."));
    }

    private String findNextNodeId(List<Map<String, Object>> edges, String sourceNodeId) {
        return edges.stream()
                .filter(edge -> sourceNodeId.equals(edge.get("source")))
                .map(edge -> (String) edge.get("target"))
                .findFirst()
                .orElse(null); // Returns null if it's the end of the line
    }

    private Map<String, Object> findNodeById(List<Map<String, Object>> nodes, String nodeId) {
        return nodes.stream()
                .filter(node -> nodeId.equals(node.get("id")))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Node with ID " + nodeId + " not found during execution."));
    }

    private String extractPromptTemplateFromNode(String nodesJson, String nodeId) {
        try {
            List<Map<String, Object>> nodes = parseJson(nodesJson);
            Map<String, Object> targetNode = findNodeById(nodes, nodeId);
            return getPromptFromNodeData(targetNode);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse nodes JSON", e);
        }
    }

    private String getPromptFromNodeData(Map<String, Object> node) {
        Map<String, Object> data = (Map<String, Object>) node.get("data");
        if (data == null || !(data.get("promptTemplate") instanceof String)) {
            throw new IllegalArgumentException("Node '" + node.get("id") + "' does not contain a valid 'promptTemplate'.");
        }
        return (String) data.get("promptTemplate");
    }

    private List<Map<String, Object>> parseJson(String json) throws JsonProcessingException {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<>() {
        };
        return objectMapper.readValue(json, typeRef);
    }

    public List<ModelProviderDto> getAvailableModels() {
        return chatModelFactory.getAvailableModels();
    }
}
