package vn.vnsky.bcss.admin.mapper;

import org.mapstruct.*;
import vn.vnsky.bcss.admin.dto.GroupDTO;
import vn.vnsky.bcss.admin.dto.RoleDTO;
import vn.vnsky.bcss.admin.dto.UserDTO;
import vn.vnsky.bcss.admin.dto.UserSuggestDTO;
import vn.vnsky.bcss.admin.entity.GroupEntity;
import vn.vnsky.bcss.admin.entity.RoleEntity;
import vn.vnsky.bcss.admin.entity.UserEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link GroupEntity} and its DTO {@link GroupDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface GroupMapper extends BaseMapper<GroupDTO, GroupEntity> {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Named("ignore")
    GroupDTO mapperToDTOIgnoring(GroupEntity groupEntity);

    @IterableMapping(qualifiedByName = "ignore")
    List<GroupDTO> mapperToDTOIgnoring(List<GroupEntity> groups);

    @Named("userDTOSet")
    @Mapping(target = "authorities", source = "authorities", ignore = true)
    default Set<UserDTO> toDTOGroup(Set<UserEntity> userEntities) {
        if (userEntities != null) {
            return userEntities.stream().filter(Objects::nonNull).map(this::toDTOUserId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Named("roleDTOSet")
    default Set<RoleDTO> toDTORole(Set<RoleEntity> roleEntities) {
        if (roleEntities != null) {
            return roleEntities.stream().filter(Objects::nonNull).map(this::toEntityRoleId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    @Override
    @Mapping(target = "users", source = "users", qualifiedByName = "userDTOSet")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "roleDTOSet")
    GroupDTO toDto(GroupEntity entity);

    @Override
    GroupEntity toEntity(GroupDTO dto);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "code", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    void patchEntity(@MappingTarget GroupEntity groupEntity, GroupDTO groupDTO);

    @Named("userIgnore")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "status", source = "status")
    @BeanMapping(ignoreByDefault = true)
    UserSuggestDTO toDTOUserId(UserEntity usersEntity);

    @Named("userIgnore")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserEntity toEntityUserId(UserDTO usersEntity);

    @Named("roleIgnore")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "code", source = "code")
    RoleDTO toEntityRoleId(RoleEntity roleEntity);

    @Named("userEntitySet")
    default Set<UserEntity> toEntityGroup(Set<UserDTO> user) {
        if (user != null) {
            return user.stream().filter(Objects::nonNull).map(this::toEntityUserId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    default GroupEntity fromId(String id) {
        if (id == null) {
            return null;
        }
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setId(id);
        return groupEntity;
    }
}
