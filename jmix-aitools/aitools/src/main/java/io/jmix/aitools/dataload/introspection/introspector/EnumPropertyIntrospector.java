/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.aitools.dataload.introspection.introspector;

import io.jmix.aitools.dataload.introspection.model.*;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Introspects enum properties into an {@link EnumPropertyDescriptor}, including the enum constants
 * and how they are stored in the database.
 */
@Component("aitls_EnumPropertyIntrospector")
public class EnumPropertyIntrospector extends AbstractPropertyIntrospector {

    @Autowired
    protected Messages messages;

    @Override
    public boolean supports(MetaProperty property) {
        return property.getType() == MetaProperty.Type.ENUM;
    }

    @Nullable
    @Override
    public EntityPropertyDescriptor introspect(MetaProperty property) {
        if (!supports(property)) {
            return null;
        }

        Class<?> enumClass = property.getJavaType();
        Map<String, EnumValueDescriptor> enums = new HashMap<>();

        if (enumClass.isEnum()) {
            if (EnumClass.class.isAssignableFrom(enumClass)) {
                for (Object enumConstant : enumClass.getEnumConstants()) {
                    EnumClass<?> enumClassConstant = (EnumClass<?>) enumConstant;
                    String enumName = ((Enum<?>) enumConstant).name();
                    enums.put(enumName,
                            new EnumValueDescriptor(enumClassConstant.getId(), enumName,
                                    getLocalizedEnumValueNames(enumConstant)));
                }
            } else {
                // For plain enums
                String storageMode = getEnumStorageMode(property);
                for (Object enumConstant : enumClass.getEnumConstants()) {
                    String enumName = ((Enum<?>) enumConstant).name();
                    Object id = Objects.requireNonNull(storageMode).equals(EnumType.ORDINAL.name().toLowerCase())
                            ? ((Enum<?>) enumConstant).ordinal()
                            : ((Enum<?>) enumConstant).name();

                    enums.put(enumName, new EnumValueDescriptor(id, enumName, Collections.emptyList()));
                }
            }
            return new EnumPropertyDescriptor(property.getName(),
                    getPropertyLocalizedNames(property),
                    property.getJavaType().getSimpleName(),
                    getPropertyType(property),
                    null,
                    getPersistent(property),
                    getMandatory(property),
                    getComment(property),
                    new EnumClassDescriptor(enumClass.getSimpleName(), Collections.emptyList(), enums),
                    getEnumStorageMode(property));
        }
        return null;
    }

    protected List<String> getLocalizedEnumValueNames(Object enumConstant) {
        Collection<Locale> locales = messageTools.getAvailableLocalesMap().values();
        List<String> names = new ArrayList<>(locales.size());
        for (Locale locale : locales) {
            String localizedName = messages.getMessage((Enum<?>) enumConstant, locale);
            if (!((Enum<?>) enumConstant).name().equals(localizedName)
                    && !getEnumCaptionFallbackKey(enumConstant).equals(localizedName)) {
                names.add(localizedName);
            }
        }
        return names;
    }

    protected String getEnumCaptionFallbackKey(Object enumConstant) {
        Enum<?> enumValue = (Enum<?>) enumConstant;
        return enumValue.getDeclaringClass().getSimpleName() + "." + enumValue.name();
    }

    @Nullable
    protected String getEnumStorageMode(MetaProperty property) {
        if (EnumClass.class.isAssignableFrom(property.getJavaType())) {
            return null;
        }

        Enumerated enumerated = property.getAnnotatedElement().getAnnotation(Enumerated.class);
        EnumType enumType = enumerated != null ? enumerated.value() : EnumType.ORDINAL;
        return enumType.name().toLowerCase();
    }
}
