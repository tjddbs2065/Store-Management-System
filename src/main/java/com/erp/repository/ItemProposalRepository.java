package com.erp.repository;

import com.erp.repository.entity.ItemProposal;
import com.erp.repository.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemProposalRepository extends JpaRepository<ItemProposal, Long> {
    List<ItemProposal> findByStoreNo(Store storeNo);

    List<ItemProposal> findByStoreNoAndResponseDateNull(Store storeNo);
}
