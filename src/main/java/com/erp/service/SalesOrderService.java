//package com.erp.service;
//
//import com.erp.dto.SalesOrderDTO;
//import com.erp.dto.SalesOrderDetailDTO;
//import com.erp.dto.StoreMenuDTO;
//import com.erp.repository.*;
//import com.erp.repository.entity.*;
//import com.erp.specification.SalesOrderSpec;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class SalesOrderService {
//    private final SalesOrderRepository salesOrderRepository;
//    private final StoreOrderDetailRepository storeOrderDetailRepository;
//    private final StoreRepository storeRepository;
//    private final StoreMenuRepository storeMenuRepository;
//    private final MenuIngredientRepository menuIngredientRepository;
//    private final StoreItemRepository storeItemRepository;
//    private final StoreStockRepository storeStockRepository;
//
//    @Transactional
//    public SalesOrderDTO addSalesOrder(Long storeNo, List<StoreMenuDTO> menuDTOList, List<SalesOrderDetailDTO> detailDTOList) {
//        Store store = storeRepository.findById(storeNo).orElse(null);
//
//        int totalOrderAmount = 0;
//        for (SalesOrderDetailDTO dto : detailDTOList) {
//            totalOrderAmount += dto.getPrice() * dto.getCount();
//        }
//
//        SalesOrder salesOrder = SalesOrder.builder()
//                .store(store)
//                .salesOrderDatetime(LocalDateTime.now())
//                .salesOrderAmount(totalOrderAmount)
//                .build();
//
//        salesOrderRepository.save(salesOrder);
//
//        for (int i = 0; i < menuDTOList.size(); i++) {
//            StoreMenuDTO storeMenuDTO = menuDTOList.get(i);
//            SalesOrderDetailDTO salesOrderDetailDTO = detailDTOList.get(i);
//
//            StoreMenu storeMenu = storeMenuRepository.findById(storeMenuDTO.getStoreMenuNo()).orElse(null);
//            StoreOrderDetail orderDetail = StoreOrderDetail.builder()
//                    .salesOrder(salesOrder)
//                    .storeMenu(storeMenu)
//                    .menuCount(salesOrderDetailDTO.getCount())
//                    .menuPrice(salesOrderDetailDTO.getPrice())
//                    .build();
//
//            salesOrder.addOrderDetail(orderDetail);
//
//
//            List<MenuIngredient> ingredientList = menuIngredientRepository.findByMenu_MenuNo(storeMenu.getMenu().getMenuNo());
//
//            for (MenuIngredient ingredient : ingredientList) {
//                Long itemNo = ingredient.getItem().getItemNo();
//                int needQty = ingredient.getIngredientQuantity();
//                int totalQty = needQty * salesOrderDetailDTO.getCount();
//
//                StoreItem storeItem = storeItemRepository
//                        .findByStoreNoAndItemNo(storeNo, itemNo)
//                        .stream().findFirst().orElse(null);
//
//                StoreStock latestStock = storeStockRepository.findFirstByStoreItemNoOrderByStoreStockNoDesc(storeItem.getStoreItemNo());
//
//                int previousQty = (latestStock == null) ? 0 : latestStock.getCurrentQuantity();
//                int updatedQty = previousQty - totalQty;
//
//                StoreStock newStock = StoreStock.builder()
//                        .storeItemNo(storeItem.getStoreItemNo())
//                        .changeQuantity(-totalQty)
//                        .currentQuantity(updatedQty)
//                        .changeReason("판매")
//                        .build();
//
//                storeStockRepository.save(newStock);
//            }
//        }
//        return SalesOrderDTO.fromEntity(salesOrder);
//
//    }
//
//    @Transactional(readOnly = true)
//    public Page<SalesOrderDTO> getSalesOrderList(Integer pageNo, LocalDate date, String storeName) {
//        Specification<SalesOrder> spec = Specification.where(null);
//
//        spec = spec.and(SalesOrderSpec.findByDate(date));
//        spec = spec.and(SalesOrderSpec.findByStoreName(storeName));
//
//        PageRequest pageable = PageRequest.of(
//                pageNo,
//                10,
//                Sort.by(Sort.Direction.DESC, "salesOrderNo") // ★ 내림차순 정렬 추가!
//        );
//
//        Page<SalesOrder> page = salesOrderRepository.findAll(spec, pageable);
//
//        return page.map(SalesOrderDTO::fromEntity);
//    }
//
//    @Transactional
//    public Map<String, Object> getSalesOrderDetail(Long salesOrderNo) {
//        List<StoreOrderDetail> list = storeOrderDetailRepository.getStoreOrderDetail(salesOrderNo);
//
//        if (list.isEmpty()) {
//            return null;
//        }
//        SalesOrder first = list.get(0).getSalesOrder();
//
//       List<SalesOrderDetailDTO> menuList = list.stream().map(
//               storeOrderDetail -> new SalesOrderDetailDTO(
//                       storeOrderDetail.getStoreMenu().getMenu().getMenuName(),
//                       storeOrderDetail.getStoreMenu().getMenu().getSize(),
//                       storeOrderDetail.getMenuPrice(),
//                       storeOrderDetail.getMenuCount(),
//                       storeOrderDetail.getMenuPrice() * storeOrderDetail.getMenuCount()
//               )).toList();
//
//       int totalPrice = menuList.stream()
//               .mapToInt(SalesOrderDetailDTO::getTotalPrice)
//               .sum();
//
//       return Map.of(
//               "salesOrderNo", first.getSalesOrderNo(),
//               "salesOrderDatetime", first.getSalesOrderDatetime(),
//               "storeName", first.getStore().getStoreName(),
//               "menuList", menuList,
//               "totalPrice", totalPrice
//       );
//    }
//}
