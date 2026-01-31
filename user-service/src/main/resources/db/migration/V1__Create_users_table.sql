CREATE TABLE IF NOT EXISTS user_profile
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    display_name   VARCHAR(255) NOT NULL UNIQUE,
    gender         VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')),
    birthdate      DATE,
    preferred_unit VARCHAR(5)   NOT NULL DEFAULT 'KG' CHECK (preferred_unit IN ('KG', 'LB'))
);