package com.erp.service;

import com.erp.controller.exception.ManagerException;
import com.erp.controller.exception.StoreNotFoundException;
import com.erp.dao.ManagerDAO;
import com.erp.dao.MenuDAO;
import com.erp.dao.StoreDAO;
import com.erp.dto.ManagerDTO;
import com.erp.dto.MenuDTO;
import com.erp.dto.StoreDTO;
import com.erp.repository.StoreMenuRepository;
import com.erp.repository.entity.Menu;
import com.erp.repository.entity.Store;
import com.erp.repository.entity.StoreMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final BCryptPasswordEncoder encoder;
    private final StoreDAO storeDAO;
    private final ManagerDAO managerDAO;
    private final MenuDAO menuDAO;
    private final StoreMenuRepository storeMenuRepository;


    public Page<StoreDTO> getStoresList(Integer pageNo, String address, String storeName, String managerName, String storeStatus) {
        int pageSize = 10;
        int offset = pageNo * pageSize;

        Map<String, Object> params = new HashMap<>();
        params.put("address", address);
        params.put("storeName", storeName);
        params.put("managerName", managerName);
        params.put("storeStatus", storeStatus);
        params.put("offset", offset);
        params.put("limit", pageSize);

        List<StoreDTO> content = storeDAO.getStoresList(params);
        long total = storeDAO.countStoreList(params);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return new PageImpl<>(content, pageable, total);
    }

    @Transactional
    public void addStore(ManagerDTO manager, StoreDTO store) {
        manager.setRole("ROLE_STORE");
        store.setStoreManagerId(manager.getManagerId());
        try{
            managerDAO.addManager(manager);
        }
        catch(Exception e){
            throw new ManagerException("직원 등록이 실패했습니다.");
        }

        try{
            storeDAO.addStore(store);
            addStoreMenu(store);
        }
        catch(Exception e){
            throw new StoreNotFoundException("직영점 등록이 실패했습니다.");
        }
    }

    private void addStoreMenu(StoreDTO store){
        List<MenuDTO> menuList = menuDAO.getMenuList(null, "출시 중");
        List<StoreMenu> storeMenuList = new ArrayList<>();

        menuList.forEach(menu -> {
            storeMenuList.add(StoreMenu.builder()
                    .store(Store.builder().storeNo(store.getStoreNo()).build())
                    .menu(Menu.builder().menuNo(menu.getMenuNo()).build())
                    .salesStatus("판매중단")
                    .build()
            );
        });
        storeMenuRepository.saveAll(storeMenuList);
    }

}
