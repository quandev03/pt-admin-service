package vn.vnsky.bcss.admin.mapper;

import java.util.List;
import java.util.Set;

public interface BaseMapper<D, E> {

    E toEntity(D dto);

    D toDto(E entity);

    List<E> toEntity(List<D> dtoList);

    List<D> toDto(List<E> entityList);

    Set<E> toEntity(Set<D> dtoSet);

    Set<D> toDto(Set<E> entitySet);

}
