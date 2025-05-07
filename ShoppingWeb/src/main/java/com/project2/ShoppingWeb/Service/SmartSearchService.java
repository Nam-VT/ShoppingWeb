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
import java.util.ArrayList;

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
                "keywords": [...],
                "attributes": [...]
            }
            Quy tắc phân tích:
            1. Phân tích câu tìm kiếm:
               - Tách thành các thành phần: sản phẩm, thuộc tính, giá cả,...
               - Loại bỏ các từ không cần thiết (tôi, muốn, tìm, sản phẩm,...)
               - Giữ lại các từ khóa có ý nghĩa
               
            2. Xử lý thuộc tính (attributes):
               - Màu sắc: đen, trắng, xanh, đỏ,...
               - Kích thước: nhỏ, vừa, lớn,...
               - Chất liệu: nhựa, kim loại,...
               - Ví dụ: "tôi muốn tìm sản phẩm màu đen" -> 
                 attributes: ["đen"],
                 keywords: ["sản phẩm"]
                 
            3. Xử lý từ khóa (keywords):
               - Loại bỏ các từ thừa: "tôi muốn tìm", "sản phẩm",...
               - Giữ lại các từ có ý nghĩa tìm kiếm
               - Ví dụ: "tôi muốn tìm điện thoại samsung màu đen" ->
                 keywords: ["điện thoại", "samsung"],
                 attributes: ["đen"]
                 
            4. Khoảng giá (priceRange):
               - Số cụ thể: "5 triệu" -> min: null, max: 5000000
               - "dưới X" -> min: null, max: X
               - "trên X" -> min: X, max: null
               - "từ X đến Y" -> min: X, max: Y
               - "giá rẻ" -> max: 500000
               - "tầm trung" -> min: 500000, max: 2000000
               - "cao cấp" -> min: 2000000, max: null
               
            5. Tồn kho (availability):
               - Mặc định: null (tìm tất cả)
               - "còn hàng" -> true
               - "hết hàng" -> false
               
            6. Sắp xếp (sortBy):
               - "giá tăng/rẻ nhất" -> "price_asc"
               - "giá giảm/đắt nhất" -> "price_desc"
               - "bán chạy/phổ biến" -> "sold"
               - "mới nhất" -> "newest"
               
            Ví dụ phân tích:
            1. "tôi muốn tìm sản phẩm màu đen" ->
               {
                 "valid": true,
                 "keywords": ["sản phẩm"],
                 "attributes": ["đen"]
               }
               
            2. "điện thoại samsung màu đen giá dưới 10 triệu" ->
               {
                 "valid": true,
                 "keywords": ["điện thoại", "samsung"],
                 "attributes": ["đen"],
                 "priceRange": {"max": 10000000}
               }
               
            3. "laptop gaming asus màn hình 15 inch" ->
               {
                 "valid": true,
                 "keywords": ["laptop", "gaming", "asus"],
                 "attributes": ["15 inch"]
               }
               
            Chỉ trả về JSON, không thêm giải thích.
            """;
    }

    public Mono<SmartSearchResponse> searchProducts(String searchQuery) {
        log.info("Đang xử lý tìm kiếm với query: {}", searchQuery);

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return Mono.just(new SmartSearchResponse(
                List.of(),
                "Vui lòng nhập từ khóa tìm kiếm",
                null
            ));
        }

        Map<String, Object> body = Map.of(
            "model", "llama3-70b-8192",
            "messages", List.of(
                Map.of("role", "system", "content", createSystemPrompt()),
                Map.of("role", "user", "content", searchQuery)
            ),
            "temperature", 0.3,
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

                    if (!isValidCriteria(criteria)) {
                        return new SmartSearchResponse(
                            List.of(),
                            "Không thể phân tích yêu cầu tìm kiếm. Vui lòng thử lại với từ khóa cụ thể hơn.",
                            criteria
                        );
                    }

                    List<Product> products = findProductsByCriteria(criteria);
                    
                    if (products.isEmpty()) {
                        return new SmartSearchResponse(
                            products,
                            generateNoResultsExplanation(criteria),
                            criteria
                        );
                    }

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
                    "Đã xảy ra lỗi trong hệ thống. Vui lòng thử lại sau!",
                    null
                ));
            });
    }

    private boolean isValidCriteria(SearchCriteria criteria) {
        if (criteria == null || Boolean.FALSE.equals(criteria.getValid())) {
            return false;
        }

        return (criteria.getName() != null && !criteria.getName().isBlank()) ||
               (criteria.getDescription() != null && !criteria.getDescription().isBlank()) ||
               (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) ||
               (criteria.getPriceRange() != null && 
                (criteria.getPriceRange().getMin() != null || criteria.getPriceRange().getMax() != null)) ||
               (criteria.getKeywords() != null && !criteria.getKeywords().isEmpty()) ||
               (criteria.getAttributes() != null && !criteria.getAttributes().isEmpty());
    }

    private String generateNoResultsExplanation(SearchCriteria criteria) {
        StringBuilder explanation = new StringBuilder("Không tìm thấy sản phẩm phù hợp");
        
        if (criteria.getName() != null && !criteria.getName().isBlank()) {
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

        explanation.append(". Gợi ý: Hãy thử tìm kiếm với từ khóa khác hoặc mở rộng tiêu chí tìm kiếm.");
        
        return explanation.toString();
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

        if (criteria.getKeywords() != null && !criteria.getKeywords().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<Product, Category> categoryJoin = root.join("categories", JoinType.LEFT);
                
                List<Predicate> predicates = new ArrayList<>();
                
                for (String keyword : criteria.getKeywords()) {
                    String searchTerm = "%" + keyword.toLowerCase() + "%";
                    predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchTerm),
                        cb.like(cb.lower(root.get("description")), searchTerm),
                        cb.like(cb.lower(categoryJoin.get("name")), searchTerm)
                    ));
                }
                
                return cb.and(predicates.toArray(new Predicate[0]));
            });
        }

        if (criteria.getAttributes() != null && !criteria.getAttributes().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                for (String attribute : criteria.getAttributes()) {
                    String searchTerm = "%" + attribute.toLowerCase() + "%";
                    predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), searchTerm),
                        cb.like(cb.lower(root.get("description")), searchTerm)
                    ));
                }
                
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }

        if (criteria.getName() != null && !criteria.getName().isBlank()) {
            spec = spec.and((root, query, cb) -> {
                String searchTerm = "%" + criteria.getName().toLowerCase() + "%";
                return cb.like(cb.lower(root.get("name")), searchTerm);
            });
        }

        if (criteria.getDescription() != null && !criteria.getDescription().isBlank()) {
            spec = spec.and((root, query, cb) -> {
                String searchTerm = "%" + criteria.getDescription().toLowerCase() + "%";
                return cb.like(cb.lower(root.get("description")), searchTerm);
            });
        }

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

        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<Product, Category> categoryJoin = root.join("categories");
                return categoryJoin.get("name").in(criteria.getCategories());
            });
        }

        if (Boolean.TRUE.equals(criteria.getAvailability())) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThan(root.get("stockQuantity"), 0));
        }

        spec = spec.and((root, query, cb) -> cb.isTrue(root.get("isActive")));

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
