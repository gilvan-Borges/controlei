# TASK-08: Leitura Inteligente de Comprovantes e Cupons Fiscais (OCR + IA)

## 🎯 Objetivo
Permitir o upload de fotos de notas fiscais, cupons de supermercado e comprovantes de transferência/PIX, utilizando OCR e IA para extrair automaticamente valor, data, descrição, estabelecimento e sugerir a categoria correta para a transação.

---

## 📋 Requisitos Técnicos

### 1. Migrations de Banco de Dados (`attachments` e `ocr_scans`)
```sql
CREATE TABLE attachments (
    id UUID PRIMARY KEY,
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(255)
);

CREATE TABLE receipt_scans (
    id UUID PRIMARY KEY,
    attachment_id UUID NOT NULL REFERENCES attachments(id),
    family_id UUID NOT NULL REFERENCES families(id),
    user_id UUID NOT NULL REFERENCES users(id),
    raw_text TEXT,
    extracted_amount DECIMAL(19, 4),
    extracted_date DATE,
    extracted_merchant VARCHAR(255),
    suggested_category_id UUID REFERENCES categories(id),
    status VARCHAR(50) NOT NULL, -- PROCESSING, COMPLETED, FAILED
    confidence_score DECIMAL(5, 2),
    created_at TIMESTAMP NOT NULL
);
```

### 2. Integração com OCR e LLM
- Suporte a provedores de OCR (ex: Google Cloud Vision, Tesseract ou OpenAI/Claude/Gemini multimodal).
- Pipeline de extração:
  1. Frontend envia imagem/PDF (`POST /api/v1/receipts/scan`).
  2. Backend salva o anexo e dispara o processamento assíncrono.
  3. Prompt estruturado para IA retornando JSON com `{ valor, data, estabelecimento, itens, categoria_sugerida }`.
  4. Retorna payload pronto para pré-preenchimento da tela de transação no frontend.

---

## 🧪 Critérios de Aceite e Testes
1. Validação de formato e tamanho de arquivo (máx. 10MB, apenas JPEG, PNG e PDF).
2. Isolamento de arquivos por família no storage.
3. Se o OCR falhar, retornar fallback amigável permitindo preenchimento manual com a imagem ao lado.
