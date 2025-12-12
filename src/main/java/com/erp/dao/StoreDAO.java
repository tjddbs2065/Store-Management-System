package com.erp.dao;

import com.erp.dto.StoreDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface StoreDAO {
    List<StoreDTO> getActiveStores();

    int countStores();
    List<StoreDTO> getStores();
    List<StoreDTO> getStoresByAddress(String address);
    List<StoreDTO> getStoresByStoreName(String storeName);
    List<StoreDTO> getStoresByManagerName(String managerName);
    List<StoreDTO> getStoresByStoreStatus(String storeStatus);
    StoreDTO getStoreDetail(Long storeNo);
    void addStore(StoreDTO store);
    void setStore(StoreDTO store);
    void setStoreRole(StoreDTO store);
    List<StoreDTO> getStoresByAdmin();
    Long getStoreNoByManager(String managerId);
    List<StoreDTO> getStoresList(Map<String, Object> params);
    Long countStoreList(Map<String, Object> params);

}
