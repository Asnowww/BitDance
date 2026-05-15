package com.bitdance.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 分组：把 /public、/auth、/h5、/merchant、/admin 五段路径分组，
 * 方便前端按角色筛选接口、Postman 按组导入。
 * 通过 /api/swagger-ui.html?urls.primaryName=... 切换查看。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bitDanceOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("BitDance API")
            .version("0.5.0")
            .description("BitDance 舞蹈学习与约练平台后端 API。"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("public").pathsToMatch("/public/**").build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("auth").pathsToMatch("/auth/**").build();
    }

    @Bean
    public GroupedOpenApi h5Api() {
        return GroupedOpenApi.builder()
            .group("h5").pathsToMatch("/h5/**").build();
    }

    @Bean
    public GroupedOpenApi merchantApi() {
        return GroupedOpenApi.builder()
            .group("merchant").pathsToMatch("/merchant/**").build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("admin").pathsToMatch("/admin/**").build();
    }
}
