package com.lms.repository;

import com.lms.entity.Content;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {
    List<Content> findByCourseIdOrderBySortOrderAsc(Long courseId);
}