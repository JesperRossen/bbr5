-- PIECE

CREATE TABLE piece (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    old_id BIGINT,
    updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by_id BIGINT NOT NULL REFERENCES site_user(id),
    owner_id BIGINT NOT NULL REFERENCES site_user(id),
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(60) NOT NULL,
    piece_year VARCHAR(4),
    category VARCHAR(1) NOT NULL DEFAULT 'T',
    notes TEXT
);

CREATE TABLE piece_alternative_name (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    updated DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by_id BIGINT NOT NULL REFERENCES site_user(id),
    owner_id BIGINT NOT NULL REFERENCES site_user(id),
    piece_id BIGINT NOT NULL REFERENCES piece(id),
    name VARCHAR(100) NOT NULL,
    hidden BIT NOT NULL DEFAULT 0
);