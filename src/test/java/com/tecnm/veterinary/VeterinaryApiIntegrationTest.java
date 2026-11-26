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
}
