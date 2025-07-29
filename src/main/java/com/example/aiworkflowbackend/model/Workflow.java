package com.example.aiworkflowbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a workflow entity stored in the database.
 * The workflow's structure, including individual node details like prompt templates,
 * is stored in the nodesJson and edgesJson fields.
 */
@Entity
@Data
@NoArgsConstructor
public class Workflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Stores the JSON representation of the workflow's nodes.
     * @Lob: Indicates that this field can store large objects (useful for long JSON strings).
     * @Column(columnDefinition = "TEXT"): Specifies the database column type as TEXT,
     * which can hold larger strings than VARCHAR.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String nodesJson;

    /**
     * Stores the JSON representation of the workflow's edges (connections).
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String edgesJson;

    public Workflow(String name, String nodesJson, String edgesJson) {
        this.name = name;
        this.nodesJson = nodesJson;
        this.edgesJson = edgesJson;
    }
}
