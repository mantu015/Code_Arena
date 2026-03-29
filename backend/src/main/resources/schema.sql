-- Create User table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    points INT DEFAULT 0,
    role VARCHAR(20) DEFAULT 'USER'
);

-- Create Problem table
CREATE TABLE IF NOT EXISTS problems (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    difficulty VARCHAR(50) NOT NULL,
    test_cases TEXT
);

-- Create Daily Challenge table
CREATE TABLE IF NOT EXISTS daily_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    challenge_date DATE NOT NULL UNIQUE,
    problem_id BIGINT NOT NULL,
    bonus_points INT DEFAULT 50
);

CREATE TABLE IF NOT EXISTS submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    code TEXT NOT NULL,
    language VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (problem_id) REFERENCES problems(id)
);
