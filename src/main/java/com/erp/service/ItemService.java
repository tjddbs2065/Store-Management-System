package com.erp.service;

import com.erp.dao.ItemDAO;
import com.erp.dto.ItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemDAO itemDAO;

    public ItemDTO getDetail(Long itemNo) { return itemDAO.getItemDetail(itemNo); }

    public List<ItemDTO> getItemList(String itemCategory, String ingredientName, String itemCode) {
        return itemDAO.getItemList(itemCategory, ingredientName, itemCode); }

    public List<ItemDTO> getItemsByCategory(String category){ return itemDAO.getByCategory(category); }
    public List<ItemDTO> getItemsByItemName(String name){ return itemDAO.getByItemName(name); }
    public List<ItemDTO> getItemsByItemCode(String code){ return itemDAO.getByItemCode(code); }
    public List<ItemDTO> getItemsByIngredient(String ing){ return itemDAO.getByIngredient(ing); }

    @Transactional
    public int addItem(ItemDTO dto){ return itemDAO.addItem(dto); }

    @Transactional
    public int setItem(ItemDTO dto){ return itemDAO.setItem(dto); }

    @Transactional
    public int removeItem(Long itemNo){ return itemDAO.removeItem(itemNo); }
}
