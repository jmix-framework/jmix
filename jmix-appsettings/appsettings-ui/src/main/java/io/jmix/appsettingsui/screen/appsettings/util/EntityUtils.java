package io.jmix.appsettingsui.screen.appsettings.util;

import io.jmix.core.annotation.Secret;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;

import java.util.UUID;

public final class EntityUtils {

    private EntityUtils() {
        //to prevent instantiation
    }

    public static boolean isSecret(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().isAnnotationPresent(Secret.class);
    }

    public static boolean isMany(MetaProperty metaProperty) {
        return metaProperty.getRange().getCardinality().isMany();
    }

    static boolean isByteArray(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(byte[].class);
    }

    static boolean isUuid(MetaProperty metaProperty) {
        return metaProperty.getRange().asDatatype().getJavaClass().equals(UUID.class);
    }

    static boolean isBoolean(MetaProperty metaProperty) {
        return metaProperty.getRange().isDatatype()
                && metaProperty.getRange().asDatatype().getJavaClass().equals(Boolean.class);
    }

    static boolean requireTextArea(MetaProperty metaProperty, Object item, int maxTextFieldLength) {
        if (!String.class.equals(metaProperty.getJavaType())) {
            return false;
        }

        Integer textLength = (Integer) metaProperty.getAnnotations().get("length");
        boolean isLong = textLength != null && textLength > maxTextFieldLength;

        Object value = EntityValues.getValue(item, metaProperty.getName());
        boolean isContainsSeparator = value != null && containsSeparator((String) value);

        return isLong || isContainsSeparator;
    }

    static boolean containsSeparator(String s) {
        return s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0;
    }
}
