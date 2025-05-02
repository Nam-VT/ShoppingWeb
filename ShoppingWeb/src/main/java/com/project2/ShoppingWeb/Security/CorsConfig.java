package com.project2.ShoppingWeb.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer webMvcConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedOrigins("http://localhost:3000") // Chỉ cho phép React kết nối
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization", "Access-Control-Allow-Origin")
                        .allowCredentials(true) // Cho phép gửi cookie nếu cần
                        .maxAge(3600); // Cache CORS preflight response
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations("file:./src/main/resources/uploads/");
            }
        };
    }
    
    // Thêm CorsFilter để đảm bảo CORS được áp dụng trước các filter khác
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:3000");
        config.addAllowedHeader("*");
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Access-Control-Allow-Origin");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
