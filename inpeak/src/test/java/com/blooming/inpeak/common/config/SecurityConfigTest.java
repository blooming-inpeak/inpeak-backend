package com.blooming.inpeak.common.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.blooming.inpeak.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("개발 환경에서 모든 엔드포인트 접근 가능 테스트")
    @Test
    void whenAccessingAnyEndpoint_thenReturns200() throws Exception {
        mockMvc.perform(get("/healthcheck"))
            .andExpect(status().isOk());
    }

    @DisplayName("존재하지 않는 경로는 404 응답을 반환해야 한다.")
    @Test
    void whenAccessingNonExistentPath_thenReturns404() throws Exception {
        mockMvc.perform(get("/non-existent"))
            .andExpect(status().isNotFound());
    }
}
