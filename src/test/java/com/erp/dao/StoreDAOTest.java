package com.erp.dao;

import com.erp.dto.ManagerDTO;
import com.erp.dto.StoreDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class StoreDAOTest {
    @Autowired
    private StoreDAO storeDAO;
    @Autowired
    private ManagerDAO managerDAO;
    @Autowired
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void countStoresTest() { System.out.println(storeDAO.countStores()); }
    @Test
    void getStoresTest() {
        System.out.println(storeDAO.getStores());
    }
    @Test
    void getStoresByAddressTest() {
        System.out.println(storeDAO.getStoresByAddress("서울"));
    }
    @Test
    void getStoresByStoreNameTest() {
        System.out.println(storeDAO.getStoresByStoreName("가산"));
    }
    @Test
    void getStoresByManagerNameTest() {
        System.out.println(storeDAO.getStoresByManagerName("수정"));
    }
    @Test
    void getStoresByStoreStatusTest() {
        System.out.println(storeDAO.getStoresByStoreStatus("오픈준비"));
    }
    @Test
    void getStoreDetailTest() {
        System.out.println(storeDAO.getStoreDetail(1L));
    }

    @Test
    void addStoreTest() {
        ManagerDTO managerStore = ManagerDTO.builder()
                .managerId("testStore1")
                .pw(encoder.encode("1234"))
                .email("testStore1@pizza.com")
                .managerName("김민하")
                .phoneNumber("02-9988-1122")
                .delDate(null)
                .role("ROLE_STORE")
                .build();

        managerDAO.addManager(managerStore);
        System.out.println("매니저 등록 성공");
        StoreDTO store = StoreDTO.builder()
                .storeManagerId("testStore1")       // FK
                .storeStatus("영업중")
                .storeName("부천 리파인빌점")
                .address("경기 부천시 원미구 길주로 71")
                .latitude("37.506375390554")
                .longitude("126.75051640666")
                .openedDate(null)
                .closedDate(null)
                .storePhoneNumber("02-2222-5555")
                .storeImage(null)
                .openTime("10:00")
                .closeTime("22:00")
                .menuStopRole("N")
                .build();

        storeDAO.addStore(store);

        System.out.println("직영점 등록 성공");
    }

    @Test
    void setStoreTest() {
        ManagerDTO manager = new ManagerDTO();
        manager.setManagerId("kosta123");
        manager.setPw(encoder.encode("123"));
        manager.setEmail("storeManager3@test.com");
        manager.setManagerName("표수정");
        manager.setPhoneNumber("010-1331-2222");

        managerDAO.setManager(manager);

        StoreDTO store = new StoreDTO();
        store.setStoreNo(1);
        store.setStoreStatus("영업중");
        store.setAddress("서울특별시 금천구 가산디지털1로 128");
        store.setLatitude("37.47984");
        store.setLongitude("126.88279");
        store.setOpenedDate(null);
        store.setClosedDate(null);
        store.setStorePhoneNumber("02-567-8888");
        store.setStoreImage("default_store_image.png");   // 필요 없으면 null
        store.setOpenTime("10:00");
        store.setCloseTime("22:00");

        storeDAO.setStore(store);
    }

    @Test
    void setStoreRoleTest() {
        StoreDTO store = new StoreDTO();
        store.setStoreNo(15);
        store.setMenuStopRole("Y");
        storeDAO.setStoreRole(store);
    }

    @Test
    void getStoresByAdminTest() {
        System.out.println(storeDAO.getStoresByAdmin());
    }

    @Test
    void getStoreDetail(){

    }
}
