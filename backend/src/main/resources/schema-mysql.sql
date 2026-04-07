CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    department VARCHAR(255),
    role VARCHAR(255) NOT NULL,
    active BIT(1) NOT NULL,
    reset_token VARCHAR(255),
    reset_token_expiry DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

SET @has_pk := (
    SELECT COUNT(*)
    FROM information_schema.table_constraints
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND constraint_type = 'PRIMARY KEY'
);
SET @sql := IF(@has_pk = 0, 'ALTER TABLE users ADD PRIMARY KEY (id)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @id_is_auto := (
    SELECT CASE WHEN EXTRA LIKE '%auto_increment%' THEN 1 ELSE 0 END
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'id'
);
SET @sql_auto := IF(IFNULL(@id_is_auto, 0) = 0,
    'ALTER TABLE users MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT',
    'SELECT 1');
PREPARE stmt_auto FROM @sql_auto;
EXECUTE stmt_auto;
DEALLOCATE PREPARE stmt_auto;
