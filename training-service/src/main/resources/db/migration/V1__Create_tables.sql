CREATE TABLE IF NOT EXISTS workout_templates
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id    UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    UNIQUE (owner_id, name)
);

CREATE TABLE IF NOT EXISTS exercise_templates
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exercise_id         UUID    NOT NULL,
    workout_template_id UUID    NOT NULL REFERENCES workout_templates ON DELETE CASCADE,
    order_index         INTEGER NOT NULL CHECK (order_index >= 0),
    UNIQUE (workout_template_id, order_index)
);

CREATE TABLE IF NOT EXISTS set_templates
(
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exercise_template_id UUID          NOT NULL REFERENCES exercise_templates ON DELETE CASCADE,
    order_index          INTEGER       NOT NULL CHECK (order_index >= 0),
    weight               DECIMAL(7, 2) NOT NULL CHECK (weight >= 0),
    weight_unit          VARCHAR(5)    NOT NULL DEFAULT 'KG' CHECK (weight_unit IN ('KG', 'LB')),
    reps                 INTEGER       NOT NULL CHECK (reps > 0),
    planned_rest_time    INTEGER CHECK (planned_rest_time >= 0),
    UNIQUE (exercise_template_id, order_index)
);

CREATE TABLE IF NOT EXISTS workout_logs
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id   UUID         NOT NULL,
    name       VARCHAR(255) NOT NULL,
    start_time TIMESTAMPTZ  NOT NULL,
    end_time   TIMESTAMPTZ,
    CONSTRAINT check_dates CHECK (end_time IS NULL OR end_time >= start_time)
);

CREATE TABLE IF NOT EXISTS exercise_logs
(
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workout_log_id         UUID         NOT NULL REFERENCES workout_logs ON DELETE CASCADE,
    exercise_id            UUID         NOT NULL,
    exercise_name_snapshot VARCHAR(255) NOT NULL,
    order_index            INTEGER      NOT NULL CHECK (order_index >= 0),
    UNIQUE (workout_log_id, order_index)
);

CREATE TABLE IF NOT EXISTS set_logs
(
    id               UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    exercise_log_id  UUID          NOT NULL REFERENCES exercise_logs ON DELETE CASCADE,
    weight           DECIMAL(7, 2) NOT NULL CHECK (weight >= 0),
    weight_unit      VARCHAR(5)    NOT NULL DEFAULT 'KG' CHECK (weight_unit IN ('KG', 'LB')),
    reps             INTEGER       NOT NULL CHECK (reps >= 0),
    order_index      INTEGER       NOT NULL CHECK (order_index >= 0),
    actual_rest_time INTEGER CHECK (actual_rest_time >= 0),
    UNIQUE (exercise_log_id, order_index)
);