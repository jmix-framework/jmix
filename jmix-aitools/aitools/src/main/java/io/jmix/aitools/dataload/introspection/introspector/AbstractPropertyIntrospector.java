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
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public abstract class AbstractPropertyIntrospector implements MetaPropertyIntrospector {

    @Autowired
    protected MessageTools messageTools;
    @Autowired
    protected MetadataTools metadataTools;

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

    public String getComment(MetaProperty property) {
        return metadataTools.getMetaAnnotationValue(property, Comment.class);
    }

    public String getPropertyType(MetaProperty property) {
        return property.getType().name().toLowerCase();
    }

    public Boolean getIdentifier(MetaProperty property) {
        return property.equals(metadataTools.getPrimaryKeyProperty(property.getDomain())) ? true : null;
    }

    public Boolean getPersistent(MetaProperty property) {
        return metadataTools.isJpa(property);
    }

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
