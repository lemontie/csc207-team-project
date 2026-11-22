package entity;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProductFactory implements Function<JsonNode, Product> {
    @Override
    public Product apply(JsonNode json) {
        if (json == null || json.isNull()) {
            return null;
        }

        return Product.builder()
                .id(text(json, "id"))
                .title(text(json, "title"))
                .description(text(json, "description"))
                .brands(buildBrands(json.get("brands")))
                .images(buildImages(json.get("images")))
                .category(buildCategory(json.get("category")))
                .gender(text(json, "gender"))
                .age(text(json, "age"))
                .materials(buildStringList(json.get("materials")))
                .keyFeatures(buildStringList(json.get("key_features")))
                .colors(buildColors(json.get("colors")))
                .offers(buildOffers(json.get("offers")))
                .variants(json.has("variants") ? json.get("variants") : null)
                .structuredAttributes(buildStructuredAttributes(json.get("structured_attributes")))
                .ratings(json.has("ratings") ? json.get("ratings") : null)
                .embedding(buildDoubleList(json.get("embedding")))
                .oklab(buildDoubleList(json.get("oklab")))
                .build();
    }

    private static String text(JsonNode json, String fieldName) {
        JsonNode value = json == null ? null : json.get(fieldName);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static Boolean booleanValue(JsonNode json, String fieldName) {
        JsonNode value = json == null ? null : json.get(fieldName);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private static Double doubleValue(JsonNode json, String fieldName) {
        JsonNode value = json == null ? null : json.get(fieldName);
        return value == null || value.isNull() ? null : value.asDouble();
    }

    private static List<String> buildStringList(JsonNode array) {
        List<String> values = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode value : array) {
                values.add(value.isNull() ? null : value.asText());
            }
        }
        return values;
    }

    private static List<Double> buildDoubleList(JsonNode array) {
        List<Double> values = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode value : array) {
                values.add(value.isNumber() ? value.asDouble() : null);
            }
        }
        return values;
    }

    private static List<Product.Brand> buildBrands(JsonNode array) {
        List<Product.Brand> brands = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode brand : array) {
                brands.add(Product.Brand.builder()
                        .id(text(brand, "id"))
                        .name(text(brand, "name"))
                        .build());
            }
        }
        return brands;
    }

    private static List<Product.Image> buildImages(JsonNode array) {
        List<Product.Image> images = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode image : array) {
                images.add(Product.Image.builder()
                        .url(text(image, "url"))
                        .cleanedUrl(text(image, "cleaned_url"))
                        .isMainImage(booleanValue(image, "is_main_image"))
                        .shotType(text(image, "shot_type"))
                        .altText(text(image, "alt_text"))
                        .build());
            }
        }
        return images;
    }

    private static Product.Category buildCategory(JsonNode category) {
        if (category == null || category.isNull()) {
            return null;
        }

        List<Product.CategoryPathItem> path = new ArrayList<>();
        JsonNode pathArray = category.get("path");
        if (pathArray != null && pathArray.isArray()) {
            for (JsonNode item : pathArray) {
                path.add(Product.CategoryPathItem.builder()
                        .slug(text(item, "slug"))
                        .title(text(item, "title"))
                        .build());
            }
        }

        return Product.Category.builder()
                .slug(text(category, "slug"))
                .title(text(category, "title"))
                .path(path)
                .hasChildren(booleanValue(category, "has_children"))
                .build();
    }

    private static List<Product.Color> buildColors(JsonNode array) {
        List<Product.Color> colors = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode color : array) {
                colors.add(Product.Color.builder()
                        .hex(text(color, "hex"))
                        .percentage(doubleValue(color, "percentage"))
                        .build());
            }
        }
        return colors;
    }

    private static List<Product.Offer> buildOffers(JsonNode array) {
        List<Product.Offer> offers = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode offer : array) {
                JsonNode priceNode = offer.get("price");
                Product.Price price = priceNode == null || priceNode.isNull()
                        ? null
                        : Product.Price.builder()
                                .price(doubleValue(priceNode, "price"))
                                .compareAtPrice(doubleValue(priceNode, "compare_at_price"))
                                .currency(text(priceNode, "currency"))
                                .build();

                offers.add(Product.Offer.builder()
                        .url(text(offer, "url"))
                        .domain(text(offer, "domain"))
                        .price(price)
                        .availability(text(offer, "availability"))
                        .condition(text(offer, "condition"))
                        .maxCommissionRate(doubleValue(offer, "max_commission_rate"))
                        .dimensions(offer.has("dimensions") ? offer.get("dimensions") : null)
                        .build());
            }
        }
        return offers;
    }

    private static Map<String, List<String>> buildStructuredAttributes(JsonNode attributes) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (attributes == null || attributes.isNull()) {
            return result;
        }
        attributes.fields().forEachRemaining(entry ->
                result.put(entry.getKey(), buildStringList(entry.getValue())));
        return result;
    }
}
