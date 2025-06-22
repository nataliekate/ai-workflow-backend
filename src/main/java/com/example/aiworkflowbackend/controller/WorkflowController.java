package com.example.aiworkflowbackend.controller;

import com.example.aiworkflowbackend.model.Workflow;
import com.example.aiworkflowbackend.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for managing Workflow entities.
 * @RestController: Marks this class as a Spring REST controller, handling incoming web requests.
 * @RequestMapping("/api/workflows"): Base path for all endpoints in this controller.
 * @CrossOrigin(origins = "http://localhost:5173"): Allows requests from your React development server.
 * Adjust this in production.
 */
@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = "http://localhost:5173") // Crucial for connecting with your React frontend
public class WorkflowController {

    @Autowired // Injects an instance of WorkflowRepository
    private WorkflowRepository workflowRepository;

    /**
     * GET /api/workflows
     * Retrieves all workflows from the database.
     * @return A list of Workflow objects.
     */
    @GetMapping
    public List<Workflow> getAllWorkflows() {
        return workflowRepository.findAll();
    }

    /**
     * GET /api/workflows/{id}
     * Retrieves a single workflow by its ID.
     * @param id The ID of the workflow to retrieve.
     * @return ResponseEntity with the Workflow object if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Workflow> getWorkflowById(@PathVariable Long id) {
        Optional<Workflow> workflow = workflowRepository.findById(id);
        return workflow.map(ResponseEntity::ok) // If workflow is present, return 200 OK with workflow
                .orElseGet(() -> ResponseEntity.notFound().build()); // Else, return 404 Not Found
    }

    /**
     * POST /api/workflows
     * Creates a new workflow.
     * @param workflow The Workflow object sent in the request body.
     * @return The saved Workflow object with its generated ID.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Returns 201 Created status
    public Workflow createWorkflow(@RequestBody Workflow workflow) {
        return workflowRepository.save(workflow);
    }

    /**
     * PUT /api/workflows/{id}
     * Updates an existing workflow.
     * @param id The ID of the workflow to update.
     * @param workflowDetails The updated Workflow object sent in the request body.
     * @return ResponseEntity with the updated Workflow object if found, or 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Workflow> updateWorkflow(@PathVariable Long id, @RequestBody Workflow workflowDetails) {
        Optional<Workflow> optionalWorkflow = workflowRepository.findById(id);
        if (optionalWorkflow.isPresent()) {
            Workflow existingWorkflow = optionalWorkflow.get();
            existingWorkflow.setName(workflowDetails.getName());
            existingWorkflow.setNodesJson(workflowDetails.getNodesJson());
            existingWorkflow.setEdgesJson(workflowDetails.getEdgesJson());
            // You might want to update other fields if they exist (e.g., updatedAt)
            return ResponseEntity.ok(workflowRepository.save(existingWorkflow));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE /api/workflows/{id}
     * Deletes a workflow by its ID.
     * @param id The ID of the workflow to delete.
     * @return ResponseEntity with 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        if (workflowRepository.existsById(id)) { // Check if the workflow exists before deleting
            workflowRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}
