package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.*;
import vn.vnsky.bcss.admin.dto.GroupDTO;
import vn.vnsky.bcss.admin.dto.RoleDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.dto.UserSuggestDTO;
import vn.vnsky.bcss.admin.entity.GroupEntity;
import vn.vnsky.bcss.admin.entity.RoleEntity;
import vn.vnsky.bcss.admin.entity.UserEntity;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RoleMapper.class, GroupMapper.class, DepartmentMapper.class},
        unmappedSourcePolicy = ReportingPolicy.WARN, unmappedTargetPolicy = ReportingPolicy.WARN)
public interface UserMapper extends BaseMapper<UserDTO, UserEntity> {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "fullname", source = "fullname")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "status", source = "status")
    @Named("toDtoPure")
    UserDTO toDtoPure(UserEntity entity);

    @Override
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "departments", ignore = true)
    @Named("ignoreRoleGroup")
    UserDTO toDto(UserEntity objEntity);

    @Named("full")
    @Mapping(target = "groups", qualifiedByName = "toDtoPureGroups")
    @Mapping(target = "roles", qualifiedByName = "toDtoPureRoles")
    @Mapping(target = "departments", source = "departments")
    UserDTO convertEntity2DtoFull(UserEntity objEntity);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Named("toDtoPureGroup")
    GroupDTO toDtoPureGroup(GroupEntity entity);

    @Named("toDtoPureGroups")
    @IterableMapping(qualifiedByName = "toDtoPureGroup")
    Set<GroupDTO> toDtoPureGroup(Set<GroupEntity> entityList);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Named("toDtoPureRole")
    RoleDTO toDtoPureRole(RoleEntity entity);

    @Named("toDtoPureRoles")
    @IterableMapping(qualifiedByName = "toDtoPureRole")
    Set<RoleDTO> toDtoPureRole(Set<RoleEntity> entityList);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @BeanMapping(ignoreByDefault = true)
    @Named("ignore")
    UserSuggestDTO convertIgnore(UserEntity objEntity);

    @IterableMapping(qualifiedByName = "ignore")
    List<UserDTO> mapperToDTOIgnoring(List<UserEntity> groups);

    @Override
    @IterableMapping(qualifiedByName = "ignoreRoleGroup")
    List<UserDTO> toDto(List<UserEntity> objEntity);

    @Named("groupIgnore")
    @Mapping(target = "users", ignore = true)
    GroupDTO toDTOGroupId(GroupEntity groupEntity);

    @Named("groupSet")
    default Set<GroupDTO> toDTOGroup(Set<GroupEntity> groups) {
        return groups.stream().map(this::toDTOGroupId).collect(Collectors.toSet());
    }

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fullname")
    @Mapping(target = "dateOfBirth")
    @Mapping(target = "positionTitle")
    @Mapping(target = "phoneNumber")
    @Mapping(target = "gender")
    void patch(@MappingTarget UserEntity oldUserEntity, UserDTO userDTO);

}
