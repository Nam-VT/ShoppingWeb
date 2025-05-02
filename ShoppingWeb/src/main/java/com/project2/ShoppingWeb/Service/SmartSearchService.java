package com.project2.ShoppingWeb.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project2.ShoppingWeb.DTO.GroqResponse;
import com.project2.ShoppingWeb.DTO.SearchCriteria;
import com.project2.ShoppingWeb.DTO.SmartSearchResponse;
import com.project2.ShoppingWeb.Entity.Category;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Repository.ProductRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;

@Service
@Slf4j
public class SmartSearchService {
    private final WebClient webClient;
    private final ProductRepo productRepository;
    private final ObjectMapper objectMapper;

    public SmartSearchService(
            @Value("${groq.api.key}") String apiKey,
            ProductRepo productRepository,
            ObjectMapper objectMapper
    ) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    private String createSystemPrompt() {
        return """
            Bạn là AI assistant chuyên phân tích yêu cầu tìm kiếm sản phẩm.
            Nếu không thể trích xuất được bất kỳ trường nào, hãy trả về {"valid": false}.
            Nếu phân tích được, trả về JSON với các trường như sau:
            {
                "valid": true,
                "name": ...,
                "description": ...,
                "priceRange": { "min": ..., "max": ... },
                "categories": [...],
                "availability": ...,
                "sortBy": ...,
                "keywords": [...]
            }
            Quy tắc phân tích:
            1. Tên sản phẩm và danh mục:
               - Chỉ trích xuất từ khóa chính xác (ví dụ: điện thoại, laptop, tai nghe)
               - Không thêm các từ phụ hoặc tính từ vào tên
               - Danh mục phải khớp chính xác với DB (điện thoại, laptop, phụ kiện)
            2. Khoảng giá (priceRange):
               - Số cụ thể: "5 triệu" -> min: null, max: 5000000
               - "dưới X" -> min: null, max: X
               - "trên X" -> min: X, max: null
               - "từ X đến Y" -> min: X, max: Y
               - "giá rẻ" -> max: 500000
               - "tầm trung" -> min: 500000, max: 2000000
               - "cao cấp" -> min: 2000000, max: null
            3. Tồn kho (availability):
               - Mặc định: null (tìm tất cả)
               - "còn hàng" -> true
               - "hết hàng" -> false
            4. Sắp xếp (sortBy):
               - "giá tăng/rẻ nhất" -> "price_asc"
               - "giá giảm/đắt nhất" -> "price_desc"
               - "bán chạy/phổ biến" -> "sold"
               - "mới nhất" -> "newest"
            Chỉ trả về JSON, không thêm giải thích.
            """;
    }

    public Mono<SmartSearchResponse> searchProducts(String searchQuery) {
        log.info("Đang xử lý tìm kiếm với query: {}", searchQuery);

        Map<String, Object> body = Map.of(
                "model", "llama3-70b-8192",
                "messages", List.of(
                        Map.of("role", "system", "content", createSystemPrompt()),
                        Map.of("role", "user", "content", searchQuery)
                ),
                "temperature", 0.7,
                "response_format", Map.of("type", "json_object")
        );

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GroqResponse.class)
                .map(response -> {
                    String aiResponse = response.getChoices().get(0).getMessage().getContent();
                    log.info("AI Response: {}", aiResponse);
                    
                    try {
                        SearchCriteria criteria = objectMapper.readValue(aiResponse, SearchCriteria.class);
                        log.info("Parsed criteria: {}", criteria);

                        // Kiểm tra valid hoặc tất cả trường đều null/rỗng
                        boolean isInvalid = Boolean.FALSE.equals(criteria.getValid())
                            || (
                                (criteria.getName() == null || criteria.getName().isBlank())
                                && (criteria.getDescription() == null || criteria.getDescription().isBlank())
                                && (criteria.getCategories() == null || criteria.getCategories().isEmpty())
                                && (criteria.getPriceRange() == null || 
                                    (criteria.getPriceRange().getMin() == null && criteria.getPriceRange().getMax() == null))
                            );

                        if (isInvalid) {
                            return new SmartSearchResponse(
                                List.of(),
                                "Không thể phân tích yêu cầu tìm kiếm, vui lòng nhập chi tiết hơn.",
                                criteria
                            );
                        }

                        List<Product> products = findProductsByCriteria(criteria);
                        
                        return new SmartSearchResponse(
                            products,
                            generateSearchExplanation(criteria, products.size()),
                            criteria
                        );
                    } catch (Exception e) {
                        log.error("Lỗi khi xử lý response từ AI", e);
                        throw new RuntimeException("Không thể xử lý kết quả tìm kiếm", e);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Lỗi khi tìm kiếm", e);
                    return Mono.just(new SmartSearchResponse(
                        List.of(),
                        "Có lỗi xảy ra khi tìm kiếm: " + e.getMessage(),
                        null
                    ));
                });
    }

    private String generateSearchExplanation(SearchCriteria criteria, int productCount) {
        if (productCount == 0) {
            StringBuilder notFoundReason = new StringBuilder("Không tìm thấy sản phẩm");
            
            if (criteria.getName() != null) {
                notFoundReason.append(" với tên '").append(criteria.getName()).append("'");
            }
            
            if (criteria.getPriceRange() != null) {
                if (criteria.getPriceRange().getMin() != null) {
                    notFoundReason.append(", giá từ ").append(formatPrice(criteria.getPriceRange().getMin()));
                }
                if (criteria.getPriceRange().getMax() != null) {
                    notFoundReason.append(" đến ").append(formatPrice(criteria.getPriceRange().getMax()));
                }
            }
            
            if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
                notFoundReason.append(" trong danh mục ").append(String.join(", ", criteria.getCategories()));
            }
            
            return notFoundReason.toString() + ". Vui lòng thử với tiêu chí khác.";
        }

        StringBuilder explanation = new StringBuilder("Tìm thấy ")
            .append(productCount).append(" sản phẩm");
        
        if (criteria.getName() != null) {
            explanation.append(" với tên '").append(criteria.getName()).append("'");
        }
        
        if (criteria.getPriceRange() != null) {
            if (criteria.getPriceRange().getMin() != null) {
                explanation.append(", giá từ ").append(formatPrice(criteria.getPriceRange().getMin()));
            }
            if (criteria.getPriceRange().getMax() != null) {
                explanation.append(" đến ").append(formatPrice(criteria.getPriceRange().getMax()));
            }
        }

        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            explanation.append(" trong danh mục ").append(String.join(", ", criteria.getCategories()));
        }

        if (Boolean.TRUE.equals(criteria.getAvailability())) {
            explanation.append(" (còn hàng)");
        }

        if (criteria.getSortBy() != null) {
            explanation.append(", sắp xếp theo ");
            switch (criteria.getSortBy()) {
                case "price_asc" -> explanation.append("giá tăng dần");
                case "price_desc" -> explanation.append("giá giảm dần");
                case "sold" -> explanation.append("bán chạy nhất");
                case "newest" -> explanation.append("mới nhất");
            }
        }

        return explanation.toString();
    }

    private String formatPrice(Long price) {
        if (price >= 1000000) {
            return String.format("%.1f triệu", price / 1000000.0);
        }
        return String.format("%,d đ", price);
    }

    private List<Product> findProductsByCriteria(SearchCriteria criteria) {
        Specification<Product> spec = Specification.where(null);

        // Tìm theo tên sản phẩm
        if (criteria.getName() != null) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("name")), 
                    "%" + criteria.getName().toLowerCase() + "%"));
        }

        // Tìm theo mô tả
        if (criteria.getDescription() != null) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("description")), 
                    "%" + criteria.getDescription().toLowerCase() + "%"));
        }

        // Tìm theo khoảng giá
        if (criteria.getPriceRange() != null) {
            if (criteria.getPriceRange().getMin() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.greaterThanOrEqualTo(root.get("price"), 
                        BigDecimal.valueOf(criteria.getPriceRange().getMin())));
            }
            if (criteria.getPriceRange().getMax() != null) {
                spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("price"), 
                        BigDecimal.valueOf(criteria.getPriceRange().getMax())));
            }
        }

        // Tìm theo danh mục
        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<Product, Category> categoryJoin = root.join("categories");
                return categoryJoin.get("name").in(criteria.getCategories());
            });
        }

        // Kiểm tra tồn kho
        if (Boolean.TRUE.equals(criteria.getAvailability())) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThan(root.get("stockQuantity"), 0));
        }

        // Sắp xếp kết quả
        List<Product> products = productRepository.findAll(spec);
        if (criteria.getSortBy() != null) {
            switch (criteria.getSortBy()) {
                case "price_asc" -> 
                    products.sort((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()));
                case "price_desc" -> 
                    products.sort((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()));
                case "sold" -> 
                    products.sort((p1, p2) -> Integer.compare(p2.getSoldQuantity(), p1.getSoldQuantity()));
                case "newest" -> 
                    products.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
            }
        }

        return products;
    }

    // Thêm method hỗ trợ để tạo Specification
    private Specification<Product> hasNameLike(String name) {
        return (root, query, cb) -> {
            if (name == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    private Specification<Product> hasDescriptionLike(String description) {
        return (root, query, cb) -> {
            if (description == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%");
        };
    }

    private Specification<Product> isPriceInRange(Long min, Long max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            
            if (min != null && max != null) {
                return cb.between(root.get("price"), 
                    BigDecimal.valueOf(min), BigDecimal.valueOf(max));
            }
            
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), 
                    BigDecimal.valueOf(min));
            }
            
            return cb.lessThanOrEqualTo(root.get("price"), 
                BigDecimal.valueOf(max));
        };
    }

    private Specification<Product> isInStock() {
        return (root, query, cb) -> cb.greaterThan(root.get("stockQuantity"), 0);
    }
}
