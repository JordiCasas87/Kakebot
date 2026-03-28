package com.jordi.kakebot.expense.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.math.BigDecimal;

public class LenientBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String rawValue = parser.getValueAsString();

        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalizedValue = rawValue.trim().replace(',', '.');

        try {
            return new BigDecimal(normalizedValue);
        } catch (NumberFormatException exception) {
            throw JsonMappingException.from(parser, "El importe debe ser un numero valido");
        }
    }
}
