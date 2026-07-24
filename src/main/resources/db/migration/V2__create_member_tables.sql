CREATE TABLE member
(
    id      UUID PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    status  VARCHAR(50)  NOT NULL,
    version BIGINT       NOT NULL
);