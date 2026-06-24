# Electronic Clinic
A Full-Stack Electronic Medical Records (EMR) Backend System for clinic management, covering patients, doctors, appointments, prescriptions, payments, and queuing. Backend built with Java (plain HTTP server), PostgreSQL via Supabase, JWT authentication, and BCrypt password hashing — zero framework dependencies.

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java (no framework) |
| HTTP Server | Custom `RestServer` + `SimpleRestServer` |
| Authentication | JWT (`jjwt-0.9.1`) + BCrypt (`jbcrypt-0.4`) |
| Database | Supabase PostgreSQL (JDBC) |
| JSON | Jackson (`jackson-databind-2.9.6`) |
| Build | Maven (`pom.xml`) |
| Config | `.env` file (manual load) |
| Schema | SQL migration files (`data/`) |

---

## Local Development

```bash
# Build the project
mvn compile

# Run the server
mvn exec:java -Dexec.mainClass="com.eclinic.App"

# Or use the PowerShell runner
./run-clinic.ps1

# Or the batch file
run.bat
```

The server starts on the configured port (see `.env`).

---

## Project Structure

```
Electronic-Clinic/
├── src/main/java/com/eclinic/
│   ├── api/                    # HTTP route handlers
│   │   ├── AppointmentsHandler.java
│   │   ├── AuthHandler.java
│   │   ├── DashboardHandler.java
│   │   ├── DoctorsHandler.java
│   │   ├── MedicalRecordsHandler.java
│   │   ├── MedicineCategoriesHandler.java
│   │   ├── MedicinesHandler.java
│   │   ├── NotificationsHandler.java
│   │   ├── PatientsHandler.java
│   │   ├── PaymentsHandler.java
│   │   ├── PrescriptionHandler.java
│   │   ├── PrescriptionDetailHandler.java
│   │   ├── PrescriptionTemplatesHandler.java
│   │   ├── QueueHandler.java
│   │   ├── ReceptionistsHandler.java
│   │   ├── UsersHandler.java
│   │   ├── AuditLogHandler.java
│   │   └── RestServer.java     # Main route dispatcher
│   ├── dao/                    # Data Access Objects (DB queries)
│   ├── database/               # Connection manager, migrations, seed
│   ├── models/                 # Plain Java model classes
│   ├── util/                   # JwtUtil, PasswordUtil
│   ├── App.java                # Entry point
│   └── Functions.java          # Shared utility functions
├── data/
│   ├── clinic-schema.sql       # Main schema
│   ├── supabase-schema.sql     # Supabase-specific schema
│   ├── migration-receptionists.sql
│   ├── seed-medicines.sql
│   └── seed-medicine-categories.sql
├── lib/                        # Vendored JARs
├── target/                     # Compiled classes
├── pom.xml
├── run-clinic.ps1
├── run.bat
└── .env
```

---

## Environment Variables

Copy `.env.example` to `.env` and fill in your values:

```env
# Required
DB_URL=jdbc:postgresql://<host>:<port>/<db>
DB_USER=
DB_PASSWORD=
JWT_SECRET=

# Supabase (if using hosted DB)
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=
```

---

## Database Setup

Run migrations in order against your PostgreSQL instance:

```sql
-- 1. Core schema
\i data/clinic-schema.sql

-- 2. Supabase-specific extensions (if using Supabase)
\i data/supabase-schema.sql

-- 3. Role migration
\i data/migration-receptionists.sql

-- 4. Seed data
\i data/seed-medicine-categories.sql
\i data/seed-medicines.sql
```

Test users can be inserted via:
```bash
psql -f insert_test_users.sql
```

---

## API Overview

All endpoints require a `Bearer <token>` JWT header unless otherwise noted. See `API_GUIDE.md` for full documentation.

| Domain | Base Path | Handler |
|---|---|---|
| Authentication | `/api/auth` | `AuthHandler` |
| Users | `/api/users` | `UsersHandler` |
| Patients | `/api/patients` | `PatientsHandler` |
| Doctors | `/api/doctors` | `DoctorsHandler` |
| Receptionists | `/api/receptionists` | `ReceptionistsHandler` |
| Appointments | `/api/appointments` | `AppointmentsHandler` |
| Queue | `/api/queue` | `QueueHandler` |
| Medical Records | `/api/medical-records` | `MedicalRecordsHandler` |
| Prescriptions | `/api/prescriptions` | `PrescriptionHandler` |
| Prescription Details | `/api/prescription-details` | `PrescriptionDetailHandler` |
| Prescription Templates | `/api/prescription-templates` | `PrescriptionTemplatesHandler` |
| Medicines | `/api/medicines` | `MedicinesHandler` |
| Medicine Categories | `/api/medicine-categories` | `MedicineCategoriesHandler` |
| Payments | `/api/payments` | `PaymentsHandler` |
| Notifications | `/api/notifications` | `NotificationsHandler` |
| Dashboard | `/api/dashboard` | `DashboardHandler` |
| Audit Logs | `/api/audit-logs` | `AuditLogHandler` |

---

## Roles

The system supports three roles enforced via JWT claims:

| Role | Access |
|---|---|
| `admin` | Full system access, user management |
| `doctor` | Patients, medical records, prescriptions, queue |
| `receptionist` | Appointments, queue, patient registration, payments |

See `REFACTOR_PLAN_3_ROLES.md` for the full role permission matrix.

---

## Security

✅ **Authentication** — JWT tokens signed with a configurable secret  
✅ **Passwords** — BCrypt hashed via `jbcrypt`  
✅ **Role-based access** — checked per handler per endpoint  
✅ **Audit logging** — all write operations logged to `audit_logs` table  
✅ **Secrets** — loaded from `.env`, never hardcoded  
✅ **DB credentials** — managed via `ConnectionManager`, not scattered inline  

---

## Testing

Standalone test files are included at the project root for quick verification:

```bash
# Database connectivity
javac -cp lib/* CheckDb.java && java -cp .:lib/* CheckDb

# JSON serialization
javac -cp lib/* TestJSON.java && java -cp .:lib/* TestJSON

# JWT token generation
javac -cp lib/* TestToken.java && java -cp .:lib/* TestToken

# Payment logic
javac -cp lib/* TestPayments.java && java -cp .:lib/* TestPayments
```

PowerShell smoke tests:

```powershell
# Full CRUD smoke test
./smoke-crud.ps1

# Email + password CRUD
./test-email-password-crud.ps1
```

---

## Troubleshooting

**Server won't start**
- Check `.env` has all required fields
- Verify DB is reachable: `CheckDb.java`
- Ensure port is not already in use

**JWT errors**
- Confirm `JWT_SECRET` in `.env` matches what was used to issue tokens
- Run `TestToken.java` to verify token generation

**Database connection failures**
- Run `CheckDb.java`, `CheckDb2.java`, `CheckDb3.java` in order to isolate the issue
- Confirm `DB_URL`, `DB_USER`, `DB_PASSWORD` are correct

**Build errors**
- All JARs in `lib/` are vendored — no internet required for compilation
- Run `mvn compile` and check for missing classpath entries

---
