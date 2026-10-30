-- Needed so the tutor overlap constraint can mix uuid equality with range overlap.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
    id            uuid PRIMARY KEY,
    sr_code       varchar(30)  NOT NULL UNIQUE,
    name          varchar(120) NOT NULL,
    email         varchar(254) NOT NULL UNIQUE,
    password_hash varchar(100) NOT NULL,
    university    varchar(120) NOT NULL,
    active        boolean      NOT NULL DEFAULT true,
    year_level    integer,
    program       varchar(60),
    hourly_rate   numeric(10, 2) CHECK (hourly_rate >= 0),
    bio           varchar(1000)
);

CREATE TABLE user_roles (
    user_id uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role    varchar(20) NOT NULL CHECK (role IN ('LEARNER', 'TUTOR', 'ADMIN')),
    PRIMARY KEY (user_id, role)
);

CREATE TABLE courses (
    id       uuid PRIMARY KEY,
    code     varchar(30)  NOT NULL UNIQUE,
    name     varchar(120) NOT NULL,
    category varchar(60)  NOT NULL,
    type     varchar(30)  NOT NULL
);

CREATE TABLE tutor_courses (
    tutor_id  uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    course_id uuid NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    PRIMARY KEY (tutor_id, course_id)
);

CREATE TABLE availabilities (
    id          uuid PRIMARY KEY,
    tutor_id    uuid        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    day_of_week varchar(10) NOT NULL,
    start_time  time        NOT NULL,
    end_time    time        NOT NULL,
    CHECK (end_time > start_time)
);

CREATE TABLE tutoring_classes (
    id             uuid PRIMARY KEY,
    tutor_id       uuid           NOT NULL REFERENCES users (id),
    course_id      uuid           NOT NULL REFERENCES courses (id),
    max_capacity   integer        NOT NULL CHECK (max_capacity > 0),
    rate           numeric(10, 2) NOT NULL CHECK (rate >= 0),
    platform       varchar(60)    NOT NULL,
    classroom_link varchar(500),
    status         varchar(20)    NOT NULL CHECK (status IN ('OPEN', 'ONGOING', 'COMPLETED', 'REMOVED'))
);

CREATE TABLE class_enrollments (
    class_id   uuid NOT NULL REFERENCES tutoring_classes (id) ON DELETE CASCADE,
    learner_id uuid NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (class_id, learner_id)
);

CREATE TABLE tutoring_sessions (
    id           uuid PRIMARY KEY,
    tutor_id     uuid           NOT NULL REFERENCES users (id),
    course_id    uuid           NOT NULL REFERENCES courses (id),
    class_id     uuid REFERENCES tutoring_classes (id),
    session_date date           NOT NULL,
    start_time   time           NOT NULL,
    end_time     time           NOT NULL,
    rate         numeric(10, 2) NOT NULL CHECK (rate >= 0),
    status       varchar(20)    NOT NULL CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),
    meeting_link varchar(500),
    CHECK (end_time > start_time),
    -- A tutor can never be double-booked, even if two requests race each other.
    CONSTRAINT no_tutor_overlap EXCLUDE USING gist (
        tutor_id WITH =,
        tsrange(session_date + start_time, session_date + end_time) WITH &&
    ) WHERE (status <> 'CANCELLED')
);

CREATE TABLE requests (
    id           uuid PRIMARY KEY,
    learner_id   uuid        NOT NULL REFERENCES users (id),
    tutor_id     uuid        NOT NULL REFERENCES users (id),
    course_id    uuid        NOT NULL REFERENCES courses (id),
    session_id   uuid REFERENCES tutoring_sessions (id),
    session_date date        NOT NULL,
    start_time   time        NOT NULL,
    end_time     time        NOT NULL,
    message      varchar(1000),
    status       varchar(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED')),
    CHECK (end_time > start_time),
    CHECK (learner_id <> tutor_id)
);

-- Being invited to a session and actually showing up are separate facts.
CREATE TABLE session_attendees (
    id         uuid PRIMARY KEY,
    session_id uuid    NOT NULL REFERENCES tutoring_sessions (id) ON DELETE CASCADE,
    learner_id uuid    NOT NULL REFERENCES users (id),
    attended   boolean NOT NULL DEFAULT false,
    UNIQUE (session_id, learner_id)
);

CREATE TABLE reviews (
    id             uuid PRIMARY KEY,
    session_id     uuid    NOT NULL REFERENCES tutoring_sessions (id) ON DELETE CASCADE,
    learner_id     uuid    NOT NULL REFERENCES users (id),
    rating         integer NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment        varchar(1000),
    date_submitted date    NOT NULL,
    UNIQUE (session_id, learner_id)
);

-- Simulated for the demo: no real money moves, but the record is real.
CREATE TABLE payments (
    id         uuid PRIMARY KEY,
    session_id uuid           NOT NULL REFERENCES tutoring_sessions (id),
    learner_id uuid           NOT NULL REFERENCES users (id),
    amount     numeric(10, 2) NOT NULL CHECK (amount >= 0),
    method     varchar(20)    NOT NULL CHECK (method IN ('GCASH', 'CARD', 'CASH')),
    reference  varchar(30)    NOT NULL UNIQUE,
    paid_at    timestamptz    NOT NULL,
    UNIQUE (session_id, learner_id)
);

CREATE INDEX idx_availabilities_tutor ON availabilities (tutor_id);
CREATE INDEX idx_requests_learner ON requests (learner_id);
CREATE INDEX idx_requests_tutor ON requests (tutor_id);
CREATE INDEX idx_sessions_tutor ON tutoring_sessions (tutor_id);
CREATE INDEX idx_sessions_class ON tutoring_sessions (class_id);
CREATE INDEX idx_attendees_learner ON session_attendees (learner_id);
