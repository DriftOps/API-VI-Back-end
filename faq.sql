/*8. Perguntas e Respostas (FAQ) 
*/
CREATE TABLE faqs (
  id SERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  question TEXT NOT NULL,
  answer   TEXT NOT NULL,
  tags     TEXT, -- ex: "Alimentação;Horario"
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Esse trecho cria uma coluna calculada (tsv) que serve para buscas de texto completo (full-text search) no PostgreSQL, 
-- e em seguida cria um índice GIN para que as buscas fiquem rápidas.
ALTER TABLE faqs
  ADD COLUMN IF NOT EXISTS tsv tsvector
  GENERATED ALWAYS AS (
    to_tsvector('portuguese',
      coalesce(question,'') || ' ' || coalesce(answer,'') || ' ' || coalesce(tags,'')
    )
  ) STORED;

CREATE INDEX IF NOT EXISTS faqs_tsv_idx ON faqs USING GIN (tsv);