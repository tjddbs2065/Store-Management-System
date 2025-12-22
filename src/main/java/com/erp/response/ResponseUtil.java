package com.erp.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void writeJson(
            HttpServletResponse response,
            int status,
            Object body
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        mapper.writeValue(response.getOutputStream(), body);
    }
}
