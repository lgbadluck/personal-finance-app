package com.softuni.personal_finance_app.web.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.softuni.personal_finance_app.enitity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDeserializer extends JsonDeserializer<Category> {

    @Override
    public Category deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (!node.has("id")) {
            throw new IOException("ID is missing in the category JSON.");
        }

        String id = node.get("id").asText();
        Category category = new Category();
        category.setId(UUID.fromString(id));

        return category;
    }
}
