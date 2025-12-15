# Personal Knowledge Base Chat Application

A full-stack Microservices application that allows users to upload documents (PDFs, etc.), ingest and index them into a Vector Store, and perform RAG (Retrieval Augmented Generation) based chat using AWS Bedrock (Titan models).

## Architecture

The application behaves as a microservices architecture with the following components:

| Service | Port | Description |
| :--- | :--- | :--- |
| **Api Gateway** | `8080` | Entry point for all requests. Routes traffic to backend services. |
| **Document Service** | `8081` | Handles file uploads to AWS S3. |
| **Ingestion Service** | `8082` | Downloads files from S3, parses them (Tika), chunks, embeds (Titan Text Embed v1), and stores in vector store. |
| **Chat Service** | `8083` | Handles chat requests, retrieves context from vector store, and calls LLM (Bedrock Titan Text Express) for answers. |
| **Frontend** | `4200` | Angular application for user interface. |

## Prerequisites

*   **Java 21+** (JDK 21 Recommended)
*   **Maven**
*   **Node.js** (v20+ recommended) & **npm**
*   **AWS Account** with access to:
    *   **Bedrock Models**: `amazon.titan-embed-text-v1` (us-east-1) and `amazon.titan-text-express-v1`.
    *   **S3**: A bucket for storing uploaded documents.

## Configuration

**CRITICAL**: You must set the following environment variables. The application relies on `EnvironmentVariableCredentialsProvider` for AWS Authentication.

Set these in your terminal (PowerShell example) or IDE Launch Configuration:

```powershell
$env:AWS_ACCESS_KEY_ID="your-access-key"
$env:AWS_SECRET_ACCESS_KEY="your-secret-key"
$env:AWS_REGION="us-east-1"
$env:AWS_S3_BUCKET="your-s3-bucket-name"
```

*Note: The `launch.json` is pre-configured for VS Code debugging, but you must replace the placeholder credentials with valid ones.*

## Running the Application

It is recommended to build the services first to ensure dependencies are resolved.

### 1. Build All Services
```bash
mvn clean package -DskipTests
```
*(Run this in the root or individually in each service folder: api-gateway, document-service, ingestion-service, chat-service)*

### 2. Start Backend Services
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

### 3. Start Frontend
```bash
cd frontend
npm install # Only first time
ng serve
```

Access the application at: [http://localhost:4200](http://localhost:4200)

## Functionality

1.  **Upload**: Go to the "Upload" tab to select and upload document files.
2.  **Ingestion**: After upload, the file is automatically sent for ingestion (parsed and embedded). *Check Ingestion Service logs for confirmation.*
3.  **Chat**: Go to the "Chat" tab to ask questions. The system will answer based on the context of your uploaded documents.

## Technology Stack

*   **Backend**: Java 21, Spring Boot 3.3.x, Spring AI 0.8.0, Spring Cloud Gateway.
*   **Frontend**: Angular 17/18, Tailwind CSS.
*   **AI/ML**: AWS Bedrock (Titan Embeddings, Titan Text Express), Apache Tika (Document Parsing), SimpleVectorStore (In-memory/File-based vector storage).
