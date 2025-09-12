CREATE DATABASE nutrition_db;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    goal VARCHAR(100),
    height INT,
    weight NUMERIC(5,2),
    birth_date DATE,
    created_at DATE DEFAULT CURRENT_DATE,
    restrictions TEXT[],
    activity_level VARCHAR(50),
    dietary_preferences TEXT[],
    chat_history JSONB,
    plan JSONB
);

INSERT INTO users (
    name, email, password, goal, height, weight, birth_date,
    restrictions, activity_level, dietary_preferences, chat_history, plan
) VALUES (
    'Maria Silva',
    'maria.silva@example.com',
    '123456',
    'emagrecer',
    165,
    70.5,
    '1997-09-20',
    ARRAY['lactose', 'glúten'],
    'moderado',
    ARRAY['low-carb', 'vegano'],
    '[
        {"pergunta": "Quero emagrecer rápido", "resposta": "Foque em déficit calórico saudável"}
    ]'::jsonb,
    '{
        "calorias": 1800,
        "refeicoes": [
            {"tipo": "café da manhã", "descricao": "Ovos mexidos com espinafre"}
        ]
    }'::jsonb
);

SELECT id, name, restrictions, dietary_preferences, chat_history, plan
FROM users;

SELECT 
    name,
    DATE_PART('year', AGE(CURRENT_DATE, birth_date)) AS idade
FROM users;

/*
Calcule a idade sempre que precisar:
SELECT 
    name,
    DATE_PART('year', AGE(CURRENT_DATE, birth_date)) AS idade
FROM users;

💡 Dica extra: criar uma view com os dados calculados

Você pode criar uma view chamada user_profile_view que já traz IMC, idade, meses desde cadastro e até se é aniversário

CREATE VIEW user_profile_view AS
SELECT 
    id,
    name,
    height,
    weight,
    weight / POWER(height::numeric / 100, 2) AS imc,
    birth_date,
    DATE_PART('year', AGE(CURRENT_DATE, birth_date)) AS idade,
    created_at,
    DATE_PART('month', AGE(CURRENT_DATE, created_at)) AS meses_desde_cadastro,
    CASE 
        WHEN EXTRACT(MONTH FROM birth_date) = EXTRACT(MONTH FROM CURRENT_DATE)
         AND EXTRACT(DAY FROM birth_date) = EXTRACT(DAY FROM CURRENT_DATE)
        THEN 'Feliz Aniversário!'
        ELSE NULL
    END AS mensagem_aniversario
FROM users;

SELECT * FROM user_profile_view;
*/

/*
💡 Criação da função get_user_notifications
CREATE OR REPLACE FUNCTION get_user_notifications(user_id INT)
RETURNS TEXT AS $$
DECLARE
    msg TEXT := '';
    u RECORD;
    meses_cadastrado INT;
    idade_atual INT;
BEGIN
    SELECT *,
           DATE_PART('month', AGE(CURRENT_DATE, created_at)) AS meses,
           DATE_PART('year', AGE(CURRENT_DATE, birth_date)) AS idade
    INTO u
    FROM users
    WHERE id = user_id;

    IF u IS NULL THEN
        RETURN 'Usuário não encontrado.';
    END IF;

    -- Verifica se é aniversário
    IF EXTRACT(MONTH FROM u.birth_date) = EXTRACT(MONTH FROM CURRENT_DATE)
       AND EXTRACT(DAY FROM u.birth_date) = EXTRACT(DAY FROM CURRENT_DATE) THEN
        msg := msg || '🎂 Feliz aniversário, ' || u.name || '! Você completou ' || u.idade::INT || ' anos hoje. ';
    END IF;

    -- Verifica se completou meses desde o cadastro
    IF MOD(u.meses, 1) = 0 AND u.meses >= 1 THEN
        msg := msg || '🎉 Parabéns! Já se passaram ' || u.meses::INT || ' meses desde seu cadastro. ';
    END IF;

    IF msg = '' THEN
        RETURN 'Nenhuma notificação para hoje.';
    ELSE
        RETURN msg;
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT get_user_notifications(1);


💬 Exemplo de retorno:

Se for o aniversário do usuário e ele tiver 3 meses de cadastro:

🎂 Feliz aniversário, Maria Silva! Você completou 29 anos hoje. 🎉 Parabéns! Já se passaram 3 meses desde seu cadastro.

1. Verificar tempo desde o cadastro
SELECT 
    name,
    CURRENT_DATE - created_at AS dias_desde_cadastro
FROM users;


Ou em meses:

SELECT 
    name,
    DATE_PART('month', AGE(CURRENT_DATE, created_at)) AS meses_desde_cadastro
FROM users;

2. 🎉 Mensagem somente de aniversário
SELECT 
    name,
    CASE 
        WHEN EXTRACT(MONTH FROM birth_date) = EXTRACT(MONTH FROM CURRENT_DATE)
          AND EXTRACT(DAY FROM birth_date) = EXTRACT(DAY FROM CURRENT_DATE)
        THEN 'Feliz aniversário!'
        ELSE NULL
    END AS mensagem_aniversario
FROM users;
*/
