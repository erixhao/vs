package com.vs.common.domain;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vs.common.domain.annotation.MapInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public abstract class Entity {
    private final String SPLIT = ",";

    public abstract String getKey();

    public String to() {
        StringBuilder vson = new StringBuilder();

        Map<Integer, String> values = getFieldMapping();

        values.forEach((position, value) -> {
            if (vson.length() > 0) {
                vson.append(SPLIT);
            }
            vson.append(value);
        });

        return vson.toString();
    }

    @NotNull
    private Map<Integer, String> getFieldMapping() {
        Map<Integer, String> values = Maps.newHashMap();
        for (Field field : getAllFields()) {
            field.setAccessible(true);
            MapInfo mapInfo = field.getAnnotation(MapInfo.class);

            try {
                if (mapInfo != null) {
                    if (field.getType().equals(Map.class)) {
                        values.put(mapInfo.position(), fromMap((Map<String, Integer>) field.get(this)));
                    } else {
                        values.put(mapInfo.position(), field.get(this).toString());
                    }

                }
            } catch (Exception e) {
                try {
                    System.err.println(field.getName() + "/" + field.get(this));
                } catch (IllegalArgumentException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return values;
    }

    public <T> T from(String string) {
        String[] values = string.split(SPLIT);

        for (Field field : getAllFields()) {
            MapInfo mapInfo = field.getAnnotation(MapInfo.class);

            if (mapInfo == null || mapInfo.position() >= values.length) {
                continue;
            }

            String value = values[mapInfo.position()].trim();
            setFieldValue(field, value);
        }

        return (T) this;
    }

    private List<Field> getAllFields() {
        List<Field> fields = Lists.newArrayList();

        Class cur = this.getClass();
        while (cur != Object.class) {
            for (Field field : cur.getDeclaredFields()) {
                fields.add(field);
            }

            cur = cur.getSuperclass();
        }
        return fields;
    }

    private void setFieldValue(Field field, String value) {
        field.setAccessible(true);

        try {
            Class<?> type = field.getType();
            if (type.equals(String.class)) {
                field.set(this, value);
            } else if (type.equals(Double.class) || type.equals(double.class)) {
                Double dValue = Double.valueOf(StringUtils.isBlank(value) ? "0" : value);
                field.set(this, dValue);
            } else if (type.equals(Float.class) || type.equals(float.class)) {
                Float fValue = Float.valueOf(value);
                field.set(this, fValue);
            } else if (type.equals(Integer.class) || type.equals(int.class)) {
                Integer fValue = Integer.valueOf(value);
                field.set(this, fValue);
            } else if (type.equals(Long.class) || type.equals(long.class)) {
                Long lValue = Long.valueOf(value);
                field.set(this, lValue);
            } else if (type.equals(Character.class) || type.equals(char.class)) {
                Character fValue = Character.valueOf((char) value.getBytes()[0]);
                field.set(this, fValue);
            } else if (type.equals(Map.class)) {
                Map<String, Integer> fValue = toMap(value);
                field.set(this, fValue);
            } else if (type.equals(LocalDate.class)) {
                String[] sDate = value.split("-");
                LocalDate date = LocalDate.of(Integer.valueOf(sDate[0]), Integer.valueOf(sDate[1]), Integer.valueOf(sDate[2]));
                field.set(this, date);
            } else {
                throw new RuntimeException("Not Supported Type in JEntity.fromString method.   [" + type.toString() + "/" + value + "]");
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            System.out.println(field.getName() + "-" + field.getType() + "-" + value);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Map<String, Integer> toMap(String value) {
        Map<String, Integer> map = Maps.newHashMap();

        value = value.replace("{", "");
        value = value.replace("}", "");

        if (value.trim().length() > 0) {
            String[] entries = value.split(";");
            for (String entry : entries) {
                String[] pair = entry.split("=");
                map.put(pair[0], Integer.valueOf(pair[1]));
            }
        }

        return map;
    }

    private String fromMap(Map<String, Integer> map) {
        StringBuilder s = new StringBuilder();

        s.append("{");
        for (Entry<String, Integer> entry : map.entrySet()) {
            if (s.length() > 1) {
                s.append(";");
            }
            s.append(entry.getKey());
            s.append("=");
            s.append(entry.getValue().toString());
        }

        s.append("}");

        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MarketData that = (MarketData) o;
        return Objects.equals(this.getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getKey());
    }
}
