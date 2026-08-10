-- Admins Table
CREATE TABLE IF NOT EXISTS admins (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    address TEXT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

-- Customers Table
CREATE TABLE IF NOT EXISTS customers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    address TEXT,
    age INT NOT NULL,
    gender VARCHAR(20) NOT NULL
);

-- Accounts Table
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    customer_id VARCHAR(50) REFERENCES customers(id) ON DELETE CASCADE,
    pin VARCHAR(4) NOT NULL,
    balance DECIMAL(15,2) NOT NULL,
    type VARCHAR(15) NOT NULL,
    date_created TIMESTAMP NOT NULL,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_attempts INT NOT NULL DEFAULT 0
);

-- Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(20) REFERENCES accounts(account_number) ON DELETE CASCADE,
    date_time TIMESTAMP NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL
);
