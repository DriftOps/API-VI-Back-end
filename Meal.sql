CREATE TABLE meals (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    meal_date DATE NOT NULL DEFAULT CURRENT_DATE,
    type VARCHAR(50) NOT NULL,             -- Café da manhã, Almoço, Jantar etc.
    description TEXT,                      -- Ex: "Frango + arroz + salada"

    calories DOUBLE PRECISION DEFAULT 0,
    protein DOUBLE PRECISION DEFAULT 0,
    carbs DOUBLE PRECISION DEFAULT 0,
    fat DOUBLE PRECISION DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

select * from meals;