package com.ticket_online.global.config.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket_online.global.common.constants.UrlConstants;
import com.ticket_online.global.util.SpringEnvironmentUtil;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {
    private static final String SERVER_NAME = "ticket-online";
    private static final String API_TITLE = "ticket-online-api";
    private static final String API_DESCRIPTION = "ticket-online-api-dercription";
    private static final String GITHUB_URL = "https://github.com/camdao/ticket-online";

    private final SpringEnvironmentUtil springEnvironmentUtil;

    @Value("${swagger.version}")
    private String version;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(swaggerServers())
                .addSecurityItem(securityRequirement())
                .components(authSetting())
                .info(swaggerInfo());
    }

    private List<Server> swaggerServers() {
        Server server = new Server().url(getServerUrl()).description(API_DESCRIPTION);
        return List.of(server);
    }

    private String getServerUrl() {
        return switch (springEnvironmentUtil.getCurrentProfile()) {
            case "prod" -> UrlConstants.PROD_SERVER_URL.getValue();
            case "dev" -> UrlConstants.DEV_SERVER_URL.getValue();
            default -> UrlConstants.LOCAL_SERVER_URL.getValue();
        };
    }

    private Components authSetting() {
        return new Components()
                .addSecuritySchemes(
                        "accessToken",
                        new SecurityScheme()
                                .type(Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(In.HEADER)
                                .name("Authorization"));
    }

    private Info swaggerInfo() {
        License license = new License();
        license.setUrl(GITHUB_URL);
        license.setName(SERVER_NAME);

        return new Info()
                .version("v" + version)
                .title(API_TITLE)
                .description(API_DESCRIPTION)
                .license(license);
    }

    private SecurityRequirement securityRequirement() {
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("accessToken");
        return securityRequirement;
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        return new ModelResolver(objectMapper);
    }
}
