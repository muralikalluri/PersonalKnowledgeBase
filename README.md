# Personal Knowledge Base Chat Application

A full-stack Microservices application that allows users to upload documents (PDFs, etc.), ingest and index them into a Vector Store, and perform RAG (Retrieval Augmented Generation) based chat using AWS Bedrock (Titan models).

## Architecture

The application behaves as a microservices architecture with the following components:

| Service | Port | Description |
| :--- | :--- | :--- |
| **Api Gateway** | `8080` | Entry point for all requests. Routes traffic to backend services. |
| **Document Service** | `8081` | Handles file uploads to AWS S3. |
| **Ingestion Service** | `8082` | Downloads files from S3, parses them (Tika), chunks, embeds (Titan), and stores in vector store. |
| **Chat Service** | `8083` | Handles chat requests, retrieves context from vector store, and calls LLM (Bedrock Titan) for answers. |
| **Frontend** | `4200` | Angular application for user interface. |

## Prerequisites

*   **Java 21+** (JDK 21 Recommended)
*   **Maven**
*   **Node.js** (v20+ recommended) & **npm**
*   **AWS Account** with access to Bedrock (Titan Embeddings V1, Titan Text Express/Lite) and S3.

## Configuration

The services are pre-configured to use `us-east-1` region. You **MUST** provide your AWS credentials.

The backend services (`ingestion-service` and `chat-service`) have been configured to look for these environment variables or fallback to hardcoded values (configured in `application.yml`).

For best security, set these environment variables in your terminal or IDE:

```bash
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"
export AWS_REGION="us-east-1"
```

*Note: The project currently includes default credentials in `launch.json` and `application.yml` for ease of local testing.*

## Running the Application

You can run the application services individually using Maven or via your IDE.

### 1. Start Backend Services

Open separate terminals for each service and run:

**API Gateway:**
```bash
cd api-gateway
mvn spring-boot:run
```

**Document Service:**
```bash
cd document-service
mvn spring-boot:run
```

**Ingestion Service:**
```bash
cd ingestion-service
mvn spring-boot:run
```

**Chat Service:**
```bash
cd chat-service
mvn spring-boot:run
```

### 2. Start Frontend

```bash
cd frontend
npm install # Only first time
ng serve
```

Access the application at: [http://localhost:4200](http://localhost:4200)

## Functionality

1.  **Upload**: Go to the "Upload" tab to select and upload document files.
2.  **Ingestion**: After upload, the file is automatically sent for ingestion (parsed and embedded).
3.  **Chat**: Go to the "Chat" tab to ask questions. The system will answer based on the context of your uploaded documents.

## Technology Stack

*   **Backend**: Java, Spring Boot 3.3.x, Spring AI 0.8.x, Spring Cloud Gateway.
*   **Frontend**: Angular 17/18, Tailwind CSS.
*   **AI/ML**: AWS Bedrock (Titan Embeddings, Titan Text Express), Apache Tika (Document Parsing), SimpleVectorStore (In-memory/File-based vector storage).
