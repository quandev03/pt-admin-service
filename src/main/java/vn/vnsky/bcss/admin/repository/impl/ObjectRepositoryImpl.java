package vn.vnsky.bcss.admin.repository.impl;

import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.dto.MenuItemDTO;
import vn.vnsky.bcss.admin.dto.ObjectActionDTO;
import vn.vnsky.bcss.admin.dto.ObjectResponseDTO;
import vn.vnsky.bcss.admin.dto.PermissionDTO;
import vn.vnsky.bcss.admin.repository.ObjectRepositoryCustom;
import vn.vnsky.bcss.admin.util.DbMapper;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author thanhvt
 * @created 12/04/2023 - 10:19 SA
 * @project str-auth
 * @since 1.0
 **/
@Slf4j
@Repository
@SuppressWarnings("unchecked")
public class ObjectRepositoryImpl implements ObjectRepositoryCustom {

    private static final String OBJECT_ACTION_SQL = """
                    SELECT O.ID AS OBJECT_ID, O.CODE AS OBJECT_CODE, O.NAME AS OBJECT_NAME,A.ID AS ACTION_ID, A.CODE AS ACTION_CODE, A.NAME AS ACTION_NAME
                        FROM OBJECT_ACTION OA
                        INNER JOIN OBJECT O ON O.ID = OA.OBJECT_ID
                        INNER JOIN ACTION A ON A.ID = OA.ACTION_ID
                        WHERE O.APP_ID = :appId AND O.CODE IN :objectCodes
            """;

    private static final String USER_ID_PARAM = "P_USER_ID";

    private static final String APP_CODE_PARAM = "P_APP_CODE";

    private static final String OWNER_TYPE_PARAM = "P_OWNER_TYPE";

    private static final String PARENT_ID_LABEL = "parent_id";

    private static final String LEVEL_LABEL = "level";

    private static final String JOINED_ACTIONS_LABEL = "joined_actions";

    private static final String REF_CURSOR_PARAM = "p_rc";

    private static final List<String> REF_CURSOR_PROPS = Arrays.asList("id", "code", "name", "uri", "icon", PARENT_ID_LABEL, LEVEL_LABEL, "path", JOINED_ACTIONS_LABEL);

    @PersistenceContext
    private EntityManager entityManager;

    private final DbMapper dbMapper;

    @Autowired
    public ObjectRepositoryImpl(DbMapper dbMapper) {
        this.dbMapper = dbMapper;
    }

    @Override
    public List<MenuItemDTO> getMenuItemHierarchy(String userId, boolean isOwner, String appCode) {
        StoredProcedureQuery storedProcedure = this.entityManager.createStoredProcedureQuery("get_menu_item_hierarchy");
        storedProcedure.registerStoredProcedureParameter(USER_ID_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(APP_CODE_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(REF_CURSOR_PARAM, Class.class, ParameterMode.REF_CURSOR);
        storedProcedure.setParameter(USER_ID_PARAM, isOwner ? null : userId);
        storedProcedure.setParameter(APP_CODE_PARAM, appCode);
        List<Object[]> result = storedProcedure.getResultList();
        return result.stream().map(e -> MenuItemDTO.builder()
                .id((String) e[REF_CURSOR_PROPS.indexOf("id")])
                .code((String) e[REF_CURSOR_PROPS.indexOf("code")])
                .name((String) e[REF_CURSOR_PROPS.indexOf("name")])
                .uri((String) e[REF_CURSOR_PROPS.indexOf("uri")])
                .icon((String) e[REF_CURSOR_PROPS.indexOf("icon")])
                .parentId((String) e[REF_CURSOR_PROPS.indexOf(PARENT_ID_LABEL)])
                .level(((Number) e[REF_CURSOR_PROPS.indexOf(LEVEL_LABEL)]).intValue())
                .joinedActions((String) e[REF_CURSOR_PROPS.indexOf(JOINED_ACTIONS_LABEL)])
                .build()
        ).toList();
    }

    @Override
    public List<MenuItemDTO> getMenuItemFlat(String userId, boolean isOwner, String appCode) {
        log.info("[DEBUG] UserId = {}, appCode = {}", userId, appCode);
        StoredProcedureQuery storedProcedure = this.entityManager.createStoredProcedureQuery("GET_MENU_ITEM_FLAT");
        storedProcedure.registerStoredProcedureParameter(USER_ID_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(APP_CODE_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(REF_CURSOR_PARAM, Class.class, ParameterMode.REF_CURSOR);
        storedProcedure.setParameter(USER_ID_PARAM, isOwner ? null : userId);
        storedProcedure.setParameter(APP_CODE_PARAM, appCode);
        List<Object[]> result = storedProcedure.getResultList();
        return result.stream().map(e -> MenuItemDTO.builder()
                .id((String) e[REF_CURSOR_PROPS.indexOf("id")])
                .code((String) e[REF_CURSOR_PROPS.indexOf("code")])
                .name((String) e[REF_CURSOR_PROPS.indexOf("name")])
                .uri((String) e[REF_CURSOR_PROPS.indexOf("uri")])
                .icon((String) e[REF_CURSOR_PROPS.indexOf("icon")])
                .parentId((String) e[REF_CURSOR_PROPS.indexOf(PARENT_ID_LABEL)])
                .level(((Number) e[REF_CURSOR_PROPS.indexOf(LEVEL_LABEL)]).intValue())
                .joinedActions((String) e[REF_CURSOR_PROPS.indexOf(JOINED_ACTIONS_LABEL)])
                .build()
        ).toList();
    }

    @Override
    public List<ObjectResponseDTO> getObjectHierarchy(String appCode) {
        StoredProcedureQuery storedProcedure = this.entityManager.createStoredProcedureQuery("GET_MENU_ITEM_HIERARCHY");
        storedProcedure.registerStoredProcedureParameter(USER_ID_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(APP_CODE_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(REF_CURSOR_PARAM, Class.class, ParameterMode.REF_CURSOR);
        storedProcedure.setParameter(USER_ID_PARAM, null);
        storedProcedure.setParameter(APP_CODE_PARAM, appCode);
        List<Object[]> result = storedProcedure.getResultList();
        return result.stream().map(e -> ObjectResponseDTO.builder()
                .key((String) e[REF_CURSOR_PROPS.indexOf("id")])
                .title((String) e[REF_CURSOR_PROPS.indexOf("name")])
                .uri((String) e[REF_CURSOR_PROPS.indexOf("uri")])
                .parentId((String) e[REF_CURSOR_PROPS.indexOf(PARENT_ID_LABEL)])
                .joinedActions((String) e[REF_CURSOR_PROPS.indexOf(JOINED_ACTIONS_LABEL)])
                .build()
        ).toList();
    }

    @Override
    public List<ObjectActionDTO> findAllObjectActionByObjectCodes(String appId, Set<String> objectCodes) {
        Query query = this.entityManager.createNativeQuery(OBJECT_ACTION_SQL, Tuple.class);
        query.setParameter("appId", appId);
        query.setParameter("objectCodes", objectCodes);
        List<Tuple> resultSet = query.getResultList();
        return this.dbMapper.castSqlResult(resultSet, ObjectActionDTO.class);
    }

    public Map<String, Set<String>> getUserPermissionList(String userId, String appCode) {
        StoredProcedureQuery storedProcedure = this.entityManager.createStoredProcedureQuery("GET_PERMISSION_LIST");
        storedProcedure.registerStoredProcedureParameter(USER_ID_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(APP_CODE_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(OWNER_TYPE_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(REF_CURSOR_PARAM, Class.class, ParameterMode.REF_CURSOR);
        storedProcedure.setParameter(USER_ID_PARAM, userId);
        storedProcedure.setParameter(APP_CODE_PARAM, appCode);
        storedProcedure.setParameter(OWNER_TYPE_PARAM, AuthConstants.OWNER_TYPE);
        List<PermissionDTO> permissionDTOList = storedProcedure.getResultStream()
                .map(e -> {
                            Object[] values = (Object[]) e;
                            return PermissionDTO.builder()
                                    .objectCode((String) values[0])
                                    .actionCode((String) values[1])
                                    .build();
                        }
                ).toList();
        Map<String, Set<String>> policyMap = permissionDTOList.stream()
                .collect(Collectors.groupingBy(PermissionDTO::getObjectCode, new Collector<PermissionDTO, Set<PermissionDTO>, Set<String>>() {
                    @Override
                    public Supplier<Set<PermissionDTO>> supplier() {
                        return HashSet::new;
                    }

                    @Override
                    public BiConsumer<Set<PermissionDTO>, PermissionDTO> accumulator() {
                        return Set::add;
                    }

                    @Override
                    public BinaryOperator<Set<PermissionDTO>> combiner() {
                        return (left, right) -> {
                            if (left.size() < right.size()) {
                                right.addAll(left);
                                return right;
                            } else {
                                left.addAll(right);
                                return left;
                            }
                        };
                    }

                    @Override
                    public Function<Set<PermissionDTO>, Set<String>> finisher() {
                        return i -> i.stream()
                                .map(PermissionDTO::getActionCode)
                                .collect(Collectors.toSet());
                    }

                    @Override
                    public Set<Characteristics> characteristics() {
                        return Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED));
                    }
                }));
        log.info("User id={} has {} object permissions", userId, policyMap.size());
        return policyMap;
    }

}
