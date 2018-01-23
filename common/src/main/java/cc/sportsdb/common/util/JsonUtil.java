package cc.sportsdb.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class JsonUtil {

    public static final ObjectMapper OBJECT_MAPPER;
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private JsonUtil() {
    }

    public static <T> T parse(String jsonStr, TypeReference<? extends T> typeReference) {
        return parse(jsonStr, typeReference, OBJECT_MAPPER);
    }

    public static <T> T parse(String jsonStr, TypeReference<? extends T> typeReference, ObjectMapper objectMapper) {
        T t = null;
        try {
            t = objectMapper.readValue(jsonStr, typeReference);
        } catch (IOException e) {
            logger.error("Parse to object fail", e);
        }
        return t;
    }

    public static <T> T parse(String jsonStr, Class<? extends T> clazz) {
        return parse(jsonStr, clazz, OBJECT_MAPPER);
    }

    public static <T> T parse(String jsonStr, Class<? extends T> clazz, ObjectMapper objectMapper) {
        T t = null;
        try {
            t = objectMapper.readValue(jsonStr, clazz);
        } catch (IOException e) {
            logger.error("Parse to object fail", e);
        }
        return t;
    }

    public static String toJsonString(Object value) {
        return toJsonString(value, OBJECT_MAPPER, false);
    }

    public static String toPrettyJsonString(Object value) {
        return toJsonString(value, OBJECT_MAPPER, true);
    }

    public static String toJsonString(Object value, ObjectMapper objectMapper, boolean pretty) {
        String jsonString = null;
        try {
            ;
            jsonString = pretty
                    ? objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value)
                    : objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            logger.error("To json string fail", e);
        }
        return jsonString;
    }

}
