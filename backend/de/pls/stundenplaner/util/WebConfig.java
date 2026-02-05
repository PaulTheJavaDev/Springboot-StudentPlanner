package de.pls.stundenplaner.util;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Helper class for synchronizing Back- and Frontend.
 */
@Configuration
public class WebConfig {

    /**
     * The host 3000 is linked to the Frontend and makes it possible to run Web Requests via Backend Endpoints.
     * @return A {@link WebMvcConfigurer}
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull @NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}
