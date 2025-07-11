package de.hs_esslingen.besy.jackson;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class NumericBooleanDeserzializer extends JsonDeserializer<Boolean> {
    @Override
    public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        // Long ago, someone declared: 'Let there be Strings!' — even for integers. And so it was.
        String str = jsonParser.getText();
        int value = Integer.parseInt(str);
        return value == 1;
    }
}
