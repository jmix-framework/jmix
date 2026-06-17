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

import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Base class for {@link MetaPropertyIntrospector} implementations.
 */
public abstract class AbstractPropertyIntrospector implements MetaPropertyIntrospector {

    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Returns the property captions across all configured locales, skipping locales where the caption
     * falls back to the raw property name.
     *
     * @param property property to read captions for
     * @return localized property names
     */
    public List<String> getPropertyLocalizedNames(MetaProperty property) {
        Collection<Locale> locales = messageTools.getAvailableLocalesMap().values();
        List<String> names = new ArrayList<>(locales.size());
        for (Locale locale : locales) {
            String localizedName = messageTools.getPropertyCaption(property, locale);
            String fallbackKey = getPropertyCaptionFallbackKey(property);
            if (!property.getName().equals(localizedName) && !fallbackKey.equals(localizedName)) {
                names.add(localizedName);
            }
        }
        return names;
    }

    /**
     * Returns the entity captions across all configured locales, skipping locales where the caption
     * falls back to the raw entity name.
     *
     * @param metaClass entity meta-class to read captions for
     * @return localized entity names
     */
    public List<String> getEntityLocalizedNames(MetaClass metaClass) {
        Collection<Locale> locales = messageTools.getAvailableLocalesMap().values();
        List<String> names = new ArrayList<>(locales.size());
        for (Locale locale : locales) {
            String localizedName = messageTools.getEntityCaption(metaClass, locale);
            if (!isEntityCaptionFallback(metaClass, localizedName)) {
                names.add(localizedName);
            }
        }
        return names;
    }

    /**
     * Returns the value of the property's {@code @Comment} annotation.
     *
     * @param property property to read the comment for
     * @return comment text, or {@code null} if the property is not annotated
     */
    @Nullable
    public String getComment(MetaProperty property) {
        return metadataTools.getMetaAnnotationValue(property, Comment.class);
    }

    /**
     * Returns the lower-cased Jmix property type name (for example {@code "datatype"} or {@code "enum"}).
     *
     * @param property property to read the type for
     * @return property type name
     */
    public String getPropertyType(MetaProperty property) {
        return property.getType().name().toLowerCase();
    }

    /**
     * Returns whether the property is the entity identifier.
     *
     * @param property property to check
     * @return {@code true} if the property is the entity's primary key, or {@code null} otherwise
     */
    @Nullable
    public Boolean getIdentifier(MetaProperty property) {
        return property.equals(metadataTools.getPrimaryKeyProperty(property.getDomain())) ? true : null;
    }

    /**
     * Returns whether the property is persistent (stored in the database).
     *
     * @param property property to check
     * @return {@code true} if the property is persistent
     */
    public Boolean getPersistent(MetaProperty property) {
        return metadataTools.isJpa(property);
    }

    /**
     * Returns whether the property is mandatory.
     *
     * @param property property to check
     * @return {@code true} if the property is mandatory
     */
    public Boolean getMandatory(MetaProperty property) {
        return property.isMandatory();
    }

    protected boolean isEntityCaptionFallback(MetaClass metaClass, String localizedName) {
        return metaClass.getName().equals(localizedName)
                || metaClass.getJavaClass().getSimpleName().equals(localizedName);
    }

    protected String getPropertyCaptionFallbackKey(MetaProperty property) {
        Class<?> declaringClass = property.getDeclaringClass();
        if (declaringClass == null) {
            return property.getName();
        }

        return declaringClass.getSimpleName() + "." + property.getName();
    }
}
