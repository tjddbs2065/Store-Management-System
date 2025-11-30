package com.erp.service;

import com.erp.controller.exception.ItemOrderNotFoundException;
import com.erp.dto.ItemOrderDetailDTO;
import com.erp.repository.*;
import com.erp.dto.ItemOrderDTO;
import com.erp.repository.entity.ItemOrder;
import com.erp.repository.entity.ItemOrderDetail;
import com.erp.repository.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemOrderService {
    final private ItemOrderRepository repoOrder;
    final private ItemOrderDetailRepository orderDetailRepo;
    final private ItemProposalRepository proposalRepo;
    final private StoreRepository storeRepo;

    @Transactional(readOnly = true)
    public List<ItemOrderDTO> getAllItemOrder() {
        List<ItemOrderDTO> itemOrder = new ArrayList<>();
        repoOrder.findAll().forEach(order -> itemOrder.add(ItemOrderDTO.toDTO(order)));

        return itemOrder;
    }

    public Page<ItemOrderDTO> getItemOrderList(Integer pageNo) {
        return repoOrder.findAllItemOrderList(PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()));
    }

    public Page<ItemOrderDTO> getItemOrderListByDate(Integer pageNo, LocalDate startDate, LocalDate endDate) {
        return repoOrder.findByRequestDatetimeBetween(startDate.atStartOfDay(), endDate.atStartOfDay(), PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()));
    }

    public Page<ItemOrderDTO> getItemOrderListByDay(Integer pageNo, Integer day){
        // 일: 1, 월: 2, 화: 3, 수: 4, 목: 5, 금: 6, 토: 7
        return repoOrder.findByRequestDatetimeDay(day,PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()));
    }

    public Page<ItemOrderDTO> getItemOrderListByStore(Integer pageNo, Long storeNo){
        return repoOrder.findByStoreNo(Store.builder().storeNo(storeNo).build(), PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()));
    }

    public Page<ItemOrderDTO> getItemOrderListByStatus(Integer pageNo, String status){
        return repoOrder.findByItemOrderStatus(status, PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()));
    }

    public List<ItemOrderDetailDTO> getItemOrderDetailByOrderNo(Long itemOrderNo) {
        return orderDetailRepo.findAllItemOrderDetail(ItemOrder.builder().itemOrderNo(itemOrderNo).build());
    }

    public void cancelItemOrder(Long orderNo) {
        // 대기 중 발주 선택
        boolean result = false;
        ItemOrder itemOrder = repoOrder.findByItemOrderStatusAndStoreNo(
                "대기",
                Store.builder().storeNo(orderNo).build() // 직영점 번호 지정
        ).get(0);

        if(itemOrder == null){
            throw new ItemOrderNotFoundException(orderNo);
        }

        // 선택한 발주 요청 번호 데이터 상태 취소 변경
        itemOrder.setItemOrderStatus("취소"); // 상태 변경
        itemOrder.setProcessDatetime(new Timestamp(System.currentTimeMillis())); // 처리 시간
    }
}
