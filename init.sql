CREATE DATABASE IF NOT EXISTS blackjack_reactive_mysql;

USE blackjack_reactive_mysql;

CREATE TABLE IF NOT EXISTS players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    total_score DOUBLE DEFAULT 0.0,
    games_played INT DEFAULT 0,
    games_won INT DEFAULT 0
);