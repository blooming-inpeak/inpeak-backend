package com.blooming.inpeak.support;

import com.blooming.inpeak.HealthcheckController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
    HealthcheckController.class
})
@WithMockUser
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvc mockMvc;
}
