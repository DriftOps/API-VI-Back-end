-- Criar tabela principal de usuários
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    goal VARCHAR(100),
    height NUMERIC(5,2), -- altura em cm (ex: 175.50)
    weight NUMERIC(5,2), -- peso em kg (ex: 72.30)
    age INT,
    activity_level VARCHAR(50),
    chat_history JSONB,  -- histórico salvo em JSON
    plan JSONB           -- plano alimentar em JSON
);

-- Criar tabela para restrições alimentares
CREATE TABLE user_restrictions (
    user_id INT NOT NULL,
    restriction VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criar tabela para preferências alimentares
CREATE TABLE user_dietary_preferences (
    user_id INT NOT NULL,
    preference VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
