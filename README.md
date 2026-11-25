# HydroFlow (Java Swing + MySQL)

## Setup (one time)
1. **MySQL**: open `sql/schema.sql` in MySQL Workbench and run it. It creates the database `hydroflow_db`
   with the tables and the starting data (admin account, settings, 7 customers).
   WARNING: schema.sql deletes and recreates `hydroflow_db`, so do not run it again once you have real data.
2. **Eclipse**: *File > Import > General > Existing Projects into Workspace* > choose this folder.
3. **Jars**: add MySQL Connector/J and JFreeChart (see `lib/README.txt`).
4. **Connection**: open `src/hydroflow/db/DBConnection.java` and set USER / PASSWORD / URL
   (default: user `root`, own MySQL password, `localhost:3306`). Or set the environment variables
   `HYDROFLOW_DB_URL`, `HYDROFLOW_DB_USER`, `HYDROFLOW_DB_PASS` so the shared code is never edited.
5. Run `src/hydroflow/HydroFlowApp.java`.  Login: **admin / admin123**

## How to see tables in MySQL Command Line
1. enter your MySQL password
2. enter `USE hydroflow_db;`
3. enter `SHOW TABLES;`
3. select which tables you'd like to see a specific data from
4. enter `SELECT * FROM (table data name);`      -- ex: **SELECT * FROM customers;**

## Folder layout
```
sql/schema.sql,                   database
src/hydroflow/db/                 DBConnection (settings), DatabaseException,
src/hydroflow/dao/                one class per table: UserDAO, CustomerDAO, SettingsDAO, OrderDAO, LedgerDAO
src/hydroflow/data/DataStore.java what the windows call; it calls the DAOs
src/hydroflow/model/              Customer, Order, LedgerEntry, Settings, Billing (all formulas)
src/hydroflow/ui/                 the windows (they never contain SQL)
```
Flow: window -> `DataStore` -> DAO -> MySQL.

## Tables
`users`, `customers`, `settings` (key/value), `orders`, `empties_ledger` (one row per delivered order).
MARK AS DELIVERED updates the order and inserts the ledger row in ONE transaction (`LedgerDAO.addDeliveredEntry`).

## Notes
- Database errors (MySQL off, wrong password, missing jar) show up as an error dialog with hints.
- Change URL, User, and Password in `DBConnection.java` to match your MySQL credentials.
- Passwords are stored as SHA-256 hashes.
