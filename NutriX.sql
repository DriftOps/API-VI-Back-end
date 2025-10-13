/*1. Tipo ENUM para papéis de usuário
Assim você garante consistência e pode expandir futuramente.
*/
-- GRANT CONNECT ON DATABASE postgres TO spring;
-- GRANT USAGE ON SCHEMA public TO spring;

-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO admin;
-- GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO admin;

ALTER TABLE users
ADD COLUMN approved BOOLEAN DEFAULT FALSE NOT NULL;

CREATE TYPE user_role AS ENUM (
    'CLIENT',
    'EMPLOYEE',
    'ADMIN',
    'NUTRITIONIST'
);

-- 1. Criar o tipo ENUM para goal
CREATE TYPE goal_type AS ENUM (
    'LOSE_WEIGHT',
    'LOSE_FAT',
    'GAIN_WEIGHT',
    'BUILD_MUSCLE',
    'IMPROVE_ENDURANCE',
    'IMPROVE_STRENGTH',
    'MAINTAIN_WEIGHT' 
);

-- drop DATABASE NutriX;

-- CREATE DATABASE NutriX;

-- USE DATABASE NutriX;

-- 2. Criar o tipo ENUM para activity_level
CREATE TYPE activity_level_type AS ENUM (
    'SEDENTARY',     -- Quase sem exercício
    'LIGHT',         -- Exercício leve diário
    'MODERATE',      -- Exercício moderado médio
    'ACTIVE',        -- Exercício frequente intenso
    'VERY_ACTIVE' 
);

-- 3. Criar a tabela users
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
    goal goal_type,
    activity_level activity_level_type,
    timezone VARCHAR(50),
    language VARCHAR(10) DEFAULT 'pt',
    onboarding_completed BOOLEAN DEFAULT FALSE,
    ai_assistant_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    chat_history JSONB,
    plan JSONB,
    approved BOOLEAN DEFAULT FALSE NOT NULL
);

UPDATE users
SET approved = true
WHERE id IN (2);

/*3. Preferências e restrições alimentares
Separadas em tabelas auxiliares para normalização e reutilização.
*/
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

/*4. Progresso físico
Permite armazenar histórico (peso, gordura, massa muscular).
*/

CREATE TABLE user_progress (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    weight NUMERIC(5,2),
    body_fat_percent NUMERIC(5,2),
    muscle_mass NUMERIC(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/*5. Planos alimentares
Cada usuário pode ter vários planos, com refeições em formato flexível (JSONB).
*/
CREATE TABLE meal_plans (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255),
    total_calories INT,
    meals JSONB,  -- exemplo: {"café da manhã": "...", "almoço": "..."}
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN DEFAULT TRUE
);

/*6. Sessões de chat com LLM
Separar sessões facilita análise e rastreamento.
*/
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

/*7. Logs de atividades
Para rastrear ações importantes do usuário.
*/
CREATE TABLE user_activity_logs (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    activity_type VARCHAR(50), -- ex: 'login', 'update_profile', 'generated_plan'
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Primeiro insere o usuário
INSERT INTO users (
    name, email, password, goal, height, weight, birth_date,
    activity_level, chat_history, plan
) VALUES (
    'Maria Silva',
    'maria.silva@example.com',
    '123456',
    'LOSE_WEIGHT',
    165,
    70.5,
    '1997-09-20',
    'MODERATE',
    '[
        {"pergunta": "Quero emagrecer rápido", "resposta": "Foque em déficit calórico saudável"}
    ]'::jsonb,
    '{
        "calorias": 1800,
        "refeicoes": [
            {"tipo": "café da manhã", "descricao": "Ovos mexidos com espinafre"}
        ]
    }'::jsonb
)
RETURNING id;


-- Inserir restrições (se ainda não existirem)
INSERT INTO dietary_restrictions (name) VALUES ('lactose')
    ON CONFLICT (name) DO NOTHING;
INSERT INTO dietary_restrictions (name) VALUES ('gluten')
    ON CONFLICT (name) DO NOTHING;

-- Vincular restrições ao usuário
INSERT INTO user_restrictions (user_id, restriction_id)
SELECT 1, id FROM dietary_restrictions WHERE name IN ('lactose', 'gluten');

-- Inserir preferências (se ainda não existirem)
INSERT INTO dietary_preferences (name) VALUES ('low-carb')
    ON CONFLICT (name) DO NOTHING;
INSERT INTO dietary_preferences (name) VALUES ('vegano')
    ON CONFLICT (name) DO NOTHING;

-- Vincular preferências ao usuário
INSERT INTO user_preferences (user_id, preference_id)
SELECT 1, id FROM dietary_preferences WHERE name IN ('low-carb', 'vegano');

-- ADMIN USER
INSERT INTO users (
    name, email, password, role, goal, height, weight, birth_date,
    activity_level
) VALUES (
    'admin',
    'admin@nutrix.com',
    'pass1234',
    'ADMIN',
    'IMPROVE_STRENGTH',
    184,
    83.5,
    '1999-12-10',
    'ACTIVE'
)
RETURNING id;


-- Inserir restrições (se ainda não existirem)
INSERT INTO dietary_restrictions (name) VALUES ('lactose')
    ON CONFLICT (name) DO NOTHING;
INSERT INTO dietary_restrictions (name) VALUES ('gluten')
    ON CONFLICT (name) DO NOTHING;

-- Vincular restrições ao usuário
INSERT INTO user_restrictions (user_id, restriction_id)
SELECT 2, id FROM dietary_restrictions WHERE name IN ('lactose', 'gluten');

-- Inserir preferências (se ainda não existirem)
INSERT INTO dietary_preferences (name) VALUES ('low-carb')
    ON CONFLICT (name) DO NOTHING;
INSERT INTO dietary_preferences (name) VALUES ('vegano')
    ON CONFLICT (name) DO NOTHING;

-- Vincular preferências ao usuário
INSERT INTO user_preferences (user_id, preference_id)
SELECT 2, id FROM dietary_preferences WHERE name IN ('low-carb', 'vegano');

-- Tipos ENUM para as respostas

-- Main goal type (Objetivo principal)
CREATE TYPE main_goal_type AS ENUM (
    'WEIGHT_LOSS',              -- EMAGRECIMENTO
    'MUSCLE_GAIN',              -- GANHO_MASSA_MUSCULAR
    'DIABETES_CONTROL',         -- CONTROLE_DIABETES
    'DIET_REEDUCATION',         -- REEDUCACAO_ALIMENTAR
    'PHYSICAL_MENTAL_PERFORMANCE' -- PERFORMANCE_FISICA_MENTAL
);

-- Activity type (Tipo de atividade)
CREATE TYPE activity_type_enum AS ENUM (
    'SEDENTARY',    -- SEDENTARIO
    'WALKING',      -- CAMINHADA
    'WEIGHT_TRAINING', -- MUSCULACAO
    'RUNNING',      -- CORRIDA
    'CROSSFIT',     -- CROSSFIT
    'SWIMMING',     -- NATACAO
    'FIGHT',        -- LUTA
    'OTHER'         -- OUTRO
);

-- Frequency type (Frequência)
CREATE TYPE frequency_type AS ENUM (
    'NONE',          -- NENHUMA
    'ONE_2X_WEEK',   -- 1_2X_SEMANA
    'THREE_4X_WEEK', -- 3_4X_SEMANA
    'FIVE_X_OR_MORE' -- 5X_OU_MAIS
);

-- Sleep quality type (Qualidade do sono)
CREATE TYPE sleep_quality_type AS ENUM (
    'GOOD',         -- BOA
    'REGULAR',      -- REGULAR
    'BAD'           -- RUIM
);

-- Wakes during night type (Acorda durante a noite)
CREATE TYPE wakes_during_night_type AS ENUM (
    'NO',           -- NAO
    'ONCE',         -- 1X
    'MORE_THAN_ONCE'-- MAIS_DE_1X
);

-- Bowel frequency type (Frequência intestinal)
CREATE TYPE bowel_frequency_type AS ENUM (
    'EVERY_DAY',        -- TODO_DIA
    'FIVE_X_WEEK',      -- 5X_SEMANA
    'THREE_X_WEEK',     -- 3X_SEMANA
    'ONE_X_WEEK'        -- 1X_SEMANA
);

-- Stress level type (Nível de estresse)
CREATE TYPE stress_level_type AS ENUM (
    'LOW',          -- BAIXO
    'MODERATE',     -- MODERADO
    'HIGH'          -- ALTO
);

-- Alcohol use type (Uso de álcool)
CREATE TYPE alcohol_use_type AS ENUM (
    'DOES_NOT_CONSUME',      -- NAO_CONSOME
    'SOCIAL_1_2X_WEEK',      -- SOCIAL_1_2X_SEMANA
    'FREQUENT_3_4X_WEEK',    -- FREQUENTE_3_4X_SEMANA
    'DAILY_USE'              -- USO_DIARIO
);

-- Hydration level type (Nível de hidratação)
CREATE TYPE hydration_level_type AS ENUM (
    'LESS_THAN_1L',          -- MENOS_1L
    'BETWEEN_1_2L',          -- ENTRE_1_2L
    'BETWEEN_2_3L',          -- ENTRE_2_3L
    'MORE_THAN_3L'           -- MAIS_3L
);

-- Tabela principal
CREATE TABLE user_anamnesis (
    id SERIAL PRIMARY KEY,
    user_id INT UNIQUE REFERENCES users(id) ON DELETE CASCADE,

    main_goal main_goal_type NOT NULL,            -- Objetivo principal
    medical_conditions TEXT,                      -- Condições médicas
    allergies TEXT,                               -- Alergias
    surgeries TEXT,                               -- Cirurgias

    activity_type activity_type_enum,             -- Tipo de atividade
    frequency frequency_type,                     -- Frequência semanal
    activity_minutes_per_day INT,                 -- Minutos de atividade por dia

    sleep_quality sleep_quality_type,             -- Qualidade do sono
    wakes_during_night wakes_during_night_type,   -- Acorda durante a noite

    bowel_frequency bowel_frequency_type,         -- Frequência intestinal
    stress_level stress_level_type,               -- Nível de estresse
    alcohol_use alcohol_use_type,                 -- Consumo de álcool
    smoking BOOLEAN DEFAULT FALSE,                -- Fumante
    hydration_level hydration_level_type,         -- Nível de hidratação
    continuous_medication BOOLEAN DEFAULT FALSE,  -- Uso de medicação contínua

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO user_anamnesis (
    user_id,
    main_goal,
    medical_conditions,
    allergies,
    surgeries,
    activity_type,
    frequency,
    activity_minutes_per_day,
    sleep_quality,
    wakes_during_night,
    bowel_frequency,
    stress_level,
    alcohol_use,
    smoking,
    hydration_level,
    continuous_medication
) VALUES (
    2,  -- id do usuário na tabela users
    'WEIGHT_LOSS',            -- Emagrecimento
    'Hypertension; Gastritis',-- Condições médicas
    'Lactose intolerance',    -- Alergias
    'Cesarean',               -- Cirurgias
    'WEIGHT_TRAINING',        -- Tipo de atividade
    '3_4X_WEEK',              -- Frequência semanal
    60,                       -- Minutos de atividade por dia
    'REGULAR',                -- Qualidade do sono
    'ONCE',                   -- Acorda durante a noite
    'EVERY_DAY',              -- Frequência intestinal
    'MODERATE',               -- Nível de estresse
    'SOCIAL_1_2X_WEEK',       -- Consumo de álcool
    FALSE,                    -- Fumante
    'BETWEEN_2_3L',           -- Nível de hidratação
    TRUE                      -- Uso de medicação contínua
)
RETURNING *;