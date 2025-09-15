/*1. Tipo ENUM para papéis de usuário
Assim você garante consistência e pode expandir futuramente.
*/
CREATE TYPE user_role AS ENUM ('admin', 'employee', 'client', 'nutritionist');

/*2. Tabela users
Contém os dados principais dos usuários.
*/
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL DEFAULT 'client',
    gender VARCHAR(10),
    birth_date DATE,
    height INT,
    weight NUMERIC(5,2),
    goal VARCHAR(100),
    activity_level VARCHAR(50),
    timezone VARCHAR(50),
    language VARCHAR(10) DEFAULT 'pt',
    onboarding_completed BOOLEAN DEFAULT FALSE,
    ai_assistant_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

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
