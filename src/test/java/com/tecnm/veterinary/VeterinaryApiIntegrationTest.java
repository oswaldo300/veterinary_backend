package com.tecnm.veterinary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VeterinaryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Spring Boot 4 exposes a Jackson 3 ObjectMapper bean (tools.jackson),
    // not a Jackson 2 one, so the test keeps its own Jackson 2 mapper for
    // parsing MockMvc response bodies (plain JSON either way).
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String loginAsDemoOwner() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\": \"OWN-001\", \"password\": \"Vet1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    private String createOwner(String token, String ownerId) throws Exception {
        mockMvc.perform(post("/owners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"" + ownerId + "\", \"firstName\": \"Test\", "
                                + "\"lastName\": \"User\", \"phone\": \"9990000000\", "
                                + "\"email\": \"" + ownerId.toLowerCase() + "@example.com\", "
                                + "\"password\": \"Vet1234!\"}"))
                .andExpect(status().isCreated());
        return ownerId;
    }

    private long createPet(String token, String ownerId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/pets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\", \"species\": \"Perro\", "
                                + "\"breed\": \"Mestizo\", \"age\": 2, \"weightKg\": 10.0, "
                                + "\"ownerId\": \"" + ownerId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createVeterinarian(String token, String license) throws Exception {
        MvcResult result = mockMvc.perform(post("/veterinarians")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Test\", \"lastName\": \"Vet\", "
                                + "\"specialty\": \"General\", \"licenseNumber\": \"" + license + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createService(String token, String name, double price) throws Exception {
        MvcResult result = mockMvc.perform(post("/services")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"" + name + "\", \"description\": \"Test service\", "
                                + "\"price\": " + price + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void loginOkReturnsToken() throws Exception {
        loginAsDemoOwner();
    }

    @Test
    void loginWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\": \"OWN-001\", \"password\": \"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginUnknownOwnerReturns401() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\": \"OWN-999\", \"password\": \"Vet1234!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPetsWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/pets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOwnerNeverLeaksPassword() throws Exception {
        String token = loginAsDemoOwner();
        mockMvc.perform(get("/owners/OWN-001")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("OWN-001"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertFalse(
                        result.getResponse().getContentAsString().contains("password")));
    }

    @Test
    void petFullCrud() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-CRUD");
        long petId = createPet(token, "OWN-CRUD", "Toby");

        mockMvc.perform(get("/pets/" + petId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toby"))
                .andExpect(jsonPath("$.ownerId").value("OWN-CRUD"));

        mockMvc.perform(put("/pets/" + petId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Toby Jr\", \"species\": \"Perro\", "
                                + "\"breed\": \"Mestizo\", \"age\": 3, \"weightKg\": 11.0, "
                                + "\"ownerId\": \"OWN-CRUD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Toby Jr"));

        mockMvc.perform(delete("/pets/" + petId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/pets/" + petId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAppointmentWithNestedServicesPersistsDetails() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-APPT");
        long petId = createPet(token, "OWN-APPT", "Luna");
        long vetId = createVeterinarian(token, "VET-APPT");
        long service1 = createService(token, "Consulta APPT", 350.0);
        long service2 = createService(token, "Vacuna APPT", 450.0);

        String body = "{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                + ", \"dateTime\": \"2026-12-01T10:00:00\", \"reason\": \"Vacunación anual\", "
                + "\"status\": \"SCHEDULED\", \"services\": ["
                + "{\"serviceId\": " + service1 + ", \"quantity\": 1}, "
                + "{\"serviceId\": " + service2 + ", \"quantity\": 2}]}";

        MvcResult result = mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                // Server-side totals: 350*1 + 450*2 = 1250.0
                .andExpect(jsonPath("$.total").value(1250.0))
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andReturn();
        long appointmentId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Cascade check: GET the appointment shows its persisted details.
        mockMvc.perform(get("/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.details[0].unitPrice").exists())
                .andExpect(jsonPath("$.details[0].subtotal").exists());

        mockMvc.perform(get("/appointments/pet/" + petId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getMissingPetReturns404() throws Exception {
        String token = loginAsDemoOwner();
        mockMvc.perform(get("/pets/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteServiceUsedByAppointmentReturns409Not500() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-409");
        long petId = createPet(token, "OWN-409", "Rocky");
        long vetId = createVeterinarian(token, "VET-409");
        long serviceId = createService(token, "Servicio 409", 100.0);

        mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-02T09:00:00\", \"reason\": \"Control\", "
                                + "\"status\": \"SCHEDULED\", \"services\": ["
                                + "{\"serviceId\": " + serviceId + ", \"quantity\": 1}]}"))
                .andExpect(status().isCreated());

        // The service is referenced by an appointment detail: 409, never 500.
        mockMvc.perform(delete("/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void createPetWithUnknownOwnerReturns404() throws Exception {
        String token = loginAsDemoOwner();
        mockMvc.perform(post("/pets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Fantasma\", \"species\": \"Gato\", "
                                + "\"breed\": \"Siames\", \"age\": 1, \"weightKg\": 4.0, "
                                + "\"ownerId\": \"OWN-DOES-NOT-EXIST\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOwnerWithInvalidBodyReturns400() throws Exception {
        String token = loginAsDemoOwner();
        mockMvc.perform(post("/owners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"OWN-400\", \"firstName\": \"\", "
                                + "\"lastName\": \"User\", \"phone\": \"9990000000\", "
                                + "\"email\": \"not-an-email\", \"password\": \"Vet1234!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.firstName").exists())
                .andExpect(jsonPath("$.fields.email").exists());
    }

    @Test
    void createAppointmentWithUnknownPetReturns404() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-APPT-404");
        long vetId = createVeterinarian(token, "VET-APPT-404");
        long serviceId = createService(token, "Servicio APPT 404", 100.0);

        mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": 999999, \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-03T10:00:00\", \"reason\": \"Control\", "
                                + "\"status\": \"SCHEDULED\", \"services\": ["
                                + "{\"serviceId\": " + serviceId + ", \"quantity\": 1}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createAppointmentWithUnknownServiceReturns404() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-SVC-404");
        long petId = createPet(token, "OWN-SVC-404", "Milo");
        long vetId = createVeterinarian(token, "VET-SVC-404");

        mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-03T11:00:00\", \"reason\": \"Control\", "
                                + "\"status\": \"SCHEDULED\", \"services\": ["
                                + "{\"serviceId\": 999999, \"quantity\": 1}]}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createOwnerWithDuplicateEmailReturns409() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-DUP1");

        mockMvc.perform(post("/owners")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"OWN-DUP2\", \"firstName\": \"Other\", "
                                + "\"lastName\": \"User\", \"phone\": \"9990000001\", "
                                + "\"email\": \"own-dup1@example.com\", \"password\": \"Vet1234!\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deleteVeterinarianWithAppointmentsReturns409() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-VET409");
        long petId = createPet(token, "OWN-VET409", "Bella");
        long vetId = createVeterinarian(token, "VET-DEL-409");
        long serviceId = createService(token, "Servicio VET 409", 200.0);

        mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-04T09:00:00\", \"reason\": \"Control\", "
                                + "\"status\": \"SCHEDULED\", \"services\": ["
                                + "{\"serviceId\": " + serviceId + ", \"quantity\": 1}]}"))
                .andExpect(status().isCreated());

        // The veterinarian is referenced by an appointment: 409, never 500.
        mockMvc.perform(delete("/veterinarians/" + vetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createPetWithMalformedJsonReturns400() throws Exception {
        String token = loginAsDemoOwner();
        mockMvc.perform(post("/pets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Rex\", \"species\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void ownerFullCrud() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-OWNCRUD");

        mockMvc.perform(get("/owners/OWN-OWNCRUD")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.email").value("own-owncrud@example.com"));

        // Update without a password: the stored hash must be kept, so the
        // owner can still log in afterwards with the original password.
        // (The id is sent in the body because Owner.id is @NotBlank.)
        mockMvc.perform(put("/owners/OWN-OWNCRUD")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": \"OWN-OWNCRUD\", \"firstName\": \"Test\", "
                                + "\"lastName\": \"Renamed\", \"phone\": \"9990000009\", "
                                + "\"email\": \"own-owncrud@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Renamed"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientId\": \"OWN-OWNCRUD\", \"password\": \"Vet1234!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        mockMvc.perform(delete("/owners/OWN-OWNCRUD")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/owners/OWN-OWNCRUD")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void veterinarianFullCrud() throws Exception {
        String token = loginAsDemoOwner();
        long vetId = createVeterinarian(token, "VET-VETCRUD");

        mockMvc.perform(get("/veterinarians/" + vetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenseNumber").value("VET-VETCRUD"))
                .andExpect(jsonPath("$.specialty").value("General"));

        mockMvc.perform(put("/veterinarians/" + vetId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Test\", \"lastName\": \"Vet\", "
                                + "\"specialty\": \"Medicina interna\", "
                                + "\"licenseNumber\": \"VET-VETCRUD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Medicina interna"));

        mockMvc.perform(delete("/veterinarians/" + vetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/veterinarians/" + vetId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void serviceFullCrud() throws Exception {
        String token = loginAsDemoOwner();
        long serviceId = createService(token, "Servicio CRUD", 300.0);

        mockMvc.perform(get("/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Servicio CRUD"))
                .andExpect(jsonPath("$.price").value(300.0));

        mockMvc.perform(put("/services/" + serviceId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Servicio CRUD\", "
                                + "\"description\": \"Updated description\", \"price\": 500.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(500.0));

        mockMvc.perform(delete("/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void appointmentLifecycle() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-APPT-LIFE");
        long petId = createPet(token, "OWN-APPT-LIFE", "Max");
        long vetId = createVeterinarian(token, "VET-APPT-LIFE");
        long service1 = createService(token, "Consulta LIFE", 350.0);
        long service2 = createService(token, "Vacuna LIFE", 450.0);

        // 1. Create with nested services: total is computed server-side.
        MvcResult result = mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-05T10:00:00\", \"reason\": \"Vacunación anual\", "
                                + "\"status\": \"SCHEDULED\", \"services\": ["
                                + "{\"serviceId\": " + service1 + ", \"quantity\": 1}, "
                                + "{\"serviceId\": " + service2 + ", \"quantity\": 2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(1250.0))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andReturn();
        long appointmentId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // 2. Get with details.
        mockMvc.perform(get("/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.details[0].serviceName").exists())
                .andExpect(jsonPath("$.total").value(1250.0));

        // 3. Update status: the booked services stay as they are.
        mockMvc.perform(put("/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-05T10:00:00\", \"reason\": \"Vacunación anual\", "
                                + "\"status\": \"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.total").value(1250.0))
                .andExpect(jsonPath("$.details", hasSize(2)));

        // 4. Delete, then it is gone everywhere (detail + pet listing).
        mockMvc.perform(delete("/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/appointments/pet/" + petId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void protectedEndpointsWithoutTokenReturn401() throws Exception {
        mockMvc.perform(get("/owners")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/veterinarians")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/services")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/appointments")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/owners/OWN-001")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/appointments/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void deletingAppointmentRemovesItsDetails() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-APPT-DEL");
        long petId = createPet(token, "OWN-APPT-DEL", "Nala");
        long vetId = createVeterinarian(token, "VET-APPT-DEL");
        long serviceId = createService(token, "Servicio APPT DEL", 100.0);

        MvcResult result = mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-06T09:00:00\", \"reason\": \"Control\", "
                                + "\"status\": \"SCHEDULED\", \"services\": ["
                                + "{\"serviceId\": " + serviceId + ", \"quantity\": 1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andReturn();
        long appointmentId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/appointments/" + appointmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // The cascade removed the details too: the service is no longer
        // referenced, so deleting it now succeeds (it would be 409 otherwise).
        mockMvc.perform(delete("/services/" + serviceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOwnerWithPetsReturns409() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-OWNER409");
        createPet(token, "OWN-OWNER409", "Simba");

        mockMvc.perform(delete("/owners/OWN-OWNER409")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deletePetWithAppointmentsReturns409() throws Exception {
        String token = loginAsDemoOwner();
        createOwner(token, "OWN-PET409");
        long petId = createPet(token, "OWN-PET409", "Coco");
        long vetId = createVeterinarian(token, "VET-PET409");
        long serviceId = createService(token, "Servicio PET 409", 150.0);

        mockMvc.perform(post("/appointments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"petId\": " + petId + ", \"veterinarianId\": " + vetId
                                + ", \"dateTime\": \"2026-12-07T09:00:00\", \"reason\": \"Control\", "
                                + "\"status\": \"SCHEDULED\", \"services\": ["
                                + "{\"serviceId\": " + serviceId + ", \"quantity\": 1}]}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/pets/" + petId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void listEndpointsReturnSeededData() throws Exception {
        String token = loginAsDemoOwner();

        // The seeder creates the demo owner, one veterinarian and 5 services.
        mockMvc.perform(get("/owners")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem("OWN-001")));

        mockMvc.perform(get("/veterinarians")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].licenseNumber", hasItem("VET-1001")));

        mockMvc.perform(get("/services")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[*].name", hasItem("Vacunación")));

        mockMvc.perform(get("/pets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Firulais")));
    }
}
