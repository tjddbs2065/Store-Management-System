package com.erp.service;

import com.erp.controller.exception.ItemOrderNotFoundException;
import com.erp.controller.exception.StoreItemNotFoundException;
import com.erp.dto.*;
import com.erp.repository.*;
import com.erp.repository.entity.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemOrderService {
    final private ItemOrderRepository repoOrder;
    final private ItemOrderDetailRepository orderDetailRepo;
    final private ItemProposalRepository proposalRepo;
    final private StoreRepository storeRepo;
    final private StoreItemRepository storeItemRepo;
    final private StoreStockRepository storeStockRepo;
    final private ItemRepository itemRepo;

    @Transactional(readOnly = true)
    public List<ItemOrderDTO> getAllItemOrder() {
        List<ItemOrderDTO> itemOrder = new ArrayList<>();
        repoOrder.findAll().forEach(order -> itemOrder.add(ItemOrderDTO.toDTO(order)));

        return itemOrder;
    }

    public Page<ItemOrderDTO> getItemOrderList(Integer pageNo) {
        return repoOrder.findAllItemOrderList(PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()), null, null, null);
    }
    public Page<ItemOrderDTO> getItemOrderList(Integer pageNo, String orderStatus, String startDate, String endDate) {
        LocalDateTime startDateTime = startDate.isEmpty() ? null : LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = endDate.isEmpty() ? null : LocalDate.parse(endDate).atStartOfDay();

        if(startDate.equals(endDate)) {endDateTime = endDateTime.plusDays(1);}
        String status = orderStatus.equals("전체") ? "" : orderStatus;

        return repoOrder.findAllItemOrderList(PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()), status, startDateTime, endDateTime);
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
        ItemOrder itemOrder = repoOrder.findByItemOrderNo(orderNo);

        if(itemOrder == null){
            throw new ItemOrderNotFoundException(orderNo);
        }

        // 선택한 발주 요청 번호 데이터 상태 취소 변경
        itemOrder.setItemOrderStatus("취소"); // 상태 변경
        itemOrder.setProcessDatetime(new Timestamp(System.currentTimeMillis())); // 처리 시간
        repoOrder.save(itemOrder);
    }

    @Transactional
    public void receiveItem(Long itemOrderDetailNo){;
        ItemOrderDetail orderDetail = orderDetailRepo.findByItemOrderDetailNo(itemOrderDetailNo);


        System.out.println(orderDetail.getItemOrderNo().getStoreNo().getStoreNo() +" - "+ orderDetail.getItemNo().getItemNo());
        // 재고 수량 변경
        StoreItem storeItem = null;
        List<StoreItem> storeItemList = storeItemRepo.findByStoreNoAndItemNo(orderDetail.getItemOrderNo().getStoreNo().getStoreNo(), orderDetail.getItemNo().getItemNo()); // 매장 품목 정보 획득
        if(storeItemList.isEmpty()){
            storeItem = storeItemRepo.save(StoreItem.builder()
                    .itemNo(orderDetail.getItemNo().getItemNo())
                    .storeNo(orderDetail.getItemOrderNo().getStoreNo().getStoreNo())
                    .build());
        }
        else storeItem = storeItemList.get(0);

        if(storeItem == null) throw new StoreItemNotFoundException(orderDetail.getItemOrderNo().getStoreNo().getStoreNo());

        StoreStock storeStock = storeStockRepo.findFirstByStoreItemNoOrderByStoreStockNoDesc(storeItem.getStoreItemNo()); // 현재 매장 품목의 수량 데이터 획득

        // 현재 보유 중인 품목이 아닌 경우 0부터 추가
        Integer currentQuantity = 0;
        if(storeStock != null){
            currentQuantity = storeStock.getCurrentQuantity();
        }
        
        // 재고 변동사항 등록(추가)
        storeStockRepo.save(
                StoreStock.builder()
                        .storeItemNo(storeItem.getStoreItemNo())
                        .changeDatetime(new Timestamp(System.currentTimeMillis()))
                        .changeQuantity(orderDetail.getOrderDetailQuantity())
                        .changeReason("입고")
                        .currentQuantity(currentQuantity + orderDetail.getOrderDetailQuantity())
                        .build()
        ); // 입고 수량 반영

        // 발주 상세 상태 변경(입고 완료)
        orderDetail.setReceiveDatetime(new Timestamp(System.currentTimeMillis()));
        orderDetail.setReceiveQuantity(orderDetail.getOrderDetailQuantity());
        orderDetailRepo.save(orderDetail);
    }

    @Transactional
    public List<ItemProposalDTO> getItemProposalHistoryByStoreNo(Long storeNo) {
        List<ItemProposalDTO> list = new ArrayList<>();
        proposalRepo.findByStoreNo(Store.builder().storeNo(storeNo).build()).forEach(prop -> {
            list.add(ItemProposalDTO.from(prop));
        });
        return list;
    }

    @Transactional
    public List<ItemProposalDTO> getItemProposalByStoreNo(Long storeNo) {
        List<ItemProposalDTO> list = new ArrayList<>();
        proposalRepo.findByStoreNoAndResponseDateNull(Store.builder().storeNo(storeNo).build()).forEach(prop -> {
            list.add(ItemProposalDTO.from(prop));
        });
        return list;
    }

    public void responseProposal(Long proposalNo) {
        ItemProposal proposal = proposalRepo.findById(proposalNo).orElseThrow(()-> new EntityNotFoundException("ItemProposal not found"));
        proposal.setResponseDate(new Timestamp(System.currentTimeMillis()));
        proposalRepo.save(proposal);
    }

    public List<ItemStoreQuantityDTO> itemList(Long storeNo){
        return itemRepo.findAllWithQuantity(storeNo);
    }

    // 발주 요청 생성(관리자)
    private ItemOrder makeOrder(ItemOrderRequestDTO request){
        Long storeNo = request.getStoreNo();
        Integer totalItem = request.getTotalItem();
        Integer totalAmount = request.getTotalAmount();

        ItemOrder newOrder = ItemOrder.builder() // 발주 요청 발생
                .storeNo(Store.builder().storeNo(storeNo).build()) // 요청한 직영점 정보
                .totalItem(totalItem)
                .totalAmount(totalAmount)
                .itemOrderStatus("대기")
                .requestDatetime(new Timestamp(System.currentTimeMillis()))
                .build();
        return repoOrder.save(newOrder);
    }

    public void requestItemOrder(ItemOrderRequestDTO request) {

        ItemOrder itemOrder = makeOrder(request);

        request.getOrderList().forEach((item)->{
            ItemOrderDetail orderDetail = ItemOrderDetail
                    .builder()
                    .itemNo(Item.builder().itemNo(item.getItemNo()).build())
                    .itemOrderNo(itemOrder)
                    .orderDetailQuantity(item.getItemQuantity())
                    .orderDetailPrice(item.getItemOrderPrice())
                    .build();

            orderDetailRepo.save(orderDetail);
        });
    }
}
