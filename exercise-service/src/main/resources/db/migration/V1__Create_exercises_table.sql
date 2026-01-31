CREATE TABLE IF NOT EXISTS muscle_groups
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS exercises
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    muscle_group_id UUID         NOT NULL REFERENCES muscle_groups ON DELETE RESTRICT,
    owner_id        UUID,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    CONSTRAINT unique_owner_exercise_name UNIQUE NULLS NOT DISTINCT (owner_id, name)
);