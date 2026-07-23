package domain.repository;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T> {
    Optional<T> findById(String id);
    List<T> findAll();
    T save(T entity);
    void delete(String id);
    boolean existsById(String id);
}
