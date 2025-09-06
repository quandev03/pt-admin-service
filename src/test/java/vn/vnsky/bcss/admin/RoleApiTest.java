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
import vn.vnsky.bcss.admin.dto.RoleDTO;
import vn.vnsky.bcss.admin.mock.AuthorizationMockConfig;
import vn.vnsky.bcss.admin.mock.EmbeddedKafkaConfig;
import vn.vnsky.bcss.admin.mock.WithMockCustomUser;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import({AuthorizationMockConfig.class, EmbeddedKafkaConfig.class})
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(value = "/object.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/action.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/object_action.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/existed-role.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(value = "/truncate-object-action.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class RoleApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @WithMockCustomUser
    void testGetAllInternalRoles() throws Exception {
        mvc.perform(get("/private/api/roles/internal/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Order(2)
    @WithMockCustomUser
    void testGetAllPartnerRoles() throws Exception {
        mvc.perform(get("/private/api/roles/partner/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    @Order(3)
    @WithMockCustomUser
    void testGetRoles() throws Exception {
        String query = "admin";
        mvc.perform(get("/private/api/roles/internal?q=" + query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(4)
    @WithMockCustomUser
    void testGetPartnerRoles() throws Exception {
        String query = "admin";
        mvc.perform(get("/private/api/roles/partner?q=" + query)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @ParameterizedTest
    @Order(5)
    @CsvFileSource(resources = "/create-role.csv", useHeadersInDisplayName = true)
    @Rollback
    @WithMockCustomUser
    void testCreateRole(String code, String name, int status, String checkedKeys) throws Exception {
        RoleDTO request = new RoleDTO();
        request.setCode(code);
        request.setName(name);
        request.setStatus(status);
        request.setCheckedKeys(List.of(checkedKeys.split(";")));
        String requestBody = objectMapper.writeValueAsString(request);

        mvc.perform(post("/private/api/roles/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @ParameterizedTest
    @Order(6)
    @CsvFileSource(resources = "/create-partner-role.csv", useHeadersInDisplayName = true)
    @Rollback
    @WithMockCustomUser
    void testCreatePartnerRole(String code, String name, int status, String checkedKeys) throws Exception {
        RoleDTO request = new RoleDTO();
        request.setCode(code);
        request.setName(name);
        request.setStatus(status);
        request.setCheckedKeys(List.of(checkedKeys.split(";")));
        String requestBody = objectMapper.writeValueAsString(request);

        mvc.perform(post("/private/api/roles/partner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @Order(7)
    @WithMockCustomUser
    void testGetRoleDetails() throws Exception {
        String id = "01J84M5S09J9BGEJYZZNEW7Y2J";
        mvc.perform(get("/private/api/roles/internal/" + id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(id)));
    }

    @Test
    @Order(8)
    @WithMockCustomUser
    void testGetPartnerRoleDetails() throws Exception {
        String roleId = "01JDV2K0GQ6MYAFZYPQDQM8QK9";
        mvc.perform(get("/private/api/roles/partner/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(roleId)));
    }

    @ParameterizedTest
    @Order(8)
    @CsvFileSource(resources = "/update-role.csv", useHeadersInDisplayName = true)
    @Rollback
    @WithMockCustomUser
    void testUpdateRole(String roleId, String code, String name, int status, String checkedKeys) throws Exception {
        RoleDTO request = new RoleDTO();
        request.setCode(code);
        request.setName(name);
        request.setStatus(status);
        request.setCheckedKeys(List.of(checkedKeys.split(";")));
        String requestBody = objectMapper.writeValueAsString(request);

        mvc.perform(put("/private/api/roles/internal/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(roleId)));
    }

    @ParameterizedTest
    @Order(9)
    @CsvFileSource(resources = "/update-partner-role.csv", useHeadersInDisplayName = true)
    @Rollback
    @WithMockCustomUser
    void testUpdatePartnerRole(String roleId, String code, String name, int status, String checkedKeys) throws Exception {
        RoleDTO request = new RoleDTO();
        request.setCode(code);
        request.setName(name);
        request.setStatus(status);
        request.setCheckedKeys(List.of(checkedKeys.split(";")));
        String requestBody = objectMapper.writeValueAsString(request);

        mvc.perform(put("/private/api/roles/partner/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(roleId)));
    }

    @Test
    @Order(10)
    @WithMockCustomUser
    void testDeleteRole() throws Exception {
        String roleId = "01J84M5S09J9BGEJYZZNEW7Y2J";
        mvc.perform(delete("/private/api/roles/internal/" + roleId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(11)
    @WithMockCustomUser
    void testDeletePartnerRole() throws Exception {
        String roleId = "01JDV2K0GQ6MYAFZYPQDQM8QK9";
        mvc.perform(delete("/private/api/roles/partner/" + roleId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @Order(12)
    @CsvFileSource(resources = "/create-partner-role.csv", useHeadersInDisplayName = true)
    @Rollback
    @WithMockCustomUser
    void testCreatePartnerMobileRole(String code, String name, int status, String checkedKeys) throws Exception {
        RoleDTO request = new RoleDTO();
        request.setCode(code);
        request.setName(name);
        request.setStatus(status);
        request.setCheckedKeys(List.of(checkedKeys.split(";")));
        String requestBody = objectMapper.writeValueAsString(request);

        mvc.perform(post("/private/api/roles/partner?isMobile=true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

}
