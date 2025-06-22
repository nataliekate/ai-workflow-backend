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
 * @Entity: Marks this class as a JPA entity, mapped to a database table.
 * @Data: Lombok annotation to automatically generate getters, setters, toString, equals, and hashCode.
 * @NoArgsConstructor: Lombok annotation to generate a no-argument constructor (required by JPA).
 * @AllArgsConstructor: Lombok annotation to generate a constructor with all fields.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workflow {
    /**
     * Primary key for the Workflow entity.
     * @Id: Marks the field as the primary key.
     * @GeneratedValue(strategy = GenerationType.IDENTITY): Configures the primary key to be
     * auto-incremented by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the workflow, provided by the user.
     */
    private String name;

    /**
     * Stores the JSON string representation of React Flow nodes.
     * @Lob: Indicates that this field can store large objects (useful for long JSON strings).
     * @Column(columnDefinition = "TEXT"): Specifies the database column type as TEXT,
     * which can hold larger strings than VARCHAR.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String nodesJson;

    /**
     * Stores the JSON string representation of React Flow edges.
     * Similar to nodesJson, it's a large text field.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String edgesJson;

    // You can add more fields here if needed, e.g., userId, createdAt, updatedAt
    // private String userId;
    // private Instant createdAt;
    // private Instant updatedAt;

}
