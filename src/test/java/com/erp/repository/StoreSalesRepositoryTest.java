package com.erp.repository;

import com.erp.dao.ManagerDAO;
import com.erp.dto.ManagerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;


import java.sql.Date;
import java.time.LocalDate;

@SpringBootTest
@Transactional
public class StoreSalesRepositoryTest {
    @Autowired
    private StoreSalesRepository storeSalesRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private ManagerDAO managerDAO;

    @Test
    void encodeTest(){
        String raw = "123456";
        String encoded = passwordEncoder.encode(raw);
        System.out.println(encoded);
    }

    @Test
    void check_db_password_matches() {
        // 여기 managerId는 네가 로그인 폼에서 치는 아이디로 바꿔라.
        String loginId = "testid";

        ManagerDTO manager = managerDAO.getManagerForLogin(loginId);

        System.out.println("manager = " + manager);
        System.out.println("DB pw   = [" + manager.getPw() + "]");

        boolean matches = passwordEncoder.matches("123qwe", manager.getPw());
        System.out.println("matches(123qwe, DB pw) = " + matches);
    }


    @Test
    public void findSalesListTest(){
        Pageable pageable = PageRequest.of(0, 10);
        System.out.println(storeSalesRepository.findSalesList(null,null,null,pageable).getContent());
    }

    @Test
    public void findBySalesDateBetweenTest() {
        System.out.println(storeSalesRepository.findBySalesDateBetween(LocalDate.of(2024,1,9), LocalDate.of(2024,1,14)));
    }

    @Test
    public void findByStore_StoreNoAndSalesDateBetweenTest() {
        System.out.println(storeSalesRepository.findByStore_StoreNoAndSalesDateBetween(1L, LocalDate.of(2025,12,27), LocalDate.of(2025,12,30)));
    }
}
