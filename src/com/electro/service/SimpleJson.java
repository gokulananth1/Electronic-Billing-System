package com.electro.service;

import java.util.*;

/**
 * Lightweight zero-dependency JSON serializer and parser for application state.
 * Allows running with standard Java 21 without requiring Maven, Jackson, or Gson.
 */
public class SimpleJson {

    // Simple JSON Value Types
    public static class JsonValue {
        private Object value; // Can be Map<String, JsonValue>, List<JsonValue>, String, Double, Boolean, null

        public JsonValue(Object value) {
            this.value = value;
        }

        public boolean isObject() { return value instanceof Map; }
        public boolean isArray() { return value instanceof List; }
        public boolean isString() { return value instanceof String; }
        public boolean isNumber() { return value instanceof Number; }
        public boolean isBoolean() { return value instanceof Boolean; }
        public boolean isNull() { return value == null; }

        @SuppressWarnings("unchecked")
        public Map<String, JsonValue> asObject() {
            return isObject() ? (Map<String, JsonValue>) value : Collections.emptyMap();
        }

        @SuppressWarnings("unchecked")
        public List<JsonValue> asArray() {
            return isArray() ? (List<JsonValue>) value : Collections.emptyList();
        }

        public String asString(String def) {
            if (value == null) return def;
            return value.toString();
        }

        public double asDouble(double def) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            if (value instanceof String) {
                try { return Double.parseDouble((String) value); } catch (Exception ignored) {}
            }
            return def;
        }

        public int asInt(int def) {
            if (value instanceof Number) return ((Number) value).intValue();
            if (value instanceof String) {
                try { return Integer.parseInt((String) value); } catch (Exception ignored) {}
            }
            return def;
        }

        public boolean asBoolean(boolean def) {
            if (value instanceof Boolean) return (Boolean) value;
            if (value instanceof String) return Boolean.parseBoolean((String) value);
            return def;
        }

        public JsonValue get(String key) {
            Map<String, JsonValue> obj = asObject();
            return obj.getOrDefault(key, new JsonValue(null));
        }
    }

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (ch < ' ') {
                        String hex = "000" + Integer.toHexString(ch);
                        sb.append("\\u").append(hex.substring(hex.length() - 4));
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    public static JsonValue parse(String json) {
        if (json == null) return new JsonValue(null);
        Tokenizer tokenizer = new Tokenizer(json.trim());
        return tokenizer.parseValue();
    }

    private static class Tokenizer {
        private final String src;
        private int pos = 0;

        Tokenizer(String src) {
            this.src = src;
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
                pos++;
            }
        }

        JsonValue parseValue() {
            skipWhitespace();
            if (pos >= src.length()) return new JsonValue(null);
            char c = src.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') return parseNull();
            if (c == '-' || Character.isDigit(c)) return parseNumber();
            return new JsonValue(null);
        }

        JsonValue parseObject() {
            pos++; // skip '{'
            Map<String, JsonValue> map = new LinkedHashMap<>();
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                return new JsonValue(map);
            }
            while (pos < src.length()) {
                skipWhitespace();
                String key = "";
                if (src.charAt(pos) == '"') {
                    key = parseString().asString("");
                }
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ':') {
                    pos++;
                }
                JsonValue val = parseValue();
                map.put(key, val);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == '}') {
                    pos++;
                    break;
                } else {
                    pos++;
                }
            }
            return new JsonValue(map);
        }

        JsonValue parseArray() {
            pos++; // skip '['
            List<JsonValue> list = new ArrayList<>();
            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                return new JsonValue(list);
            }
            while (pos < src.length()) {
                JsonValue val = parseValue();
                list.add(val);
                skipWhitespace();
                if (pos < src.length() && src.charAt(pos) == ',') {
                    pos++;
                } else if (pos < src.length() && src.charAt(pos) == ']') {
                    pos++;
                    break;
                } else {
                    pos++;
                }
            }
            return new JsonValue(list);
        }

        JsonValue parseString() {
            pos++; // skip opening '"'
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') break;
                if (c == '\\' && pos < src.length()) {
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u':
                            if (pos + 4 <= src.length()) {
                                String hex = src.substring(pos, pos + 4);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                            break;
                        default: sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            return new JsonValue(sb.toString());
        }

        JsonValue parseBoolean() {
            if (src.startsWith("true", pos)) {
                pos += 4;
                return new JsonValue(Boolean.TRUE);
            } else if (src.startsWith("false", pos)) {
                pos += 5;
                return new JsonValue(Boolean.FALSE);
            }
            return new JsonValue(Boolean.FALSE);
        }

        JsonValue parseNull() {
            if (src.startsWith("null", pos)) {
                pos += 4;
            }
            return new JsonValue(null);
        }

        JsonValue parseNumber() {
            int start = pos;
            if (src.charAt(pos) == '-') pos++;
            while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.' || src.charAt(pos) == 'e' || src.charAt(pos) == 'E' || src.charAt(pos) == '+' || src.charAt(pos) == '-')) {
                pos++;
            }
            String numStr = src.substring(start, pos);
            try {
                if (numStr.contains(".")) {
                    return new JsonValue(Double.parseDouble(numStr));
                } else {
                    return new JsonValue(Long.parseLong(numStr));
                }
            } catch (Exception e) {
                return new JsonValue(0.0);
            }
        }
    }
}
