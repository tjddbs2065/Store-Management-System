package com.erp.service;

import com.erp.exception.StoreNotFoundException;
import com.erp.exception.ManagerException;
import com.erp.dao.ManagerDAO;
import com.erp.dao.StoreDAO;
import com.erp.dto.ManagerDTO;
import com.erp.dto.StoreDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int PAGE_SIZE = 10;

    private final ManagerDAO managerDAO;
    private final StoreDAO storeDAO;
    private final BCryptPasswordEncoder encoder;

    public void setManager(ManagerDTO managerDTO) {
        managerDTO.setPw(encoder.encode(managerDTO.getPw()));
        try {
            managerDAO.setManager(managerDTO);
        }
        catch (Exception e) {
            throw new ManagerException("직원을 정보를 변경할 수 없습니다.");
        }
    }

    public ManagerDTO getManager(String managerId) {
        ManagerDTO managerDTO = null;
        managerDTO = managerDAO.getManagerForLogin(managerId);
        if(managerDTO == null) {
            throw new ManagerException("직원을 찾을 수 없습니다.");
        }

        return managerDTO;
    }

    public void addManager(ManagerDTO managerDTO) {
        managerDTO.setPw(encoder.encode(managerDTO.getPw()));
        managerDTO.setRole("ROLE_MANAGER");
        try {
            managerDAO.addManager(managerDTO);
        }
        catch (Exception e) {
            throw new ManagerException("직원 등록 실패");
        }
    }

    public void checkManager(String managerId) {
        if(managerDAO.getManagerForLogin(managerId) == null) {
            throw new ManagerException("직원을 찾을 수 없습니다.");
        }
    }

    /**
     * 본사 직원 목록 (ROLE_MANAGER 만 조회)
     */
    public Page<ManagerDTO> getManagerMembers(int pageNo) {
        int safePage = Math.max(0, pageNo);

        List<ManagerDTO> all = managerDAO.getManagers(); // ROLE_MANAGER + del_date IS NULL
        return toPage(all, safePage);
    }

    /**
     * 직영점 직원 목록 (ROLE_STORE 매장 관리자)
     */
    public Page<StoreDTO> getStoreMembers(int pageNo) {
        int safePage = Math.max(0, pageNo);

        List<StoreDTO> all = storeDAO.getStoresByAdmin(); // ROLE_STORE + 매장 정보
        return toPage(all, safePage);
    }

    /**
     * 직영점 메뉴 판매 중지 권한 변경
     */
    public void setStoreMenuStopRole(long storeNo, String menuStopRole) {
        // Y / N 이외 값 들어와도 기본은 N 으로 처리
        String safeRole = "Y".equalsIgnoreCase(menuStopRole) ? "Y" : "N";

        StoreDTO dto = new StoreDTO();
        dto.setStoreNo(storeNo);
        dto.setMenuStopRole(safeRole);

        storeDAO.setStoreRole(dto);  // MyBatis update
    }

    public StoreDTO getStoreDetail(long storeNo) {
        StoreDTO store = storeDAO.getStoreDetail(storeNo);
        if (store == null) {
            throw new StoreNotFoundException("해당 직영점을 찾을 수 없습니다. storeNo=" + storeNo);
        }
        return store;
    }

    // 공통 페이지 변환
    private <T> Page<T> toPage(List<T> all, int pageNo) {
        Pageable pageable = PageRequest.of(pageNo, PAGE_SIZE);

        if (all == null || all.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int fromIndex = pageNo * PAGE_SIZE;
        if (fromIndex >= all.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, all.size());
        }

        int toIndex = Math.min(fromIndex + PAGE_SIZE, all.size());
        List<T> content = all.subList(fromIndex, toIndex);

        return new PageImpl<>(content, pageable, all.size());
    }
}
