CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    company VARCHAR(255),
    position VARCHAR(50),
    salary DECIMAL(10, 2),
    status VARCHAR(50),
    photo_file_name VARCHAR(255),
    department_id BIGINT
);