# Certificate Management Microservice

<div align="center">
  
  **Certificate CRUD Operations and Peer Mapping Management**
  
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
  [![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green.svg)](https://www.mongodb.com/cloud/atlas)
  [![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
  [![Deployed on Railway](https://img.shields.io/badge/Deployed%20on-Railway-purple.svg)](https://railway.app/)
</div>

---

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Security](#security)
- [Integration](#integration)
- [Deployment](#deployment)
- [Future Enhancements](#future-enhancements)

---

## Overview

This microservice is part of the **SkillCert Certificate Tracking Platform**, a comprehensive system for managing professional certifications. Microservice 2 handles all certificate-related operations, including CRUD functionality, file storage, and peer-to-certificate mapping management.

Built with Spring Boot and MongoDB, this service provides RESTful APIs for certificate management, peer verification workflows, and file storage using MongoDB GridFS.

**Related Repositories**:
- Frontend: [SDPFRONT](https://github.com/saimahendra282/SDPFRONT)
- User Management Service: [Microservice1](https://github.com/saimahendra282/Microservice1)

---

## Architecture

### System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        A[React Frontend]
    end
    
    subgraph "API Layer"
        B[Certificate Controller<br/>/api/certificates]
        C[Peer Mapping Controller<br/>/peer-mappings]
    end
    
    subgraph "Service Layer"
        D[Certificate Service]
        E[Peer Mapping Service]
        F[JWT Manager]
    end
    
    subgraph "Repository Layer"
        G[Certificate Repository]
        H[Peer Mapping Repository]
    end
    
    subgraph "Database Layer"
        I[(MongoDB Atlas<br/>certificates)]
        J[(MongoDB Atlas<br/>peer_mappings)]
        K[GridFS<br/>File Storage]
    end
    
    A -->|REST API| B
    A -->|REST API| C
    B --> D
    C --> E
    D --> F
    E --> F
    D --> G
    E --> H
    G --> I
    H --> J
    D --> K
    E --> K
    
    style A fill:#e1f5ff
    style D fill:#fff4e1
    style E fill:#fff4e1
    style I fill:#d4edda
    style J fill:#d4edda
    style K fill:#d1ecf1
```

### Data Flow Architecture

```mermaid
sequenceDiagram
    participant C as Client
    participant API as Certificate Controller
    participant S as Certificate Service
    participant JWT as JWT Manager
    participant R as Repository
    participant DB as MongoDB
    participant GFS as GridFS
    
    C->>API: POST /api/certificates<br/>(with JWT + Files)
    API->>S: saveCertificate(token, cert, pdf, badge)
    S->>JWT: extractEmailFromToken(token)
    JWT-->>S: email
    S->>S: set certificate email
    S->>GFS: saveFileToGridFS(pdfFile)
    GFS-->>S: pdfUrl (ObjectId)
    S->>GFS: saveFileToGridFS(badge)
    GFS-->>S: badgeUrl (ObjectId)
    S->>S: set file URLs
    S->>R: save(certificate)
    R->>DB: insert document
    DB-->>R: success
    R-->>S: certificate saved
    S-->>API: success
    API-->>C: 200 OK
```

### Peer Mapping Workflow

```mermaid
stateDiagram-v2
    [*] --> Created: Admin creates mapping
    Created --> Pending: Assign to peer
    Pending --> UnderReview: Peer reviews
    
    UnderReview --> Verified: Peer approves
    UnderReview --> Rejected: Peer rejects
    
    Verified --> RenewalRequested: User requests renewal
    RenewalRequested --> RenewalPending: Upload renewal docs
    RenewalPending --> RenewalApproved: Peer approves renewal
    RenewalPending --> RenewalRejected: Peer rejects renewal
    
    RenewalApproved --> [*]
    RenewalRejected --> [*]
    Rejected --> [*]
    Verified --> [*]
    
    note right of Created
        PeerMapping created with
        certificateId and peerEmail
    end note
    
    note right of UnderReview
        Peer can view certificate
        PDF and badge
    end note
    
    note right of RenewalRequested
        New PDF and expiry date
        uploaded
    end note
```

### Component Interaction

```mermaid
graph LR
    subgraph "Microservice 2 Components"
        A[Controllers]
        B[Services]
        C[Repositories]
        D[Models]
        E[JWT Manager]
        F[CORS Configuration]
    end
    
    subgraph "External Dependencies"
        G[Microservice 1<br/>User Service]
        H[MongoDB Atlas]
        I[GridFS]
    end
    
    A --> B
    B --> C
    B --> E
    C --> D
    C --> H
    B --> I
    A --> F
    E -.->|Validates JWT| G
    
    style A fill:#17a2b8
    style B fill:#28a745
    style C fill:#ffc107
    style E fill:#dc3545
```

### File Storage Architecture

```mermaid
graph TB
    subgraph "File Upload Flow"
        A[Client Upload<br/>PDF/Badge]
        B[Certificate Service]
        C{File Type}
        D[GridFsTemplate]
        E[(GridFS Chunks)]
        F[(GridFS Files)]
    end
    
    subgraph "File Retrieval Flow"
        G[GET Request<br/>/pdf or /badge]
        H[Certificate Service]
        I[GridFsTemplate Query]
        J[GridFsResource]
        K[InputStreamResource]
    end
    
    A --> B
    B --> C
    C -->|PDF| D
    C -->|Image| D
    D --> E
    D --> F
    
    G --> H
    H --> I
    I --> F
    F --> J
    J --> K
    K --> G
    
    style A fill:#e3f2fd
    style B fill:#fff3e0
    style D fill:#e8f5e9
    style E fill:#f3e5f5
    style F fill:#f3e5f5
```

---

## Technology Stack

### Core Framework
- **Spring Boot** 3.4.0 - Enterprise Java framework
- **Java** 17 - Programming language
- **Maven** - Dependency management and build tool

### Database and Storage
- **MongoDB Atlas** - Cloud-hosted NoSQL database
- **Spring Data MongoDB** - MongoDB integration
- **GridFS** - Large file storage system

### Security
- **JJWT** (0.11.5) - JWT token generation and validation
  - jjwt-api
  - jjwt-impl
  - jjwt-jackson
- **AES Encryption** - Data encryption for sensitive fields

### Web and API
- **Spring Boot Starter Web** - RESTful API development
- **Spring Boot Starter Validation** - Input validation
- **Spring Boot Starter Thymeleaf** - Template engine (optional)

### Development Tools
- **Spring Boot DevTools** - Hot reload and development utilities
- **Spring Boot Starter Test** - Testing framework

---

## Features

### Certificate Management
- Create and store certificates with comprehensive metadata
- Upload and manage certificate PDF files using GridFS
- Upload and manage badge images using GridFS
- Retrieve certificates by ID or user email
- Fetch all certificates with JWT authentication
- Track certificate details (name, trackId, trackUrl, issuedBy, dates)

### Peer Mapping Management
- Create peer-to-certificate mappings
- Assign certificates to peers for verification
- Update verification status (Pending, Verified, Rejected)
- Add peer comments and feedback
- Track peer verification workflow

### Certificate Renewal
- Request certificate renewal with new documents
- Upload renewal PDF files
- Specify renewal reasons and new expiry dates
- Approve or reject renewal requests
- Track renewal status separately from verification status

### File Management
- Store PDF files in MongoDB GridFS
- Store badge images in GridFS
- Retrieve files by ObjectId
- Stream files directly to clients
- Handle multiple file types (PDF, PNG, JPG)

### Security
- JWT token validation for all endpoints
- Email extraction from encrypted JWT claims
- Role-based access control integration
- CORS configuration for frontend integration
- Encrypted sensitive data in JWT tokens

---

## Project Structure

```
Microservice2/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── certi/
│   │   │           ├── controllers/
│   │   │           │   ├── CertificateController.java          # Certificate REST API
│   │   │           │   └── PeerMappingController.java          # Peer mapping REST API
│   │   │           │
│   │   │           ├── services/
│   │   │           │   ├── CertificateService.java             # Certificate business logic
│   │   │           │   ├── PeerMappingService.java             # Peer mapping business logic
│   │   │           │   └── JWTManager.java                     # JWT handling & encryption
│   │   │           │
│   │   │           ├── repositories/
│   │   │           │   ├── CertificateRepository.java          # Certificate data access
│   │   │           │   └── PeerMappingRepository.java          # Peer mapping data access
│   │   │           │
│   │   │           ├── models/
│   │   │           │   ├── Certificate.java                    # Certificate entity
│   │   │           │   ├── PeerMapping.java                    # Peer mapping entity
│   │   │           │   └── PeerMappingWithCertificateDetails.java
│   │   │           │
│   │   │           ├── config/
│   │   │           │   └── WebConfig.java                      # CORS configuration
│   │   │           │
│   │   │           └── CertificatesApplication.java            # Main application class
│   │   │
│   │   └── resources/
│   │       ├── application.properties                          # Configuration file
│   │       └── templates/                                       # Thymeleaf templates (if used)
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── certi/
│                   └── CertificatesApplicationTests.java
│
├── .mvn/                                                        # Maven wrapper
├── mvnw                                                          # Maven wrapper script (Unix)
├── mvnw.cmd                                                      # Maven wrapper script (Windows)
├── pom.xml                                                       # Maven dependencies
├── .gitignore                                                    # Git ignore rules
└── README.md                                                     # Project documentation
```

---

## API Endpoints

### Certificate Endpoints

#### Upload Certificate
```http
POST /api/certificates
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}

Parameters:
- name: String (Certificate name)
- trackId: String (Tracking ID)
- trackUrl: String (Tracking URL)
- issuedBy: String (Issuing authority)
- issuedDate: String (Issue date)
- expiryDate: String (Expiry date)
- pdfFile: MultipartFile (Certificate PDF)
- badge: MultipartFile (Badge image)
```

#### Get All Certificates
```http
GET /api/certificates/all
Authorization: Bearer {JWT_TOKEN}

Response: List<Certificate>
```

#### Get Certificates by Email
```http
GET /api/certificates/email
Authorization: Bearer {JWT_TOKEN}

Response: List<Certificate> (filtered by logged-in user)
```

#### Get Certificate PDF
```http
GET /api/certificates/pdf/{fileId}

Response: PDF file stream
Content-Type: application/pdf
```

#### Get Certificate Badge
```http
GET /api/certificates/badge/{fileId}

Response: Image file stream
Content-Type: image/png
```

#### Get Certificate by ID
```http
GET /api/certificates/{id}

Response: Certificate object
```

### Peer Mapping Endpoints

#### Create Peer Mapping
```http
POST /peer-mappings/add
Content-Type: application/json

Body:
{
  "peerEmail": "peer@example.com",
  "peerName": "John Doe",
  "certificateId": "certificate_id_here"
}

Response: 201 Created
```

#### Get All Peer Mappings
```http
GET /peer-mappings/all

Response: List<PeerMapping>
```

#### Get Self-Assigned Mappings
```http
GET /peer-mappings/mapped/self
Authorization: Bearer {JWT_TOKEN}

Response: List<PeerMapping> (for logged-in peer)
```

#### Update Mapping Status
```http
POST /peer-mappings/update-status/{mappingId}
Content-Type: application/json

Body:
{
  "status": "Verified" | "Rejected",
  "comment": "Feedback comment"
}

Response: 200 OK
```

#### Request Certificate Renewal
```http
POST /peer-mappings/renew/{mappingId}
Content-Type: multipart/form-data

Parameters:
- renewalPdf: MultipartFile (New certificate PDF)
- renewalReason: String (Reason for renewal)
- newExpiryDate: String (New expiry date)

Response: 200 OK
```

---

## Database Schema

### Certificate Collection

```javascript
{
  _id: ObjectId,
  name: String,                  // Certificate name
  trackId: String,               // Unique tracking identifier
  trackUrl: String,              // External tracking URL
  IssuedBy: String,              // Issuing organization
  issuedDate: String,            // Date certificate was issued
  expiryDate: String,            // Date certificate expires
  pdfFile: String,               // GridFS ObjectId for PDF
  badge: String,                 // GridFS ObjectId for badge image
  email: String                  // User email (from JWT)
}
```

### PeerMapping Collection

```javascript
{
  _id: ObjectId,
  peerEmail: String,             // Email of assigned peer
  peerName: String,              // Name of assigned peer
  certificateId: String,         // Reference to Certificate._id
  status: String,                // "Pending" | "Verified" | "Rejected"
  comment: String,               // Peer feedback/comments
  
  // Renewal fields
  renewalRequested: Boolean,     // Default: false
  renewalReason: String,         // Reason for renewal request
  renewalStatus: String,         // "Pending" | "Approved" | "Rejected"
  renewPdfFile: String,          // GridFS ObjectId for renewal PDF
  newExpiryDate: String          // New expiry date after renewal
}
```

### GridFS Collections

GridFS automatically creates two collections:

#### fs.files
```javascript
{
  _id: ObjectId,
  length: Number,
  chunkSize: Number,
  uploadDate: Date,
  filename: String,
  metadata: Object
}
```

#### fs.chunks
```javascript
{
  _id: ObjectId,
  files_id: ObjectId,            // Reference to fs.files._id
  n: Number,                     // Chunk number
  data: BinData                  // Binary data chunk
}
```

---

## Getting Started

### Prerequisites
- **Java Development Kit (JDK) 17** or higher
- **Maven 3.6+** (or use included Maven wrapper)
- **MongoDB Atlas account** or local MongoDB installation
- **Microservice 1** running for JWT token generation

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/saimahendra282/Microservice2.git
cd Microservice2
```

2. **Configure MongoDB connection**

Edit `src/main/resources/application.properties`:
```properties
spring.application.name=certificates
server.port=5000

# MongoDB Configuration
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/certificate-db

# GridFS Configuration (optional)
spring.data.mongodb.gridfs.database=certificate-db
```

3. **Build the project**

Using Maven wrapper (recommended):
```bash
# Unix/Linux/MacOS
./mvnw clean install

# Windows
mvnw.cmd clean install
```

Or using installed Maven:
```bash
mvn clean install
```

4. **Run the application**
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Using installed Maven
mvn spring-boot:run

# Or run the JAR directly
java -jar target/certificates-0.0.1-SNAPSHOT.jar
```

5. **Verify the application is running**
```bash
curl http://localhost:5000/api/certificates/all \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Configuration

### Application Properties

```properties
# Application Settings
spring.application.name=certificates
server.port=5000

# MongoDB Connection
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/<database>
spring.data.mongodb.database=certificate-db

# GridFS Configuration
spring.data.mongodb.gridfs.database=certificate-db
spring.data.mongodb.gridfs.bucket=fs

# File Upload Settings
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging
logging.level.com.certi=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG
```

### CORS Configuration

The application is configured to accept requests from the frontend:

```java
@Configuration
public class WebConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("https://sdp-vo1.netlify.app")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "Accept")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}
```

### JWT Configuration

JWT Manager uses AES encryption for sensitive fields:

```java
public final String SECRET_KEY = "YOUR_SECRET_KEY_HERE";
public final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
```

**Important**: Change the secret key in production and store it securely.

---

## Security

### JWT Token Validation

All protected endpoints require a valid JWT token:

```java
@GetMapping("/all")
public ResponseEntity<List<Certificate>> getAllCertificates(
    @RequestHeader("Authorization") String authorizationHeader
) {
    String token = authorizationHeader.replace("Bearer ", "");
    String email = certificateService.extractEmailFromToken(token);
    // ... rest of the logic
}
```

### Data Encryption

Sensitive fields in JWT tokens are encrypted using AES:

```java
private String encryptData(String data) {
    // AES encryption implementation
}

private String decryptData(String encryptedData) {
    // AES decryption implementation
}
```

### Token Structure

JWT tokens contain encrypted user information:

```json
{
  "email": "encrypted_email_here",
  "role": "user|peer|admin",
  "name": "User Name",
  "profilePic": "profile_url",
  "phone": "encrypted_phone_here",
  "iat": 1234567890,
  "exp": 1234654290
}
```

---

## Integration

### Integration with Microservice 1

This service validates JWT tokens generated by Microservice 1:

```java
public String extractEmailFromToken(String token) {
    Map<String, String> claims = jwtManager.validateToken(token);
    if (claims.get("code").equals("200")) {
        return claims.get("email");
    } else {
        throw new RuntimeException("Invalid or expired token.");
    }
}
```

### Integration with Frontend

The frontend makes authenticated requests:

```javascript
// Example: Upload certificate
const uploadCertificate = async (formData) => {
  const token = localStorage.getItem('jwtToken');
  
  const response = await axios.post(
    'https://microservice2-production.up.railway.app/api/certificates',
    formData,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'multipart/form-data'
      }
    }
  );
  
  return response.data;
};
```

---

## Deployment

### Railway Deployment

1. **Connect GitHub repository to Railway**

2. **Set environment variables**
```bash
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/certificate-db
SERVER_PORT=5000
JWT_SECRET_KEY=your_secret_key_here
```

3. **Configure build settings**
```
Build Command: ./mvnw clean package -DskipTests
Start Command: java -jar target/certificates-0.0.1-SNAPSHOT.jar
```

4. **Deploy**
Railway automatically deploys on push to main branch

### Docker Deployment (Planned)

Create `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/certificates-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 5000
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:
```bash
docker build -t microservice2 .
docker run -p 5000:5000 \
  -e SPRING_DATA_MONGODB_URI="mongodb+srv://..." \
  microservice2
```

---

## Future Enhancements

### Technical Improvements
- Implement Docker containerization
- Add comprehensive unit and integration tests
- Implement caching with Redis for frequently accessed certificates
- Add request rate limiting
- Implement database connection pooling optimization
- Add health check endpoints for monitoring

### Feature Additions
- Certificate expiry notification system
- Bulk certificate upload functionality
- Certificate version history tracking
- Advanced search and filtering capabilities
- Certificate analytics and reporting
- Export certificates to various formats (PDF, JSON, CSV)
- Certificate validation webhook notifications

### Security Enhancements
- Implement OAuth2 authentication
- Add API key management for third-party integrations
- Implement field-level encryption for sensitive data
- Add audit logging for all operations
- Implement HTTPS/TLS certificate pinning

---

## API Testing

### Using cURL

**Upload Certificate**:
```bash
curl -X POST http://localhost:5000/api/certificates \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "name=AWS Certified Developer" \
  -F "trackId=AWS-123456" \
  -F "trackUrl=https://verify.aws.com/123456" \
  -F "issuedBy=Amazon Web Services" \
  -F "issuedDate=2025-01-01" \
  -F "expiryDate=2028-01-01" \
  -F "pdfFile=@certificate.pdf" \
  -F "badge=@badge.png"
```

**Get All Certificates**:
```bash
curl -X GET http://localhost:5000/api/certificates/all \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Using Postman

1. Import the collection from `/docs/postman_collection.json` (if available)
2. Set environment variable `BASE_URL` to `http://localhost:5000`
3. Set environment variable `JWT_TOKEN` to your valid token
4. Execute requests from the collection

---

## Troubleshooting

### Common Issues

**Issue**: Connection refused to MongoDB
```
Solution: Check MongoDB URI and network connectivity
Verify: spring.data.mongodb.uri in application.properties
```

**Issue**: JWT token validation fails
```
Solution: Ensure JWT_SECRET_KEY matches Microservice 1
Verify: Token is not expired (24-hour validity)
```

**Issue**: File upload fails
```
Solution: Check file size limits in application.properties
Verify: GridFS is properly configured
Check: spring.servlet.multipart.max-file-size
```

**Issue**: CORS errors from frontend
```
Solution: Update allowedOrigins in WebConfig.java
Verify: Frontend URL is correctly configured
```

---



<div align="center">
  <p>Part of the SkillCert Certificate Tracking Platform</p>
  <p><strong>Microservice 2</strong> - Certificate and Peer Mapping Management</p>
</div>
