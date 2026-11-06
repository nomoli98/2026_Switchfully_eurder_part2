CREATE TABLE users
(
    user_id      UUID PRIMARY KEY,
    firstname    VARCHAR(255) NOT NULL,
    lastname     VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    phone_number VARCHAR(12)  NOT NULL UNIQUE,
    -- embedded Address
    street       VARCHAR(255) NOT NULL,
    house_number VARCHAR(255) NOT NULL,
    city         VARCHAR(255) NOT NULL,
    postal_code  VARCHAR(255) NOT NULL,
    country      VARCHAR(255) NOT NULL,
    role         VARCHAR(255) NOT NULL
);

CREATE TABLE items
(
    item_id     UUID PRIMARY KEY,
    name        VARCHAR(255)     NOT NULL,
    description VARCHAR(255)     NOT NULL,
    price       DOUBLE PRECISION NOT NULL CHECK (price >= 0),
    stock       INTEGER          NOT NULL
);

CREATE TABLE eurders
(
    eurder_id  UUID PRIMARY KEY,
    fk_user_id UUID NOT NULL REFERENCES users (user_id)
);

CREATE TABLE item_groups
(
    item_group_id  UUID PRIMARY KEY,
    fk_item_id     UUID             NOT NULL REFERENCES items (item_id),
    item_amount    INTEGER          NOT NULL CHECK (item_amount > 0),
    price_at_order DOUBLE PRECISION NOT NULL CHECK (price_at_order >= 0),
    shipping_date  DATE             NOT NULL,
    fk_eurder_id   UUID             NOT NULL REFERENCES eurders (eurder_id) ON DELETE CASCADE
);