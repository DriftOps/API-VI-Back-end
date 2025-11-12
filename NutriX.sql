-- =======================================================
-- 1. SETUP INICIAL E EXTENSÕES
-- =======================================================

-- Comandos de Ambiente (Manter comentados a menos que você queira usá-los)
-- DROP DATABASE IF EXISTS "NutriX";
-- CREATE DATABASE "NutriX";

-- USE DATABASE NutriX; -- Este comando não é padrão no PostgreSQL. Use \c NutriX na CLI.

-- Extensão para criptografia de senhas (PGCrypto)
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- =======================================================
-- 2. CRIAÇÃO DE TIPOS ENUM (DDL)
-- * Tipos devem ser criados antes das tabelas que os utilizam.
-- =======================================================

-- 2.1 Papéis de Usuário
CREATE TYPE user_role AS ENUM (
    'CLIENT',
    'EMPLOYEE',
    'ADMIN',
    'NUTRITIONIST'
);

-- 2.2 Tipos para a Tabela user_anamnesis

-- Main goal type (Objetivo principal)
CREATE TYPE main_goal_type AS ENUM (
    'WEIGHT_LOSS', 
    'MUSCLE_GAIN', 
    'DIABETES_CONTROL',
    'DIET_REEDUCATION',
    'PHYSICAL_MENTAL_PERFORMANCE'
);

-- Activity type (Tipo de atividade)
CREATE TYPE activity_type_enum AS ENUM (
    'SEDENTARY', 
    'WALKING',
    'WEIGHT_TRAINING',
    'RUNNING',
    'CROSSFIT',
    'SWIMMING',
    'FIGHT',
    'OTHER'
);

-- Frequency type (Frequência)
CREATE TYPE frequency_type AS ENUM (
    'NONE',
    'ONE_2X_WEEK',
    'THREE_4X_WEEK',
    'FIVE_X_OR_MORE'
);

-- Sleep quality type (Qualidade do sono)
CREATE TYPE sleep_quality_type AS ENUM (
    'GOOD',
    'REGULAR',
    'BAD'
);

-- Wakes during night type (Acorda durante a noite)
CREATE TYPE wakes_during_night_type AS ENUM (
    'NO',
    'ONCE',
    'MORE_THAN_ONCE'
);

-- Bowel frequency type (Frequência intestinal)
CREATE TYPE bowel_frequency_type AS ENUM (
    'EVERY_DAY',
    'FIVE_X_WEEK',
    'THREE_X_WEEK',
    'ONE_X_WEEK'
);

-- Stress level type (Nível de estresse)
CREATE TYPE stress_level_type AS ENUM (
    'LOW',
    'MODERATE',
    'HIGH'
);

-- Alcohol use type (Uso de álcool)
CREATE TYPE alcohol_use_type AS ENUM (
    'DOES_NOT_CONSUME',
    'SOCIAL_1_2X_WEEK',
    'FREQUENT_3_4X_WEEK',
    'DAILY_USE'
);

-- Hydration level type (Nível de hidratação)
CREATE TYPE hydration_level_type AS ENUM (
    'LESS_THAN_1L',
    'BETWEEN_1_2L',
    'BETWEEN_2_3L',
    'MORE_THAN_3L'
);

-- Tipo para Feedback do Usuário
CREATE TYPE user_feedback_type AS ENUM ('positive', 'negative');


-- =======================================================
-- 3. CRIAÇÃO DE TABELAS (DDL)
-- * Criar tabelas principais antes das tabelas de ligação (muitos-para-muitos).
-- =======================================================

-- 3.1 Tabela users (Principal)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'CLIENT',
    gender VARCHAR(10),
    birth_date DATE,
    height INT,
    weight NUMERIC(5,2),
    timezone VARCHAR(50),
    language VARCHAR(10) DEFAULT 'pt',
    onboarding_completed BOOLEAN DEFAULT FALSE,
    ai_assistant_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    chat_history JSONB, -- Histórico de chat é melhor em tabela separada, mas ok para metadados aqui
    plan JSONB, -- Plano alimentar é melhor em tabela separada, mas ok para metadados aqui
    approved BOOLEAN DEFAULT FALSE NOT NULL
);

-- 3.2 Tabela user_anamnesis (Anamnese do Usuário)
CREATE TABLE user_anamnesis (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    main_goal main_goal_type NOT NULL,
    medical_conditions TEXT,
    allergies TEXT,
    surgeries TEXT,

    activity_type activity_type_enum,
    frequency frequency_type,
    activity_minutes_per_day INT,

    sleep_quality sleep_quality_type,
    wakes_during_night wakes_during_night_type,

    bowel_frequency bowel_frequency_type,
    stress_level stress_level_type,
    alcohol_use alcohol_use_type,
    smoking BOOLEAN DEFAULT FALSE,
    hydration_level hydration_level_type,
    continuous_medication BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3.3 Tabelas para Preferências e Restrições
CREATE TABLE dietary_preferences (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE dietary_restrictions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE user_preferences (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    preference_id INT REFERENCES dietary_preferences(id),
    PRIMARY KEY (user_id, preference_id)
);

CREATE TABLE user_restrictions (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    restriction_id INT REFERENCES dietary_restrictions(id),
    PRIMARY KEY (user_id, restriction_id)
);

-- 3.4 Tabela para Progresso Físico
CREATE TABLE user_progress (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    weight NUMERIC(5,2),
    body_fat_percent NUMERIC(5,2),
    muscle_mass NUMERIC(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3.5 Tabela para Planos Alimentares
CREATE TABLE meal_plans (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255),
    total_calories INT,
    meals JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

-- 3.6 Tabelas para Sessões de Chat com LLM
CREATE TABLE chat_sessions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    context_summary TEXT
);

CREATE TABLE chat_messages (
    id SERIAL PRIMARY KEY,
    session_id INT REFERENCES chat_sessions(id) ON DELETE CASCADE,
    sender VARCHAR(10) CHECK (sender IN ('user', 'assistant')),
    message TEXT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3.7 Tabela para Logs de Atividades
CREATE TABLE user_activity_logs (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50), 
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- =======================================================
-- 4. ALTERAÇÕES DE TABELAS (DDL)
-- * Ajustes estruturais que dependem das tabelas existirem.
-- =======================================================

-- Adiciona campos de moderação e feedback na tabela de mensagens
ALTER TABLE chat_messages
ADD COLUMN nutritionist_comment TEXT,
ADD COLUMN nutritionist_id INT REFERENCES users(id) ON DELETE SET NULL,
ADD COLUMN comment_timestamp TIMESTAMP,
ADD COLUMN user_feedback user_feedback_type;

-- Comentário da sua anotação (não será executado se o campo main_goal for NOT NULL no DDL):
-- ALTER TABLE user_anamnesis ALTER COLUMN main_goal DROP NOT NULL;


-- =======================================================
-- 5. INSERÇÃO DE DADOS (DML)
-- * População inicial das tabelas.
-- =======================================================

-- 5.1 Inserir Restrições e Preferências (tabelas auxiliares)
INSERT INTO dietary_restrictions (name) VALUES 
    ('lactose'),
    ('gluten')
ON CONFLICT (name) DO NOTHING;

INSERT INTO dietary_preferences (name) VALUES 
    ('low-carb'),
    ('vegano')
ON CONFLICT (name) DO NOTHING;

-- 5.2 Inserir Usuários (IDs 1, 2 e 3)
-- (O ID é crucial para as FOREIGN KEYs seguintes)

-- Usuário 1: CLIENTE (Maria Silva)
INSERT INTO users (
    name, email, password, height, weight, birth_date,
    chat_history, plan, approved
) VALUES (
    'Maria Silva',
    'maria.silva@example.com',
    '123456',
    165,
    70.5,
    '1997-09-20',
    '[
        {"pergunta": "Quero emagrecer rápido", "resposta": "Foque em déficit calórico saudável"}
    ]'::jsonb,
    '{
        "calorias": 1800,
        "refeicoes": [
            {"tipo": "café da manhã", "descricao": "Ovos mexidos com espinafre"}
        ]
    }'::jsonb,
    FALSE -- Cliente começa como FALSE
) RETURNING id;

-- Usuário 2: ADMIN
INSERT INTO users (
    name, email, password, role, height, weight, birth_date, approved
) VALUES (
    'admin',
    'admin@nutrix.com',
    'pass1234',
    'ADMIN',
    184,
    83.5,
    '1999-12-10',
    TRUE -- Admin já entra aprovado
) RETURNING id;

-- Usuário 3: NUTRITIONIST (Nutrix)
INSERT INTO users (
    name, email, password, role, gender, birth_date, height, weight, approved, ai_assistant_enabled
) VALUES (
    'Nutrix',
    'nutrix@nutrix.com',
    '1234',
    'NUTRITIONIST',
    'MASCULINO',
    '1985-05-15',
    178,
    78.5,
    TRUE, -- Nutricionista já entra aprovado
    FALSE
) RETURNING id;


-- 5.3 Inserir Anamnese (para o usuário ADMIN - ID 2)
INSERT INTO user_anamnesis (
    user_id,
    main_goal, medical_conditions, allergies, surgeries,
    activity_type, frequency, activity_minutes_per_day,
    sleep_quality, wakes_during_night,
    bowel_frequency, stress_level, alcohol_use, smoking, hydration_level, continuous_medication
) VALUES (
    2, 
    'WEIGHT_LOSS', 
    'Hypertension; Gastritis',
    'Lactose intolerance', 
    'Cesarean', 
    'WEIGHT_TRAINING', 
    'THREE_4X_WEEK', 
    60,
    'REGULAR', 
    'ONCE',
    'EVERY_DAY', 
    'MODERATE', 
    'SOCIAL_1_2X_WEEK', 
    FALSE, 
    'BETWEEN_2_3L', 
    TRUE
) RETURNING *;


-- 5.4 Ligar Restrições e Preferências (Usando IDs de exemplo 1 e 2 para demonstração)
-- Assumindo que os primeiros dois usuários inseridos foram ID 1 e ID 2.
-- Maria (ID 1)
INSERT INTO user_restrictions (user_id, restriction_id)
SELECT 1, id FROM dietary_restrictions WHERE name IN ('lactose', 'gluten');

INSERT INTO user_preferences (user_id, preference_id)
SELECT 1, id FROM dietary_preferences WHERE name IN ('low-carb', 'vegano');

-- Admin (ID 2)
INSERT INTO user_restrictions (user_id, restriction_id)
SELECT 2, id FROM dietary_restrictions WHERE name IN ('lactose', 'gluten');

INSERT INTO user_preferences (user_id, preference_id)
SELECT 2, id FROM dietary_preferences WHERE name IN ('low-carb', 'vegano');


-- 5.5 Atualizar Senhas (Após a inserção de todos os usuários)
UPDATE users 
SET password = crypt(password, gen_salt('bf'));

-- 5.6 Atualizar Status de Aprovação (Se desejar aprovar todos os usuários existentes)
-- UPDATE users
-- SET approved = TRUE;


-- =======================================================
-- 6. COMANDOS DE VERIFICAÇÃO/MANUTENÇÃO
-- (Manter no final ou comentados)
-- =======================================================

-- Seleções de Verificação
SELECT id, name, email, role, approved FROM users;
SELECT * FROM user_anamnesis;
-- SELECT * FROM meals; -- Você não tem uma tabela 'meals' definida, talvez fosse 'meal_plans'.

-- DELETE from user_anamnesis where id=3;
-- DELETE from users where id=29;

-- =======================================================
-- 7. TABELAS DE DIETA DINÂMICA (NOVO)
-- =======================================================

-- Garante que a tabela de refeições (essencial) exista
-- (Baseado no seu arquivo Meal.sql e Meal.java)
CREATE TABLE IF NOT EXISTS meals (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    calories INT,
    protein NUMERIC(5,2),
    carbs NUMERIC(5,2),
    fats NUMERIC(5,2),
    meal_type VARCHAR(50), -- (e.g., 'breakfast', 'lunch', 'dinner', 'snack')
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Data do registro
);

-- 7.1 Tipo ENUM para o status da dieta
CREATE TYPE diet_status AS ENUM (
    'ACTIVE',     -- Dieta em andamento
    'COMPLETED',  -- Concluída com sucesso
    'CANCELLED'   -- Interrompida
);

-- 7.2 Tabela Principal da Dieta (O "Plano Mestre")
CREATE TABLE diets (
    id SERIAL PRIMARY KEY,
    -- 'UNIQUE' aqui assume uma dieta ativa por vez. 
    -- Remova 'UNIQUE' se o usuário puder ter múltiplas dietas (ex: uma ativa, várias inativas).
    user_id INT UNIQUE REFERENCES users(id) ON DELETE CASCADE, 
    title VARCHAR(255) NOT NULL,
    status diet_status NOT NULL DEFAULT 'ACTIVE',
    
    start_date DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date DATE NOT NULL,
    
    initial_weight NUMERIC(5,2) NOT NULL,
    target_weight NUMERIC(5,2) NOT NULL,
    
    -- Metas BASE (O plano original, calculado na criação)
    base_daily_calories INT NOT NULL,
    base_daily_protein_g INT,
    base_daily_carbs_g INT,
    base_daily_fats_g INT,

    -- Piso metabólico (TMB) para a IA NUNCA sugerir abaixo
    safe_metabolic_floor INT NOT NULL, 

    -- Campo para a IA armazenar o racional da última recalibração
    ai_rationale TEXT, 
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT check_dates CHECK (end_date > start_date),
    CONSTRAINT check_positive_targets CHECK (base_daily_calories > 0 AND safe_metabolic_floor > 0)
);

-- 7.3 Tabela de Metas Diárias (A parte "Ajustável")
-- Esta é a tabela que a IA irá ler e escrever DIARIAMENTE.
CREATE TABLE diet_daily_targets (
    id SERIAL PRIMARY KEY,
    diet_id INT REFERENCES diets(id) ON DELETE CASCADE,
    
    -- O dia específico da meta
    target_date DATE NOT NULL,
    
    -- Metas ajustadas pela IA (começam = base_daily_calories)
    adjusted_calories INT NOT NULL,
    adjusted_protein_g INT,
    adjusted_carbs_g INT,
    adjusted_fats_g INT,
    
    -- O que foi consumido de fato (denormalizado da tabela 'meals' pelo job diário)
    consumed_calories INT DEFAULT 0,
    consumed_protein_g NUMERIC(5,2) DEFAULT 0,
    consumed_carbs_g NUMERIC(5,2) DEFAULT 0,
    consumed_fats_g NUMERIC(5,2) DEFAULT 0,
    
    -- Controle
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(diet_id, target_date) -- Garante apenas uma meta por dia por dieta
);

-- 7.4 (Opcional) Ligar planos de refeição (templates) a dietas
ALTER TABLE meal_plans
ADD COLUMN diet_id INT REFERENCES diets(id) ON DELETE SET NULL;

select * from diets;