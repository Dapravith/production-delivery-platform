CREATE ROLE platform_migrator LOGIN PASSWORD 'migrator-local-password';
CREATE ROLE platform_app LOGIN PASSWORD 'app-local-password';

GRANT CONNECT ON DATABASE platform TO platform_migrator, platform_app;
GRANT USAGE, CREATE ON SCHEMA public TO platform_migrator;
GRANT USAGE ON SCHEMA public TO platform_app;

REVOKE CREATE ON SCHEMA public FROM PUBLIC;
