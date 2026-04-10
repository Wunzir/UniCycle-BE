CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    university VARCHAR(100) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE
);

-- Join table for Many-to-Many relationship
CREATE TABLE user_roles (
                            user_id INT REFERENCES users(id),
                            role_id INT REFERENCES roles(id),
                            PRIMARY KEY (user_id, role_id)
);

-- Seed initial roles
INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_MANAGER'), ('ROLE_ADMIN');