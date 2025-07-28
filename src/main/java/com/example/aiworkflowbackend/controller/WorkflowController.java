package com.example.aiworkflowbackend.controller;

import com.example.aiworkflowbackend.dto.WorkflowDtos.*;
import com.example.aiworkflowbackend.model.Workflow;
import com.example.aiworkflowbackend.repository.WorkflowRepository;
import com.example.aiworkflowbackend.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller that exposes endpoints for the workflow and model APIs.
 */
@RestController
@RequestMapping("/api/workflows")
// Use @CrossOrigin if your React app is served from a different origin
@CrossOrigin(origins = "http://localhost:5173")
public class WorkflowController {
    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    // --- Endpoints for Model Providers ---
    @GetMapping("/models")
    public ResponseEntity<List<ModelProviderDto>> getAvailableModels() {
        return ResponseEntity.ok(workflowService.getAvailableModels());
    }

    // --- CRUD Endpoints for Workflows ---

    @PostMapping("/workflows")
    public ResponseEntity<WorkflowDto> createWorkflow(@RequestBody CreateWorkflowRequest request) {
        WorkflowDto createdWorkflow = workflowService.createWorkflow(request);
        return new ResponseEntity<>(createdWorkflow, HttpStatus.CREATED);
    }

    @GetMapping("/workflows")
    public ResponseEntity<List<WorkflowDto>> getAllWorkflows() {
        return ResponseEntity.ok(workflowService.getAllWorkflows());
    }

    @GetMapping("/workflows/{id}")
    public ResponseEntity<WorkflowDto> getWorkflowById(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflowById(id));
    }

    @PutMapping("/workflows/{id}")
    public ResponseEntity<WorkflowDto> updateWorkflow(@PathVariable Long id, @RequestBody UpdateWorkflowRequest request) {
        return ResponseEntity.ok(workflowService.updateWorkflow(id, request));
    }

    @DeleteMapping("/workflows/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoint for Workflow Execution ---

    @PostMapping("/workflows/{id}/execute")
    public ResponseEntity<ExecuteWorkflowResponse> executeWorkflow(
            @PathVariable Long id,
            @RequestBody ExecuteWorkflowRequest request) {
        return ResponseEntity.ok(workflowService.executeWorkflow(id, request));
    }

}
