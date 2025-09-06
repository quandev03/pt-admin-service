package vn.vnsky.bcss.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import vn.vnsky.bcss.admin.data.JsonFileSource;
import vn.vnsky.bcss.admin.mock.AuthorizationMockConfig;
import vn.vnsky.bcss.admin.mock.WithMockCustomUser;

import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(AuthorizationMockConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(value = "/existed-client.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/existed-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/existed-role.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/existed-group.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/truncate-group.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@Sql(value = "/truncate-role.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@Sql(value = "/truncate-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@Sql(value = "/truncate-client.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class GroupApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @ParameterizedTest
    @Order(1)
    @JsonFileSource(resources = "/create-group.json")
    @Rollback
    @WithMockCustomUser
    void testCreateInternalGroup(byte[] jsonBytes) throws Exception {
        mvc.perform(post("/private/api/groups/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBytes)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @ParameterizedTest
    @Order(2)
    @JsonFileSource(resources = "/update-group.json", targetType = JsonFileSource.TargetType.NODE)
    @Rollback
    @WithMockCustomUser
    void testUpdateInternalGroup(JsonNode jsonNode) throws Exception {
        String id = jsonNode.get("id").asText();
        mvc.perform(put("/private/api/groups/internal/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsBytes(jsonNode))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)));
    }

    @Test
    @Order(3)
    @WithMockCustomUser
    void testGetInternalGroupDetails() throws Exception {
        String id = "01JGX8SEPWYFMV41SX6M9VRC37";
        mvc.perform(get("/private/api/groups/internal/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)));
    }

    @Test
    @Order(4)
    @WithMockCustomUser
    void testDeleteInternalGroup() throws Exception {
        String id = "01JGX8SEPWYFMV41SX6M9VRC37";
        mvc.perform(delete("/private/api/groups/internal/" + id)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
