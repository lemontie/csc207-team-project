package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String title;
    private String description;
    private List<Brand> brands;
    private List<Image> images;
    private Category category;
    private String gender;
    private String age;
    private List<String> materials;
    private List<String> keyFeatures;
    private List<Color> colors;
    private List<Offer> offers;
    private Object variants;
    private Map<String, List<String>> structuredAttributes;
    private Object ratings;
    private List<Double> embedding;
    private List<Double> oklab;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Brand {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        private String url;
        private String cleanedUrl;
        private Boolean isMainImage;
        private String shotType;
        private String altText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        private String slug;
        private String title;
        private List<CategoryPathItem> path;
        private Boolean hasChildren;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPathItem {
        private String slug;
        private String title;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Color {
        private String hex;
        private Double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Offer {
        private String url;
        private String domain;
        private Price price;
        private String availability;
        private String condition;
        private Double maxCommissionRate;
        private Object dimensions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Price {
        private Double price;
        private Double compareAtPrice;
        private String currency;
    }
}
