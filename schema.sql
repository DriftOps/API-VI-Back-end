CREATE DATABASE nutrition_db;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    goal VARCHAR(100),
    height NUMERIC(5,2),
    weight NUMERIC(5,2),
    age INT,
    restrictions TEXT[],            -- array de strings
    activity_level VARCHAR(50),
    dietary_preferences TEXT[],     -- array de strings
    chat_history JSONB,             -- histórico em JSON
    plan JSONB                      -- plano em JSON
);

INSERT INTO users (
    name, email, password, goal, height, weight, age, restrictions,
    activity_level, dietary_preferences, chat_history, plan
) VALUES (
    'Maria Silva',
    'maria.silva@example.com',
    '123456',
    'emagrecer',
    165.0,
    70.5,
    28,
    ARRAY['lactose', 'glúten'],              -- restrições
    'moderado',
    ARRAY['low-carb', 'vegano'],             -- preferências
    '[
        {"pergunta": "Quero emagrecer rápido", "resposta": "Foque em déficit calórico saudável"},
        {"pergunta": "Posso comer pão?", "resposta": "Prefira integrais e com moderação"}
    ]'::jsonb,
    '{
        "calorias": 1800,
        "refeicoes": [
            {"tipo": "café da manhã", "descricao": "Ovos mexidos com espinafre"},
            {"tipo": "almoço", "descricao": "Peito de frango grelhado com salada"},
            {"tipo": "jantar", "descricao": "Sopa de legumes"}
        ]
    }'::jsonb
);

SELECT id, name, restrictions, dietary_preferences, chat_history, plan
FROM users;

