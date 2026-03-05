# Database initialization
# @author Ling

-- 启用 PGVector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 1. 用户表（从MySQL迁移过来）
CREATE TABLE IF NOT EXISTS "user" (
                                      id              BIGSERIAL PRIMARY KEY,
                                      user_account    VARCHAR(256)    NOT NULL UNIQUE,
    user_password   VARCHAR(512)    NOT NULL,
    user_name       VARCHAR(256),
    user_avatar     VARCHAR(1024),
    user_profile    VARCHAR(512),
    user_role       VARCHAR(256)    NOT NULL DEFAULT 'user',
    edit_time       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete       SMALLINT        NOT NULL DEFAULT 0
    );

-- 2. 预约表
CREATE TABLE IF NOT EXISTS appointment (
                                           id              BIGSERIAL PRIMARY KEY,
                                           user_id         BIGINT          NOT NULL,
                                           doctor_name     VARCHAR(256)    NOT NULL,
    appointment_date DATE           NOT NULL,
    time_slot       VARCHAR(50)     NOT NULL,  -- 如 "09:00-10:00"
    status          VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    -- PENDING / CONFIRMED / CANCELLED
    note            VARCHAR(512),
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete       SMALLINT        NOT NULL DEFAULT 0
    );

-- 3. 文档表（存上传的原始文档元数据）
CREATE TABLE IF NOT EXISTS document (
                                        id              BIGSERIAL PRIMARY KEY,
                                        title           VARCHAR(256)    NOT NULL,
    doc_type        VARCHAR(50),    -- herb / treatment / diagnosis / general
    file_hash       VARCHAR(64)     UNIQUE, -- SHA-256去重
    upload_user_id  BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_delete       SMALLINT        NOT NULL DEFAULT 0
    );

-- 4. 文档chunk表（RAG核心）
CREATE TABLE IF NOT EXISTS document_chunk (
                                              id              BIGSERIAL PRIMARY KEY,
                                              document_id     BIGINT          NOT NULL,
                                              content         TEXT            NOT NULL,
                                              embedding       vector(1536),   -- OpenAI text-embedding-3-small 维度
-- Metadata字段（用于filtering）
    doc_type        VARCHAR(50),    -- herb / treatment / diagnosis
    symptom_tag     VARCHAR(100),   -- 失眠 / 消化 / 头痛 等
    chunk_index     INT,            -- 第几个chunk
-- 全文检索字段（Hybrid Search用）
    content_tsv     TSVECTOR,       -- PostgreSQL全文索引
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

-- 向量索引（HNSW，检索速度快）
CREATE INDEX ON document_chunk
    USING hnsw (embedding vector_cosine_ops);

-- 全文检索索引
CREATE INDEX ON document_chunk
    USING GIN (content_tsv);

-- 自动维护 content_tsv 字段
CREATE OR REPLACE FUNCTION update_content_tsv()
RETURNS TRIGGER AS $$
BEGIN
    NEW.content_tsv := to_tsvector('english', NEW.content);
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_content_tsv
    BEFORE INSERT OR UPDATE ON document_chunk
                         FOR EACH ROW EXECUTE FUNCTION update_content_tsv();
