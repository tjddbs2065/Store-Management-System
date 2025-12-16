package com.erp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class AddStoreRequestDTO {
    private Map<String, String> manager;
    private Map<String, String> store;
}
