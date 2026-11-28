# Veterinary Backend — Veterinaria: Gestión de Mascotas y Citas médicas

REST API segura y documentada con Spring Boot 4.1.1 + Java 25.
Evaluación 3 (60 puntos) — Desarrollo Back-end.

## Stack

- Spring Boot 4.1.1, Java 25, Gradle
- Spring Web MVC, Spring Data JPA, Spring Security, Bean Validation
- JWT (jjwt 0.12.6, HS256, 24 h) + BCrypt
- MapStruct 1.6.3
- springdoc-openapi 3.2.0 (Swagger UI)
- PostgreSQL (producción / desarrollo), H2 (tests y pruebas locales)

## Setup

```bash
# 1. Create the database (PostgreSQL)
CREATE DATABASE veterinary;

# 2. Copy the config template and fill in YOUR OWN values
cp src/main/resources/application.properties.example src/main/resources/application.properties

# 3. Set the required environment variables (no defaults on purpose)
export DB_URL=jdbc:postgresql://localhost:5432/veterinary
export DB_USERNAME=postgres
export DB_PASSWORD=tu_contrasena
export JWT_SECRET=tu_clave_secreta_de_al_menos_64_caracteres

# 4. Run
./gradlew bootRun
```

> `application.properties` is gitignored and must never be committed.
> It holds real credentials. Only `application.properties.example` lives in
> the repository.

Swagger UI: http://localhost:8080/swagger-ui.html

## Defense checklist — Dec 1 (live demo)

Run this exact sequence on the defense machine before presenting:

1. Clone the repository (branch `main`):
   ```bash
   git clone https://github.com/oswaldo300/veterinary_backend.git
   cd veterinary_backend
   ```
2. Create the database (PostgreSQL):
   ```sql
   CREATE DATABASE veterinary;
   ```
3. Set the environment variables (no defaults on purpose):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/veterinary
   export DB_USERNAME=postgres
   export DB_PASSWORD=tu_contrasena
   export JWT_SECRET=tu_clave_secreta_de_al_menos_64_caracteres
   ```
4. Run the API:
   ```bash
   ./gradlew bootRun
   ```
   If `./gradlew` fails because `gradle-wrapper.jar` is missing, run with
   IntelliJ's bundled Gradle instead, or place the wrapper jar in
   `gradle/wrapper/` first.
5. Open Swagger UI: http://localhost:8080/swagger-ui.html
6. Log in (`POST /auth/login`) with the demo owner:
   ```json
   {"clientId": "OWN-001", "password": "Vet1234!"}
   ```
7. Click **Authorize**, paste **only the token** (no `Bearer ` prefix),
   click Authorize.
8. Run a live CRUD + appointment flow in Swagger UI:
   - `GET /pets` (seeded pet Firulais is listed).
   - `POST /pets` to create a pet, `PUT /pets/{id}` to update it.
   - `POST /appointments` with nested services (see the example below) —
     the response shows the server-computed `total` and the `details`.
   - `PUT /appointments/{id}` to change the `status` to `COMPLETED`.
   - `DELETE /appointments/{id}` (204) — its details are deleted with it.
9. Show the error contract: `GET /pets` without a token returns 401,
   `GET /pets/999999` returns 404, deleting a service used in an
   appointment returns 409 (never 500).

## Verify flow (login -> Authorize -> consume)

1. `POST /auth/login` with
   ```json
   {"clientId": "OWN-001", "password": "Vet1234!"}
   ```
   (demo owner seeded automatically on first run).
2. Copy the `token` from the response.
3. In Swagger UI, click **Authorize**, paste **only the token**
   (no `Bearer ` prefix — Swagger adds it), click Authorize.
4. Call any protected endpoint, e.g. `GET /pets`.

## API endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /auth/login | Login, returns JWT | Public |
| GET | /owners | List all owners | Bearer |
| GET | /owners/{id} | Get owner by id | Bearer |
| POST | /owners | Create owner (201) | Bearer |
| PUT | /owners/{id} | Update owner | Bearer |
| DELETE | /owners/{id} | Delete owner (204; 409 if it has pets) | Bearer |
| GET | /pets | List all pets | Bearer |
| GET | /pets/{id} | Get pet by id | Bearer |
| POST | /pets | Create pet (201) | Bearer |
| PUT | /pets/{id} | Update pet | Bearer |
| DELETE | /pets/{id} | Delete pet (204; 409 if it has appointments) | Bearer |
| GET | /veterinarians | List all veterinarians | Bearer |
| GET | /veterinarians/{id} | Get veterinarian by id | Bearer |
| POST | /veterinarians | Create veterinarian (201) | Bearer |
| PUT | /veterinarians/{id} | Update veterinarian | Bearer |
| DELETE | /veterinarians/{id} | Delete veterinarian (204; 409 if it has appointments) | Bearer |
| GET | /services | List all services | Bearer |
| GET | /services/{id} | Get service by id | Bearer |
| POST | /services | Create service (201) | Bearer |
| PUT | /services/{id} | Update service | Bearer |
| DELETE | /services/{id} | Delete service (204; 409 if used in an appointment) | Bearer |
| GET | /appointments | List all appointments | Bearer |
| GET | /appointments/{id} | Get appointment by id (with details) | Bearer |
| GET | /appointments/pet/{petId} | List appointments of a pet | Bearer |
| POST | /appointments | Create appointment with services (201) | Bearer |
| PUT | /appointments/{id} | Update appointment | Bearer |
| DELETE | /appointments/{id} | Delete appointment (204) | Bearer |

### Create appointment (master-detail, nested JSON)

`POST /appointments` — `unitPrice` and `subtotal` are computed server-side
from the service price; the client never sends totals.

```json
{
  "petId": 1,
  "veterinarianId": 1,
  "dateTime": "2026-12-01T10:00:00",
  "reason": "Vacunación anual",
  "status": "SCHEDULED",
  "services": [
    { "serviceId": 2, "quantity": 1 }
  ]
}
```

## Tests

```bash
./gradlew test
```

Integration tests (H2 in-memory, `VeterinaryApiIntegrationTest`) cover:

- Login OK / wrong credentials 401 / unknown owner 401; the owner response
  never leaks the password hash.
- Full CRUD cycle for every catalog: owners (including "update without a
  password keeps the stored hash and login still works"), pets,
  veterinarians and services — create 201, get, update, delete 204,
  get after delete 404.
- Appointment lifecycle: create with nested services (server-computed
  total), get with details, update status to COMPLETED keeping the booked
  services and the total, delete 204 and get 404 afterwards.
- Security: `GET` without a token returns 401 on every protected group
  (`/owners`, `/pets`, `/veterinarians`, `/services`, `/appointments`).
- Cascade: creating an appointment persists its details; deleting an
  appointment removes its details (the freed service can then be deleted).
- Error contract: 404 on missing ids, 400 on invalid bodies and malformed
  JSON, 409 (not 500) when deleting an owner with pets, a pet with
  appointments, a veterinarian with appointments, or a service used in an
  appointment.
- Seeded demo data: the list endpoints return the demo owner `OWN-001`,
  veterinarian `VET-1001`, the 5 seeded services and the pet Firulais.
