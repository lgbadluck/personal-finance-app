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

        String id;
        if (node.isTextual()) { // Handle plain string (UUID)
            id = node.asText();
        } else if (node.has("id")) { // Handle object with "id" field
            id = node.get("id").asText();
        } else {
            throw new IOException("ID is missing in the category JSON.");
        }

        Category category = new Category();
        category.setId(UUID.fromString(id));

        return category;
    }
}
