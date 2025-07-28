package com.example.aiworkflowbackend.service;

import com.example.aiworkflowbackend.dto.WorkflowDtos.*;
import com.example.aiworkflowbackend.llm.ChatModelFactory;
import com.example.aiworkflowbackend.model.Workflow;
import com.example.aiworkflowbackend.repository.WorkflowRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class containing the business logic for managing and executing workflows.
 */
@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final ChatModelFactory chatModelFactory;

    public WorkflowService(WorkflowRepository workflowRepository, ChatModelFactory chatModelFactory) {
        this.workflowRepository = workflowRepository;
        this.chatModelFactory = chatModelFactory;
    }

    @Transactional
    public WorkflowDto createWorkflow(CreateWorkflowRequest request) {
        Workflow workflow = new Workflow(request.name(), request.promptTemplate());
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
        workflow.setPromptTemplate(request.promptTemplate());

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
        return new WorkflowDto(workflow.getId(), workflow.getName(), workflow.getPromptTemplate());
    }

    @Transactional(readOnly = true)
    public ExecuteWorkflowResponse executeWorkflow(Long id, ExecuteWorkflowRequest request) {
        // 1. Find the workflow
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + id));

        // 2. Get the correct ChatClient from the factory
        ChatClient chatClient = chatModelFactory.getChatClient(request.modelId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid modelId: " + request.modelId()));

        // 3. Create a prompt from the template and variables
        PromptTemplate promptTemplate = new PromptTemplate(workflow.getPromptTemplate());
        var prompt = promptTemplate.create(request.variables());

        // 4. Call the AI model and get the result
        String content = chatClient.prompt(prompt).call().content();

        return new ExecuteWorkflowResponse(content);
    }

    @Transactional(readOnly = true)
    public List<ModelProviderDto> getAvailableModels() {
        return chatModelFactory.getAvailableModels();
    }
}
