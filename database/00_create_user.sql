-- Run ONCE as SYSTEM (SQL Developer or: sqlplus system/<pwd>@localhost:1521/XEPDB1 @database/00_create_user.sql)
-- Creates the schema/user the app connects as. Use FREEPDB1 instead of XEPDB1 on Oracle 23ai Free.
CREATE USER crs IDENTIFIED BY crs;
GRANT CONNECT, RESOURCE TO crs;
ALTER USER crs QUOTA UNLIMITED ON USERS;
