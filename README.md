# AI Workflow Builder

## 1. Project Overview

This project presents the AI Workflow Builder, a web application designed for the visual creation and execution of AI-driven workflows. It integrates React Flow for an intuitive drag-and-drop frontend interface with a Spring Boot backend for robust data persistence. The platform's core objective is to provide a versatile mechanism for orchestrating various AI tasks, specifically text generation and image synthesis. Workflow execution is primarily client-side, with definitions securely stored in a Firebase Firestore database via the Spring Boot API.

## 2. Features

* **Visual Workflow Editor:** Facilitates intuitive construction of AI workflows using a node-based, drag-and-drop interface.
* **Specialized AI Nodes:** Enables the creation of textual content via Gemini Flash and visual content from prompts utilizing Imagen.
* **Workflow Persistence:** Provides secure storage, retrieval, and deletion of workflow definitions through a RESTful API.
* **Client-Side AI Execution:** Workflows are processed, and AI model calls are initiated directly from the client's browser.
* **Spring Boot Backend:** Establishes a dependable infrastructure for managing workflow data.
* **Swagger/OpenAPI Documentation:** Offers automatically generated API documentation for backend endpoints via Swagger UI.

## Installation

To set up and run this project locally, please follow the instructions below:

### Prerequisites

Ensure the following software and dependencies are installed on your system:

* Java Development Kit (JDK) 17 or higher
* Apache Maven 3.6 or higher
* Node.js 18 or higher
* npm 9+ (or Yarn)

### Setup

1.  **Clone the Repository:**
    ```bash
    git clone [repository_url]
    # Navigate to your project root, which should contain 'ai-workflow-backend' and 'ai-workflow-frontend'
    cd ai-workflow-builder
    ```
2.  **Backend Setup (Spring Boot):**
    * Navigate to the backend directory: `cd ai-workflow-backend`
    * Build the project and install dependencies: `./mvnw clean install`
    * Ensure `application.yml` is configured for H2 database.
    * Verify `Workflow.java`, `WorkflowRepository.java`, and `WorkflowController.java` are in place.
    * Confirm `springdoc-openapi-starter-webmvc-ui` is in `pom.xml`.
    * (Optional) Ensure `OpenApiConfig.java` is present for custom Swagger UI.
3.  **Frontend Setup (React):**
    * Navigate to the frontend directory: `cd ../ai-workflow-frontend`
    * Install Node.js dependencies: `npm install`
    * Ensure `react-flow`, `tailwindcss` are installed.
    * Verify `tailwind.config.js` and `index.css` are configured for Tailwind CSS.
    * Confirm core React components (`App.js`, `CustomNodes.js`, `WorkflowPanel.js`, `aiApi.js`) are present and properly configured (including `API_KEY` for local testing if not in Canvas environment).

## Usage

To operate the application after installation, utilize the following commands:

* **To Run Backend Service:**
    ```bash
    cd ai-workflow-backend
    ./mvnw spring-boot:run
    ```
  The backend will be accessible at `http://localhost:8080`. API documentation (Swagger UI) is available at `http://localhost:8080/swagger-ui.html`.

* **To Run Frontend Development Server:**
    ```bash
    cd ai-workflow-frontend
    npm run dev
    ```
  The frontend will be accessible at `http://localhost:5173`.

* **Application Access:**
  Navigate to `http://localhost:5173` in a web browser.

### Backend API Endpoints

The Spring Boot backend exposes RESTful API endpoints for workflow management:

* `GET /api/workflows`: Retrieves a list of all stored workflows.
* `GET /api/workflows/{id}`: Fetches a specific workflow by its ID.
* `POST /api/workflows`: Creates a new workflow definition.
* `PUT /api/workflows/{id}`: Modifies an existing workflow definition.
* `DELETE /api/workflows/{id}`: Removes a workflow definition.

### Client-Side Workflow Execution Overview

Upon activation of the "Run Workflow" feature within the frontend:
1.  The `trigger` node is identified as the initiation point.
2.  Interconnected nodes are processed sequentially.
3.  `aiTextGeneration` nodes dispatch API calls to the Gemini Flash API.
4.  `aiImageGeneration` nodes send requests to the Imagen API.
5.  Resultant outputs (textual responses or image URLs) are dynamically rendered within their respective nodes on the user interface.


### Future Enhancements / Roadmap
Ongoing development plans for this application include the following key enhancements:
- Backend-Driven Workflow Execution: Implementation of a comprehensive workflow execution engine within the Spring Boot backend. This will involve the integration of message queues (e.g., RabbitMQ, Apache Kafka) and dedicated worker processes to facilitate reliable, scalable, and asynchronous background task execution. This architectural shift will also enable centralized management of AI API keys and support scheduled or instant webhook triggers.
- User Authentication and Authorization: Development of a robust user management system, extending beyond the current anonymous Firebase authentication.
- Expanded AI Model Integrations: Broadening support for additional Large Language Models (LLMs) and other AI services.
- Advanced Workflow Logic: Introduction of conditional nodes to enable "if/then" branching capabilities, development of looping constructs, support for parallel execution, and enhancement of error handling and retry mechanisms within the backend execution engine.
- Refined Data Mapping User Interface: Improvement of the user interface for mapping data outputs from one node to inputs of subsequent nodes.
- Workflow History and Logging: Implementation of a detailed logging system to record the history of each workflow execution, including inputs, outputs, and status.
- Pre-built Workflow Templates: Provision of a library of pre-defined workflow templates to facilitate rapid creation of common AI use cases.