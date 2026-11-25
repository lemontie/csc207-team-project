package entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a fashion product and its catalog, image, offer, and embedding data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    /** Stable product identifier. */
    private String id;
    /** Display name of the product. */
    private String title;
    /** Marketing or catalog description. */
    private String description;
    /** Brands associated with the product. */
    private List<Brand> brands;
    /** Images associated with the product. */
    private List<Image> images;
    /** Primary category and category hierarchy. */
    private Category category;
    /** Target gender category, when supplied. */
    private String gender;
    /** Target age category, when supplied. */
    private String age;
    /** Declared product materials. */
    private List<String> materials;
    /** Searchable key features. */
    private List<String> keyFeatures;
    /** Representative colors and percentages. */
    private List<Color> colors;
    /** Retail offers and prices. */
    private List<Offer> offers;
    /** Raw variant document; product-specific structure is not normalized. */
    private Object variants;
    /** Structured catalog attributes grouped by attribute name. */
    private Map<String, List<String>> structuredAttributes;
    /** Raw rating document; schema is not normalized. */
    private Object ratings;
    /** Text or catalog embedding values. */
    private List<Double> embedding;
    /** Oklab color histogram values. */
    private List<Double> oklab;

    /**
     * A brand that owns or sells the product.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Brand {
        /** Brand identifier. */
        private String id;
        /** Brand name. */
        private String name;
    }

    /**
     * An image used to present the product.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        /** Original image URL. */
        private String url;
        /** URL of a cleaned or normalized image, when available. */
        private String cleanedUrl;
        /** Whether this is the main catalog image. */
        private Boolean isMainImage;
        /** Image framing or shot classification. */
        private String shotType;
        /** Accessible alternative text for the image. */
        private String altText;
    }

    /**
     * The product's primary category and hierarchy.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Category {
        /** Stable category slug. */
        private String slug;
        /** Human-readable category name. */
        private String title;
        /** Ordered category ancestors and/or category path entries. */
        private List<CategoryPathItem> path;
        /** Whether the category contains child categories. */
        private Boolean hasChildren;
    }

    /**
     * One item in a category hierarchy.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryPathItem {
        /** Stable category-slug segment. */
        private String slug;
        /** Human-readable category title. */
        private String title;
    }

    /**
     * A color associated with the product.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Color {
        /** Six-digit hexadecimal RGB code. */
        private String hex;
        /** Fraction or percentage of this color in the product. */
        private Double percentage;
    }

    /**
     * A retail listing for the product.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Offer {
        /** URL for the retail listing. */
        private String url;
        /** Retailer or source domain. */
        private String domain;
        /** Listing price information. */
        private Price price;
        /** Inventory availability status. */
        private String availability;
        /** Listing condition, such as new or used. */
        private String condition;
        /** Maximum affiliate commission rate, when supplied. */
        private Double maxCommissionRate;
        /** Raw product-dimension document. */
        private Object dimensions;
    }

    /**
     * Price information for an offer.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Price {
        /** Current selling price. */
        private Double price;
        /** Original or comparison price, when available. */
        private Double compareAtPrice;
        /** ISO-style or catalog currency code. */
        private String currency;
    }
}
