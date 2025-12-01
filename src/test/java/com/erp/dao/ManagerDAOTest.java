package com.erp.dao;

import com.erp.dto.ManagerDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class ManagerDAOTest {
    @Autowired
    ManagerDAO managerDAO;
    @Autowired
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    @Test
    void pw(){
        String raw = "123qwe";
        String raw2= "1q2w3e4r";
        String encode = encoder.encode(raw2);
        System.out.println(encode);
    }


    @Test
    void addManager() {
        ManagerDTO manager = ManagerDTO.builder()
                .managerId("testStore")
                .pw(encoder.encode("1234"))
                .email("test@test.com")
                .managerName("테스트직영점")
                .phoneNumber("010-1111-2222")
                .role("ROLE_STORE")
                .build();

        managerDAO.addManager(manager);
    }

    @Test
    void setManager() {
        ManagerDTO manager = new ManagerDTO();
        manager.setManagerId("test123");
        manager.setPw("1233");
        manager.setEmail("test12@test.com");
        manager.setManagerName("김삿갓");
        manager.setPhoneNumber("010-1111-2222");

        managerDAO.setManager(manager);

    }

    @Test
    void removeManager() {
        ManagerDTO manager = new ManagerDTO();
        manager.setManagerId("test1234");
        managerDAO.removeManager(manager);
    }

    @Test
    void getManagers() {
        System.out.println(managerDAO.getManagers());

    }

    @Test
    void getManagerDetail() {
        ManagerDTO manager = managerDAO.getManagerDetail("sh000642");
        System.out.println(manager);
    }
}
