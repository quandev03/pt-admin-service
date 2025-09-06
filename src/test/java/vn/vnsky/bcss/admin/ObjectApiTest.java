package vn.vnsky.bcss.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import vn.vnsky.bcss.admin.dto.CreateUpdateObjectDTO;
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
@Sql(value = "/object.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/action.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/object_action.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/oauth-client.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/truncate-object-action.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class ObjectApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(2)
    @WithMockCustomUser
    void testGetAllObjects() throws Exception {
        mvc.perform(get("/private/api/objects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @ParameterizedTest
    @Order(1)
    @CsvFileSource(resources = "/create-object.csv", useHeadersInDisplayName = true)
    @Rollback
    @WithMockCustomUser
    void testCreateObject(String name, String url, boolean isPartner, int ordinal, String code, String parentId) throws Exception {
        CreateUpdateObjectDTO request = CreateUpdateObjectDTO.builder()
                .name(name)
                .url(url)
                .isPartner(isPartner)
                .ordinal(ordinal)
                .code(code)
                .parentId(parentId)
                .build();
        mvc.perform(post("/private/api/objects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsBytes(request))
                        .with(csrf()))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.key").isNotEmpty());
    }

    @Test
    @Order(3)
    @WithMockCustomUser
    void testGetObjectDetails() throws Exception {
        String id = "01J7D9XQ5ZF5YK0BW4AD5PRE4E"; // Replace with a valid ID for testing
        mvc.perform(get("/private/api/objects" + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.key", is(id)));
    }

    @ParameterizedTest
    @Order(4)
    @CsvFileSource(resources = "/update-object.csv", useHeadersInDisplayName = true)
    @Rollback
    @WithMockCustomUser
    void testUpdateObject(String id, String name, String url, boolean isPartner, int ordinal, String code, String parentId) throws Exception {
        CreateUpdateObjectDTO request = CreateUpdateObjectDTO.builder()
                .name(name)
                .url(url)
                .isPartner(isPartner)
                .ordinal(ordinal)
                .code(code)
                .parentId(parentId)
                .build();

        mvc.perform(put("/private/api/objects" + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsBytes(request))
                        .with(csrf()))
                .andDo(result -> System.out.println(result.getResponse().getContentAsString()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.key", is(id)));

    }

    @Test
    @Order(5)
    @WithMockCustomUser
    void testDeleteObject() throws Exception {
        String id = "01J7D9XQ5ZF5YK0BW4AD5PRE4E"; // Replace with a valid ID for testing
        mvc.perform(delete("/private/api/objects" + "/" + id)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

}

