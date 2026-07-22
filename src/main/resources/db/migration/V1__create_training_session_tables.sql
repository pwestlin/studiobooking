CREATE TABLE training_session
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    capacity   INT          NOT NULL,
    start_time TIMESTAMPTZ  NOT NULL,
    end_time   TIMESTAMPTZ  NOT NULL,
    status     VARCHAR(50)  NOT NULL,
    version    BIGINT
);

CREATE TABLE booking
(
    session_id UUID        NOT NULL REFERENCES training_session (id) ON DELETE CASCADE,
    member_id  UUID        NOT NULL,
    booked_at  TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (session_id, member_id)
);

CREATE INDEX idx_booking_session_id ON booking (session_id);