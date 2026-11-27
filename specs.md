# Feature Specification: HydroFlow Water Refilling Station Sales and Billing System

**Feature Branch**: `001-hydroflow-sales-billing`

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Authenticate and Use the Dashboard (Priority: P1)

An authorized station employee signs in and opens the dashboard, where the system provides access to customer, order, sales, report, settings, and empty-container ledger operations.

**Why this priority**: Authentication and dashboard access are the entry point for every station workflow.

**Independent Test**: Start the application with a running database, sign in with `admin / admin123`, and verify that the dashboard opens with all navigation actions available.

**Acceptance Scenarios**:

1. **Given** the database is available and the administrator account exists, **When** the user submits valid credentials, **Then** the dashboard opens.
2. **Given** the user submits invalid credentials, **When** login is attempted, **Then** the system displays an error and remains on the login screen.
3. **Given** the database is unavailable or credentials are misconfigured, **When** login is attempted, **Then** the system displays a database/configuration error without exposing passwords.

---

### User Story 2 - Maintain Customers (Priority: P1)

An employee creates and reviews customer records containing a name, address, and contact number so deliveries can be associated with a customer account.

**Why this priority**: Customers are required by order and ledger workflows.

**Independent Test**: Add a customer with unique name/address/contact values and verify it appears in customer and order-selection data.

**Acceptance Scenarios**:

1. **Given** a user opens Add Customer, **When** valid name, address, and contact fields are submitted, **Then** a customer record is persisted and appears in the customer list.
2. **Given** required customer fields are missing, **When** save is attempted, **Then** the system prevents persistence and presents a validation message.
3. **Given** a customer record is selected, **When** the employee opens an order, **Then** the customer's details are available for order entry.

---

### User Story 3 - Create, Review, and Cancel Orders (Priority: P1)

An employee creates an order for a customer, choosing delivery or walk-in service, water type, container type and quantity, pricing, fees, discount, deposit, and payment details.

**Why this priority**: Order creation is the primary sales transaction.

**Independent Test**: Create a pending delivery order, verify the computed amount due and persisted fields, mark it delivered, and verify the ledger receives one matching entry.

**Acceptance Scenarios**:

1. **Given** a selected customer and valid order values, **When** the employee submits a new order, **Then** the system persists an order with Pending status and Unpaid payment status.
2. **Given** a valid order, **When** marking it delivered, **Then** the system updates the order and inserts one ledger row in the same database transaction.
3. **Given** a delivered order, **When** cancellation is attempted, **Then** the system treats cancellation consistently across order and ledger state and prevents duplicate ledger entries.
4. **Given** invalid quantities, prices, discounts, or payment amounts, **When** save is attempted, **Then** the system rejects the transaction and reports the invalid field.

---

### User Story 4 - Track Sales and Payment Records (Priority: P2)

An employee reviews historical sales, filters or locates orders, and records payment outcomes so the station knows amounts paid, unpaid balances, and change.

**Why this priority**: Sales review is essential for billing, collection, and daily operations.

**Independent Test**: Create orders with unpaid and paid states, open Sales Records, and verify each record shows the correct amount due, amount paid, and change.

**Acceptance Scenarios**:

1. **Given** paid and unpaid orders exist, **When** Sales Records is opened, **Then** the system lists order and payment fields for each record.
2. **Given** an order is paid or partially managed in the UI, **When** payment is saved, **Then** the persisted amount paid, change amount, and payment status agree with the final amount due.
3. **Given** no matching records exist, **When** sales records are queried, **Then** the system displays an empty state rather than stale data.

---

### User Story 5 - Manage Empty Containers (Priority: P2)

The station records containers received on delivered orders and when customers return empties, maintaining an empties ledger tied to customer and order data.

**Why this priority**: Container deposits and returns affect refunds and inventory accountability.

**Independent Test**: Mark an order delivered and verify a ledger entry; return empties and verify the ledger updates without creating duplicate rows.

**Acceptance Scenarios**:

1. **Given** a delivered order, **When** delivery is completed, **Then** exactly one ledger row is created for that order.
2. **Given** an existing ledger row, **When** empties are returned, **Then** the system records ledger date, customer, water/container type, quantity, empties-in count, and deposit information.
3. **Given** an order already has a ledger entry, **When** delivery processing is repeated, **Then** no duplicate ledger row is inserted.

---

### User Story 6 - Configure Station Settings and View Reports (Priority: P3)

An administrator updates key/value station settings and reviews sales and container activity through reports and charts.

**Why this priority**: Reporting and settings support operational policy, but core order/customer workflows can be completed without chart customization.

**Independent Test**: Change a setting, reopen the settings screen, and verify the value persists; open reports and verify charts are populated from database records.

**Acceptance Scenarios**:

1. **Given** a setting key exists, **When** its value is changed and saved, **Then** the key/value record is updated in MySQL.
2. **Given** report data exists, **When** Reports or ReportsCharts is opened, **Then** the UI presents database-backed totals and chart representations.
3. **Given** database report queries fail, **When** a report is opened, **Then** the system displays an error dialog and does not crash.

---

### Edge Cases

- MySQL is offline, unreachable, or using wrong credentials.
- `schema.sql` is accidentally run more than once; it intentionally drops and recreates `hydroflow_db` and is destructive.
- Environment variables are missing or database credentials contain spaces or unsupported characters.
- A delivery changes status during multiple user actions.
- Mark-as-delivered is submitted more than once.
- A customer is deleted or changed after an order is created.
- Amount paid is less than, equal to, or greater than amount due.
- Quantity or empty-container counts are zero or negative.
- Database decimals are rounded or exceed UI precision.
- Password values are shown in source, logs, UI, or errors.
- JFreeChart or MySQL Connector/J is missing from Eclipse project classpath.
- SQL `DROP DATABASE` is executed against a production database.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST authenticate users against the `users` table using SHA-256 password hashes.
- **FR-002**: System MUST provide Swing screens for login, dashboard navigation, customer entry, order entry, order listing, sales records, settings, reports, chart reports, ledger review, and empty returns.
- **FR-003**: System MUST persist customers with name, address, and contact number.
- **FR-004**: System MUST persist orders with customer, order type, water type, container type, quantity, empties-in count, unit price, delivery fee, discount, deposit, amount due, order status, payment status, amount paid, change amount, order timestamp, and remarks.
- **FR-005**: System MUST support order types `Delivery` and `Walk-in` and persist status values allowed by the application.
- **FR-006**: System MUST support water types `Alkaline` and `Mineral`.
- **FR-007**: System MUST support container types `Round Cylinder` and `Slim Rectangular`.
- **FR-008**: System MUST calculate amount due from quantity, unit price, delivery fee, discount, and deposit according to the existing station formula.
- **FR-009**: System MUST calculate payment change as amount paid minus amount due; a negative change MUST NOT be represented as valid change.
- **FR-010**: System MUST update an order and insert its ledger row atomically when the order is marked delivered.
- **FR-011**: System MUST prevent duplicate ledger rows for the same delivered order.
- **FR-012**: System MUST persist ledger entries with order number, ledger date, customer, water/container type, quantity out, empties in, and deposit.
- **FR-013**: System MUST support settings as key/value records and use settings for station pricing and operational values.
- **FR-014**: System MUST provide database-backed sales and operational reports.
- **FR-015**: System MUST display database and validation errors in a user-friendly dialog without terminating the Swing event dispatch thread.
- **FR-016**: Database credentials MUST be configurable through environment variables rather than hard-coded shared source.
- **FR-017**: The system MUST initialize MySQL database, tables, constraints, and seed data from `sql/schema.sql` exactly once.
- **FR-018**: The system MUST have no SQL text embedded in or required in Swing UI classes.
- **FR-019**: DAOs MUST own all database operations and SQL for their corresponding entities.
- **FR-020**: Passwords MUST be hashed and MUST NOT be stored or logged as plaintext.

### Key Entities *(include if feature involves data)*

- **User**: Administrator login identity; fields include username and SHA-256 password hash.
- **Customer**: Station customer; fields include ID, name, address, and contact number.
- **Settings**: Key/value station configuration, including delivery fee, deposit, product prices, container count, and daily sales quota.
- **Order**: A Delivery or Walk-in sales transaction; fields include customer, order type, water/container details, quantity, pricing, payment, status, timestamp, and remarks.
- **LedgerEntry**: Delivered-order container and deposit record; fields include order number, date, customer, water/container type, quantity out, empties in, and deposit.
- **Billing**: Domain calculation helper for order and payment totals.
- **DataStore**: Shared access coordinator used by Swing screens to reach application data operations.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: An authorized employee can sign in and reach the dashboard in under 10 seconds on a local workstation.
- **SC-002**: A new customer record can be created and reused in order entry in under 1 minute.
- **SC-003**: A complete new order can be entered and persisted in under 2 minutes.
- **SC-004**: Marking an order delivered produces exactly one matching ledger row in 100% of test runs.
- **SC-005**: Repeating delivery processing does not create duplicate ledger rows.
- **SC-006**: 100% of invalid credentials, missing database credentials, and database connection failures produce a readable error dialog instead of a process crash.
- **SC-007**: Reports open and display database-backed values from all available order, customer, and ledger records.
- **SC-008**: Running `sql/schema.sql` against an initialized test database creates all required tables and seed data with no schema execution errors.
- **SC-009**: No plaintext default password or database password is committed in application source or documentation.
- **SC-010**: No Swing UI class contains direct JDBC SQL or database connection code.

## Assumptions

- The desktop application runs on a workstation with MySQL Server reachable on localhost or a configured network host.
- The application is a small internal tool and the default initial account is for setup only; production deployment requires credential rotation.
- Java Swing is the approved desktop UI for this feature.
- Eclipse project metadata and external JAR instructions remain supported unless explicitly replaced.
- The existing seed settings and formula values are initial defaults, not necessarily approved production policy.
- The database name is `hydroflow_db` and the schema is the source of truth for the five core table structures.
- JFreeChart is used for report charts and MySQL Connector/J provides JDBC connectivity.
- The environment variable names `HYDROFLOW_DB_URL`, `HYDROFLOW_DB_USER`, and `HYDROFLOW_DB_PASS` are the intended shared credential interface.

## Technology Context

- **Primary language**: Java 122,242 bytes in GitHub language statistics.
- **UI**: Java Swing.
- **Database**: MySQL 5.7 / 8.x.
- **JDBC**: MySQL Connector/J.
- **Charts**: JFreeChart.
- **Architecture**: `ui -> data/DataStore -> dao -> db/DBConnection -> MySQL`.
- **Source layout**:

```text
src/hydroflow/
  HydroFlowApp.java       application entry point
  db/                     JDBC connection and database exceptions
  dao/                    entity SQL operations
  data/                   shared DataStore façade
  model/                  Customer, Order, LedgerEntry, Settings, Billing
  ui/                     Swing frames and helpers
sql/schema.sql            destructive database bootstrap schema
lib/README.txt            external JAR setup instructions
```

## Constitution / Quality Gates

- Prefer keeping Swing UI classes free of SQL and JDBC responsibilities.
- Preserve `DBConnection` as the sole JDBC connection point.
- Preserve the one-transaction mark-delivered behavior.
- Reject invalid billing quantities and payment values before persistence.
- Do not copy plaintext passwords or credentials into source, logs, or specs.
- Use exact decimal values for currency and payment calculations.
- Keep the schema bootstrap separate from normal runtime database operations.
- Validate database behavior with MySQL before relying on chart or report totals.
