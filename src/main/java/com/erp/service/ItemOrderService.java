package com.erp.service;

import com.erp.exception.ItemOrderNotFoundException;
import com.erp.exception.StoreItemNotFoundException;
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
        return repoOrder.findAllItemOrderList(PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()), null, null, null, null);
    }
    public Page<ItemOrderDTO> getItemOrderList(Integer pageNo, Long storeNo, String orderStatus, String startDate, String endDate) {
        LocalDateTime startDateTime = startDate.isEmpty() ? null : LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = endDate.isEmpty() ? null : (endDate.equals(startDate) ? LocalDate.parse(endDate).plusDays(1).atStartOfDay() : LocalDate.parse(endDate).atStartOfDay());
        String status = orderStatus.equals("전체") ? null : orderStatus;
        storeNo = storeNo == 0 ? null : storeNo;
        return repoOrder.findAllItemOrderList(PageRequest.of(pageNo, 10, Sort.by("itemOrderNo").descending()), storeNo, status, startDateTime, endDateTime);
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
            throw new ItemOrderNotFoundException(orderNo+"");
        }

        // 선택한 발주 요청 번호 데이터 상태 취소 변경
        itemOrder.setItemOrderStatus("취소"); // 상태 변경
        itemOrder.setProcessDatetime(new Timestamp(System.currentTimeMillis())); // 처리 시간
        repoOrder.save(itemOrder);
    }

    @Transactional
    public void receiveItem(Long itemOrderDetailNo){;
        ItemOrderDetail orderDetail = orderDetailRepo.findByItemOrderDetailNo(itemOrderDetailNo);

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

        if(storeItem == null) throw new StoreItemNotFoundException("직영점 품목 정보 조회 실패: " + orderDetail.getItemOrderNo().getStoreNo().getStoreNo());

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
                        .changeQuantity(orderDetail.getOrderDetailQuantity() * orderDetail.getItemNo().getConvertStock())
                        .changeReason("입고")
                        .currentQuantity(currentQuantity + (orderDetail.getOrderDetailQuantity() * orderDetail.getItemNo().getConvertStock()))
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
    private ItemOrder makeOrder(ItemOrderRequestDTO request, Long storeNo){
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

    public void requestItemOrder(ItemOrderRequestDTO request, Long storeNo) {

        ItemOrder itemOrder = makeOrder(request, storeNo);

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

    // 발주 제안
    public void proposeItemOrder(ProposalItemOrderDTO request) {

        request.getProposalList().forEach((proposalItem)->{
            ItemProposal proposal = ItemProposal.builder()
                    .managerId(Manager.builder().managerId(request.getManagerId()).build()) // 관리자id
                    .storeNo(Store.builder().storeNo(request.getStoreNo()).build()) // 매장id
                    .itemNo(Item.builder().itemNo(proposalItem.getItemNo()).build()) // 품목id
                    .proposalQuantity(proposalItem.getItemQuantity()) // 수량
                    .proposalReason(proposalItem.getProposalReason()) // 사유
                    .proposalDate(new Timestamp(System.currentTimeMillis()))
                    .build();
            proposalRepo.save(proposal);
        });

    }

    public void approveItemOrder(Long itemOrderNo, String managerId) {
        // 대기 중 발주 선택
        ItemOrder itemOrder = repoOrder.findById(itemOrderNo).orElseThrow(() -> new ItemOrderNotFoundException("존재하지 않는 발주입니다.: " + itemOrderNo) );

        // 선택한 발주 요청 번호 데이터 상태 승인 변경
        if(itemOrder != null){
            itemOrder.setManagerId(Manager.builder().managerId(managerId).build());
            itemOrder.setItemOrderStatus("승인"); // 상태 변경
            itemOrder.setProcessDatetime(new Timestamp(System.currentTimeMillis())); // 처리 시간
            repoOrder.save(itemOrder);
        }
    }
    
    public void declineItemOrder(Long itemOrderNo, String managerId){
        // 대기 중 발주 선택
        ItemOrder itemOrder = repoOrder.findById(itemOrderNo).orElseThrow(() -> new ItemOrderNotFoundException("존재하지 않는 발주입니다.: " + itemOrderNo) );

        // 선택한 발주 요청 번호 데이터 상태 승인 변경
        if(itemOrder != null){
            itemOrder.setManagerId(Manager.builder().managerId(managerId).build());
            itemOrder.setItemOrderStatus("반려"); // 상태 변경
            itemOrder.setProcessDatetime(new Timestamp(System.currentTimeMillis())); // 처리 시간
            repoOrder.save(itemOrder);
        }
    }
}
