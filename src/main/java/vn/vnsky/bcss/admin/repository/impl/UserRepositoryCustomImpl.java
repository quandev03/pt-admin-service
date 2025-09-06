package vn.vnsky.bcss.admin.repository.impl;

import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.vnsky.bcss.admin.constant.AuthConstants;
import vn.vnsky.bcss.admin.dto.*;
import vn.vnsky.bcss.admin.repository.UserRepositoryCustom;
import vn.vnsky.bcss.admin.util.DbMapper;
import vn.vnsky.bcss.admin.util.SecurityUtil;
import vn.vnsky.bcss.admin.util.StringUtil;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    private static final String SEARCH_USER_SQL = """
                with all_data as (SELECT u.id as id, u.fullname, u.username, u.email, u.phone_number, u.status, u.type, u.last_modified_date, u.last_modified_by,
                u.created_date, u.created_by,
                g.id as group_id, g.name as group_name, g.code as group_code, g.status as group_status,
                r1.id as role_user_id, r1.code as role_user_code, r1.name as role_user_name, r1.status as role_user_status,
                r2.id as role_group_id, r2.code as role_group_code, r2.name as role_group_name, r2.status as role_group_status
                from "USER" u
                left join group_user gu on u.id = gu.user_id
                left join "GROUP" g on g.id = gu.group_id
                left join role_user ru on u.id = ru.user_id
                left join role r1 on ru.role_id = r1.id
                left join group_role rg on g.id = rg.group_id
                left join role r2 on r2.id = rg.role_id
                where (u.client_id = :clientId)
                and ((:term is null or (lower(u.fullname) like :term) or (lower(u.username) like :term) or (lower(u.email) like :term)))
                and (:statusSize = 0 or u.STATUS in (:status))
                ),
                page_ids as (select distinct all_data.id, all_data.last_modified_date from all_data order by all_data.last_modified_date desc, all_data.id
                             OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY),
                total_count as (select count(distinct all_data.id) as total from all_data)
                select all_data.*, total_count.total from all_data
                inner join page_ids on all_data.id = page_ids.id
                cross join total_count
                order by all_data.last_modified_date desc, all_data.created_date DESC
            """;

    private static final String ALL_USER_BY_DEPARTMENT_SQL = """
                select U.* from "USER" U
                           inner join DEPARTMENT_USER DU on U.ID = DU.USER_ID
                           inner join DEPARTMENT D on D.ID = DU.DEPARTMENT_ID
                where U.CLIENT_ID = :clientId and D.CODE = :departmentCode
                order by U.last_modified_date desc, U.created_date DESC
            """;

    private static final String SEARCH_USER_BY_CLIENT_TYPE_SQL = """
                select u.*,
                c.ID AS CLIENT_ID, c.CODE AS CLIENT_CODE, c.NAME AS CLIENT_NAME, c.STATUS AS CLIENT_STATUS
                from "USER" u
                left join CLIENT c on c.ID = u.CLIENT_ID
                where
                    (:clientType is null or (:clientType = 1 and u.CLIENT_ID <> :clientVnskyId) or (:clientType = 0 and u.CLIENT_ID = :clientVnskyId))
                    and (:term is null or u.USERNAME COLLATE BINARY_CI like :term or u.FULLNAME COLLATE BINARY_CI like :term or u.EMAIL COLLATE BINARY_CI like :term)
                order by u.last_modified_date desc, u.created_date DESC
                offset 0 rows fetch next 10 rows only
            """;

    private static final String PROC_CLIENT_ID_PARAM = "P_CLIENT_ID";

    private static final String PROC_OWNER_TYPE_PARAM = "P_OWNER_TYPE";

    private static final String PROC_REF_CURSOR_PARAM = "p_rc";

    private static final String CLIENT_ID_PARAM = "clientId";

    private static final String STATUS_SIZE_PARAM = "statusSize";

    private static final String STATUS_COL = "status";

    private final DbMapper dbMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserRepositoryCustomImpl(DbMapper dbMapper) {
        this.dbMapper = dbMapper;
    }

    @Override
    public Page<UserDTO> findByTerm(String term, List<Integer> status, String clientId, Pageable pageable) {
        Query query = this.entityManager.createNativeQuery(SEARCH_USER_SQL, Tuple.class);
        query.setParameter("term", term);
        query.setParameter(CLIENT_ID_PARAM, clientId);
        query.setParameter(STATUS_COL, status);
        query.setParameter(STATUS_SIZE_PARAM, status.size());
        query.setParameter(AuthConstants.QueryParams.LIMIT_PARAM, pageable.getPageSize());
        query.setParameter(AuthConstants.QueryParams.OFFSET_PARAM, pageable.getOffset());
        List<Tuple> resultSet = query.getResultList();
        Map<String, UserDTO> searchUserResultMap = new LinkedHashMap<>();
        for (Tuple result : resultSet) {
            this.transformResult(result, searchUserResultMap);
        }
        return new PageImpl<>(new ArrayList<>(searchUserResultMap.values()), pageable, resultSet.isEmpty() ? 0 : this.dbMapper.getLongSafe(resultSet.get(0), "total"));
    }

    private void transformResult(Tuple result, Map<String, UserDTO> searchUserResultMap) {
        String userId = result.get("id", String.class);
        UserDTO userRecord = searchUserResultMap.computeIfAbsent(userId, s -> {
            UserDTO userDTO = UserDTO.builder()
                    .id(userId)
                    .username(result.get("username", String.class))
                    .fullname(result.get("fullname", String.class))
                    .phoneNumber(result.get("phone_number", String.class))
                    .email(result.get("email", String.class))
                    .type(result.get("type", String.class))
                    .status(this.dbMapper.getIntegerSafe(result, STATUS_COL))
                    .createdBy(result.get("created_by", String.class))
                    .createdDate(this.dbMapper.getLocalDateTimeSafe(result, "created_date"))
                    .lastModifiedBy(result.get("last_modified_by", String.class))
                    .lastModifiedDate(this.dbMapper.getLocalDateTimeSafe(result, "last_modified_date"))
                    .build();
            userDTO.setGroups(new LinkedHashSet<>());
            userDTO.setRoles(new LinkedHashSet<>());
            return userDTO;
        });
        String groupId = result.get("group_id", String.class);
        if (groupId != null) {
            GroupDTO group = GroupDTO.builder()
                    .id(groupId)
                    .code(result.get("group_code", String.class))
                    .name(result.get("group_name", String.class))
                    .status(this.dbMapper.getIntegerSafe(result, "group_status"))
                    .build();
            if (userRecord.getGroups().isEmpty() || userRecord.getGroups().stream().noneMatch(ele -> ele.getId().equals(group.getId()))) {
                userRecord.getGroups().add(group);
            }
        }
        String roleUserId = result.get("role_user_id", String.class);
        if (roleUserId != null) {
            RoleDTO directRole = RoleDTO.builder()
                    .id(roleUserId)
                    .code(result.get("role_user_code", String.class))
                    .name(result.get("role_user_name", String.class))
                    .status(this.dbMapper.getIntegerSafe(result, "role_user_status"))
                    .build();
            if (userRecord.getRoles().isEmpty() || userRecord.getRoles().stream().noneMatch(ele -> ele.getId().equals(directRole.getId()))) {
                userRecord.getRoles().add(directRole);
            }
        }
        String roleGroupId = result.get("role_group_id", String.class);
        if (roleGroupId != null) {
            RoleDTO indirectRole = RoleDTO.builder()
                    .id(roleGroupId)
                    .code(result.get("role_group_code", String.class))
                    .name(result.get("role_group_name", String.class))
                    .status(this.dbMapper.getIntegerSafe(result, "role_group_status"))
                    .build();
            if (userRecord.getRoles().isEmpty() || userRecord.getRoles().stream().noneMatch(ele -> ele.getId().equals(indirectRole.getId()))) {
                userRecord.getRoles().add(indirectRole);
            }
        }
    }

    @Override
    public List<UserDTO> findAllByClientIdAndPermissions(String clientId, List<String> permissions) {
        StoredProcedureQuery storedProcedure = this.entityManager.createStoredProcedureQuery("GET_ALLOWED_USERS");
        storedProcedure.registerStoredProcedureParameter(PROC_CLIENT_ID_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(PROC_OWNER_TYPE_PARAM, String.class, ParameterMode.IN);
        storedProcedure.registerStoredProcedureParameter(PROC_REF_CURSOR_PARAM, Class.class, ParameterMode.REF_CURSOR);
        storedProcedure.setParameter(PROC_CLIENT_ID_PARAM, clientId);
        storedProcedure.setParameter(PROC_OWNER_TYPE_PARAM, AuthConstants.OWNER_TYPE);
        return storedProcedure.getResultStream()
                .map(e -> {
                    Object[] result = (Object[]) e;
                    return UserDTO.builder()
                            .id((String) result[0])
                            .username((String) result[1])
                            .fullname((String) result[2])
                            .type((String) result[3])
                            .email((String) result[4])
                            .phoneNumber((String) result[5])
                            .status(((Number) result[6]).intValue())
                            .build();
                })
                .toList();
    }

    @Override
    public List<UserDTO> findAllByClientIdAndDepartmentCode(String clientId, String departmentCode) {
        Query query = this.entityManager.createNativeQuery(ALL_USER_BY_DEPARTMENT_SQL, Tuple.class);
        query.setParameter(CLIENT_ID_PARAM, clientId);
        query.setParameter("departmentCode", departmentCode);
        return query.getResultStream()
                .map(e -> transformSimpleResult((Tuple) e, false))
                .toList();
    }

    @Override
    public List<UserDTO> findAllByClientType(Boolean isPartner, String term) {
        Query query = this.entityManager.createNativeQuery(SEARCH_USER_BY_CLIENT_TYPE_SQL, Tuple.class);
        Integer clientType;
        if (isPartner != null) {
            clientType = isPartner ? 1 : 0;
        } else {
            clientType = null;
        }
        query.setParameter("clientType", clientType);
        query.setParameter("clientVnskyId", AuthConstants.VNSKY_CLIENT_ID);
        query.setParameter("term", StringUtil.buildLikeOperator(term));
        return query.getResultStream()
                .map(e -> {
                    UserDTO userDTO = transformSimpleResult((Tuple) e, true);
                    userDTO.setPreferredUsername(SecurityUtil.getPreferredUsername(userDTO));
                    return userDTO;
                })
                .toList();
    }

    private UserDTO transformSimpleResult(Tuple result, boolean withClient) {
        UserDTO userDTO = UserDTO.builder()
                .id(result.get("id", String.class))
                .username(result.get("username", String.class))
                .fullname(result.get("fullname", String.class))
                .type(result.get("type", String.class))
                .email(result.get("email", String.class))
                .phoneNumber(result.get("phone_number", String.class))
                .status(this.dbMapper.getIntegerSafe(result, STATUS_COL))
                .build()
                ;
        if (withClient) {
            userDTO.setClient(ClientDTO.builder()
                    .id(result.get("client_id", String.class))
                    .code(result.get("client_code", String.class))
                    .name(result.get("client_name", String.class))
                    .status(this.dbMapper.getIntegerSafe(result, "client_status"))
                    .build());
        }
        return userDTO;
    }
}
