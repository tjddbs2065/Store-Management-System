package com.erp.service;

import com.erp.dto.StoreDTO;
import com.erp.dto.ManagerDTO;
import com.erp.dto.StoreDTO;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootTest
@Transactional
public class StoreServiceTest {
    @Autowired
    StoreService storeService;
    @Autowired
    BCryptPasswordEncoder encoder;

    @Test
    void getStoresTest(){
        Page<StoreDTO> result =  storeService.getStoresList(0, null, null, null, null);
        result.forEach(System.out::println);
    }

    @Test
    void getStoresByNameTest(){
        Page<StoreDTO> result =  storeService.getStoresList(0, null, null, "표", null);
        result.forEach(System.out::println);
    }

    @Test
    void addStoreTest(){
        ManagerDTO managerStore = ManagerDTO.builder()
                .managerId("testStore1")
                .pw(encoder.encode("1234"))
                .email("testStore1@pizza.com")
                .managerName("김민하")
                .phoneNumber("02-9988-1122")
                .delDate(null)
                .role("ROLE_STORE")
                .build();

        StoreDTO store = StoreDTO.builder()
                .storeManagerId(managerStore.getManagerId())       // FK
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

        storeService.addStore(managerStore, store);
    }
}
