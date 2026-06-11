package org.example.learnhub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Learnhub API")
                                .version("1.0")
                                .description("Online course platform")
                                .contact(
                                        new Contact()
                                                .name("Bruno")
                                                .email("brunobaldiga@gmail.com")
                                )
                );
    }
}
