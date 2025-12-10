package com.erp.repository;

import com.erp.repository.entity.MenuIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuIngredientRepository extends JpaRepository<MenuIngredient, Long> {
    List<MenuIngredient> findByMenu_MenuNo(Long menuNo);

    void deleteByMenu_MenuNo(Long menuNo);
}

