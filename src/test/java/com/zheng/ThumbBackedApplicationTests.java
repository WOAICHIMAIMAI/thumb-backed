package com.zheng;

import cn.hutool.core.util.RandomUtil;
import com.zheng.model.entity.User;
import com.zheng.service.UserService;
import jakarta.annotation.Resource;
import org.apache.pulsar.shade.javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class ThumbBackendApplicationTests {

    @Resource
    private UserService userService;

    @Resource
    private MockMvc mockMvc;

    @Test
    void testLoginAndExportSessionToCsv() throws Exception {
        List<User> list = userService.list();

        try (PrintWriter writer = new PrintWriter(new FileWriter("session_output.csv", true))) {
            // 如果文件是第一次写入，可以加一个逻辑写表头
            writer.println("userId,sessionId,timestamp");

            for (User user : list) {
                long testUserId = user.getId();

                MvcResult result = mockMvc.perform(get("/user/login")
                                .param("userId", String.valueOf(testUserId))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn();

                List<String> setCookieHeaders = result.getResponse().getHeaders("Set-Cookie");
                assertThat(setCookieHeaders).isNotEmpty();

                String sessionId = setCookieHeaders.stream()
                        .filter(cookie -> cookie.startsWith("SESSION")) // Spring Session 默认是 SESSION（不是 JSESSIONID）
                        .map(cookie -> cookie.split(";")[0]) // SESSION=xxx
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No SESSION found in response"));

                String sessionValue = sessionId.split("=")[1];

                writer.printf("%d,%s,%s%n", testUserId, sessionValue, LocalDateTime.now());

                System.out.println("✅ 写入 CSV：" + testUserId + " -> " + sessionValue);
            }
        }
    }

    @Test
    void addUser() {
        for (int i = 0; i < 50000; i++) {
            User user = new User();
            user.setUsername(RandomUtil.randomString(6));
            userService.save(user);
        }
    }

}
