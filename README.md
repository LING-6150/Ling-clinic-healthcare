# Ling Clinic — AI-Powered Healthcare Platform

A full-stack clinic management platform with RAG-based AI health assistant, built with Spring Boot, Vue 3, PostgreSQL + PGVector, and deployed on AWS EC2.

**Live Demo:** http://98.93.64.174

---

## Features

### For Patients
- **User Authentication** — Register, login, session management with JWT-style cookie auth
- **Appointment Booking** — Select doctor, date, and available time slots; view and cancel appointments
- **AI Health Assistant** — Ask health questions and receive answers grounded in the clinic's internal knowledge base

### For Admins
- **Document Management** — Upload PDF/TXT medical knowledge documents; automatic ingestion pipeline
- **Appointment Overview** — View all patient appointments across doctors

---

## System Architecture

```
Vue 3 Frontend (Nginx)
        ↓ HTTP
Spring Boot Backend (Port 8123)
        ↓
┌───────────────────────────────────┐
│  PostgreSQL + PGVector (Docker)   │
│  ├── users / appointments         │
│  ├── documents / document_chunks  │
│  └── vector embeddings (HNSW)     │
└───────────────────────────────────┘
        ↓
   OpenAI API
   (Embedding + Chat Completion)
```

---

## RAG Pipeline

### Document Ingestion
```
Upload PDF/TXT
      ↓
Text Extraction (PDFBox)
      ↓
SHA-256 Deduplication
      ↓
Semantic Chunking (sentence boundary splitting)
      ↓
Batch Embedding (text-embedding-3-small)
      ↓
PGVector Storage (HNSW index + full-text index)
```

### Query Pipeline
```
User Question
      ↓
Question Embedding
      ↓
┌─────────────────────────────────┐
│         Hybrid Search           │
│  ① Vector Search (PGVector)     │
│  ② Keyword Search (tsvector)    │
└──────────────┬──────────────────┘
               ↓
    RRF Merge (Reciprocal Rank Fusion)
               ↓
         Top-K Chunks
               ↓
    Prompt Construction
    (System + Context + History + Question)
               ↓
    OpenAI Chat API (stream=true)
               ↓
    SSE Streaming Response to Frontend
```

### Key Technical Decisions

**Hybrid Search** — Combines vector similarity search with PostgreSQL full-text search (tsvector). Pure vector search can miss exact TCM terminology (e.g. herb names); keyword search compensates for this gap.

**RRF Algorithm** — Merges ranked results from both retrieval methods using `score = 1 / (60 + rank)`. Chunks appearing in both lists receive higher combined scores, improving retrieval precision.

**Metadata Filtering** — Each chunk is tagged with `doc_type` (herb/treatment/diagnosis) and `symptom_tag`. Pre-filtering before vector search narrows the search space and reduces irrelevant results.

**Semantic Chunking** — Text is split at sentence boundaries rather than fixed character counts, preserving semantic integrity of each chunk and improving embedding quality.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Vue 3, TypeScript, Ant Design Vue, Vite |
| Backend | Spring Boot 3.5, MyBatis-Flex |
| Database | PostgreSQL 15 + PGVector (Docker) |
| AI | OpenAI text-embedding-3-small, GPT-4o-mini |
| Deployment | AWS EC2 (t3.micro, Ubuntu 24.04), Nginx |
| Build | Maven, Node.js 22 |

---

## Project Structure

```
├── src/main/java/com/ling/
│   ├── controller/
│   │   ├── UserController.java
│   │   ├── AppointmentController.java
│   │   ├── DocumentController.java
│   │   └── RagController.java
│   ├── service/
│   │   ├── impl/
│   │   │   ├── DocumentServiceImpl.java   # Ingestion pipeline
│   │   │   └── RagServiceImpl.java        # Hybrid search + RRF
│   │   ├── EmbeddingService.java          # OpenAI embedding API
│   │   └── OpenAiChatService.java         # SSE streaming chat
│   └── mapper/
│       └── DocumentChunkMapper.xml        # Vector + keyword SQL
│
└── Ling-AI-CODE-generation-frontend/
    └── src/
        ├── pages/
        │   ├── appointment/
        │   ├── admin/
        │   └── AiChatPage.vue             # SSE streaming UI
        └── api/
            └── ragController.ts           # Fetch-based SSE client
```

---

## Database Schema

```sql
-- Core tables
user               -- Authentication and roles
appointment        -- Booking with status (PENDING/CONFIRMED/CANCELLED)
document           -- Uploaded file metadata + SHA-256 hash
document_chunk     -- RAG chunks with vector embeddings

-- Key indexes
HNSW index on document_chunk(embedding)      -- Fast vector search
GIN index on document_chunk(content_tsv)     -- Full-text search
```

---

## API Endpoints

| Method | Path | Description |
|---|---|---|
| POST | /api/user/register | User registration |
| POST | /api/user/login | Login |
| POST | /api/appointment/create | Book appointment |
| GET  | /api/appointment/slots | Available time slots |
| POST | /api/appointment/my/list | My appointments |
| POST | /api/document/upload | Upload + ingest document (admin) |
| GET  | /api/document/list | List documents |
| POST | /api/rag/chat | AI health assistant (SSE) |

---

## Local Development

### Prerequisites
- Java 21
- Node.js 22
- Docker

### Backend
```bash
# Start PostgreSQL + PGVector
docker run -d \
  --name ling-postgres \
  -e POSTGRES_DB=ling_clinic \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=LingPostgres123! \
  -p 5432:5432 \
  ankane/pgvector

# Configure application-local.yml with your OpenAI API key
# Run Spring Boot
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Frontend
```bash
cd Ling-AI-CODE-generation-frontend
npm install
npm run dev
```

Visit `http://localhost:5173`

---

## Deployment (AWS EC2)

- Ubuntu 24.04, t3.micro
- PostgreSQL + PGVector running in Docker
- Spring Boot jar deployed with `nohup`
- Vue 3 built and served via Nginx
- Nginx reverse proxy for `/api` → Spring Boot

```bash
# Backend
nohup java -jar app.jar --spring.profiles.active=prod > app.log 2>&1 &

# Frontend
npx vite build --mode production
# Upload dist/ to EC2, serve via Nginx
```

---

## Comparison with Previous Projects

| Feature | LoveApp (Spring AI) | Ling Clinic |
|---|---|---|
| RAG Retrieval | Vector search only | **Hybrid search (vector + keyword)** |
| Result Merging | — | **RRF algorithm** |
| Metadata Filtering | — | **doc_type + symptom_tag pre-filtering** |
| Chunking | Fixed-size | **Semantic chunking** |
| Streaming | Spring AI SseEmitter | **Custom OkHttp SSE + Fetch API** |
| Deduplication | SHA-256 | SHA-256 |
| Deployment | Docker + EC2 | Nginx + EC2 |

---

## Future Roadmap

- **Multi-role system** — Separate Doctor role with appointment approval workflow
- **Medical records** — Doctor-authored visit notes fed back into RAG knowledge base
- **Patient data isolation** — Row-level security for medical records
- **Re-ranking** — Cross-encoder reranking of top-k retrieved chunks
- **Query rewriting** — LLM-based query expansion before retrieval
