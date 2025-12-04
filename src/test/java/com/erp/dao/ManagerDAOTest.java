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
                .managerId("testAdmin")
                .pw(encoder.encode("1234"))
                .email("test@test.com")
                .managerName("테스트관리자")
                .phoneNumber("010-1111-2222")
                .role("ROLE_ADMIN")
                .build();

//        if ("test123".equals(manager.getManagerId())){
//            System.out.println("아이디 중복");
//            return;
//        }
        managerDAO.addManager(manager);
    }

    @Test
    void setManager() {
        ManagerDTO manager = new ManagerDTO();
        manager.setManagerId("kosta123");
        manager.setPw(encoder.encode("123"));
        manager.setEmail("kosta123@gmail.com");
        manager.setManagerName("표수정");
        manager.setPhoneNumber("010-9876-5432");

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
