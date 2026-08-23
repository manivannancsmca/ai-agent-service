package com.enterprise.product.repository;

import com.enterprise.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query("""
        SELECT p FROM ProductEntity p
        WHERE p.active = true
        AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY p.rating DESC, p.reviewCount DESC
    """)
    Page<ProductEntity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<ProductEntity> findByCategoryAndActiveTrueOrderByRatingDesc(
            String category, Pageable pageable);

    List<ProductEntity> findTopByCategoryAndActiveTrueOrderByRatingDescReviewCountDesc(
            String category, Pageable pageable);

    List<ProductEntity> findByActiveTrueOrderByRatingDescReviewCountDesc(Pageable pageable);

    @Query("SELECT DISTINCT p.category FROM ProductEntity p WHERE p.active = true ORDER BY p.category")
    List<String> findAllCategories();

    long countByCategoryAndActiveTrue(String category);
}
