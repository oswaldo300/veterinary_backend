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

Integration tests (H2 in-memory) cover: login OK / wrong credentials 401,
protected endpoints without token 401, full pet CRUD, appointment creation
with nested services and cascade persistence, 404 on missing ids, and
409 (not 500) when deleting a service used in an appointment.
