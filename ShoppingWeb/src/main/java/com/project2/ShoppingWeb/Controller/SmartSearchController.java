package com.project2.ShoppingWeb.Controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.project2.ShoppingWeb.Service.SmartSearchService;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import com.project2.ShoppingWeb.DTO.SmartSearchResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
@RestController
@RequestMapping("/api/products")
@Slf4j
public class SmartSearchController {
    private final SmartSearchService smartSearchService;

    public SmartSearchController(SmartSearchService smartSearchService) {
        this.smartSearchService = smartSearchService;
    }

    @PostMapping("/smart-search")
    public Mono<ResponseEntity<SmartSearchResponse>> smartSearch(@RequestBody String searchQuery) {
        log.info("Nhận yêu cầu tìm kiếm: {}", searchQuery);
        return smartSearchService.searchProducts(searchQuery)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> 
                    log.info("Hoàn thành tìm kiếm, tìm thấy {} kết quả", 
                        response.getBody().getProducts().size()))
                .onErrorResume(e -> {
                    log.error("Lỗi khi xử lý tìm kiếm", e);
                    return Mono.just(ResponseEntity
                        .internalServerError()
                        .body(new SmartSearchResponse(
                            List.of(),
                            "Có lỗi xảy ra: " + e.getMessage(),
                            null
                        )));
                });
    }
}
