
package com.los_maestros.contenido.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.los_maestros.contenido.model.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

}
