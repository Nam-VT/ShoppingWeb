package com.project2.ShoppingWeb.Controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.project2.ShoppingWeb.Service.SmartSearchService;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import com.project2.ShoppingWeb.DTO.SmartSearchRequest;
import com.project2.ShoppingWeb.DTO.SmartSearchResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@Slf4j
public class SmartSearchController {
    private final SmartSearchService smartSearchService;

    public SmartSearchController(SmartSearchService smartSearchService) {
        this.smartSearchService = smartSearchService;
    }

    @PostMapping("/smart-search")
    public Mono<ResponseEntity<SmartSearchResponse>> smartSearch(@RequestBody SmartSearchRequest request) {
        log.info("Nhận yêu cầu tìm kiếm: {}", request.getSearchQuery());
        return smartSearchService.searchProducts(request.getSearchQuery())
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
                            "Đã xảy ra lỗi trong hệ thống. Vui lòng thử lại sau!",
                            null
                        )));
                });
    }
}
