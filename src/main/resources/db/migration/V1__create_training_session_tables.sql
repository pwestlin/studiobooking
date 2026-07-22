CREATE TABLE training_session
(
    id         VARCHAR(36) PRIMARY KEY,
    title      VARCHAR(255)             NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity   INT                      NOT NULL
);

CREATE TABLE training_session_booking
(
    id                  VARCHAR(36) PRIMARY KEY,
    training_session_id VARCHAR(36)              NOT NULL,
    member_id           VARCHAR(36)              NOT NULL,
    booked_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    type                VARCHAR(20)              NOT NULL, -- 'REGULAR' eller 'WAITLIST'
    waitlist_position   INT,                               -- NULL för vanliga bokningar, 1, 2, 3... för väntelista
    CONSTRAINT fk_session FOREIGN KEY (training_session_id) REFERENCES training_session (id) ON DELETE CASCADE
);

CREATE INDEX idx_booking_session ON training_session_booking (training_session_id);