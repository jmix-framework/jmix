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

package io.jmix.aitools;

import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Decides whether an entity or an attribute must be kept away from the AI, using {@link ExcludeFromAi},
 * {@code @Secret}, {@code @SystemLevel} and the {@code jmix.aitools.dataload.*} include/exclude properties.
 * <p>
 * Works regardless of whether the data-load feature is enabled, so any feature that sends application data
 * to the model applies the same rules as the data-load index.
 * <p>
 * The rules do not depend on the current user; access permissions are checked separately.
 */
@NullMarked
@Experimental
@Component("aitls_AiExclusionSupport")
public class AiExclusionSupport {

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected AiToolsDataLoadProperties dataLoadProperties;

    /**
     * Checks whether the entity is excluded from AI. The rules apply in this order, the first match wins:
     * <ol>
     *     <li>{@link ExcludeFromAi} (inherited by subclasses) &mdash; excluded unconditionally;</li>
     *     <li>listed in {@code excludeEntities} &mdash; excluded, above every include rule;</li>
     *     <li>explicitly included &mdash; not excluded, overriding the two rules below;</li>
     *     <li>system-level entity while {@code excludeSystemLevelEntities} is on, or matching
     *     {@code excludePackages} &mdash; excluded.</li>
     * </ol>
     * The JPA-only (DTO) rule of the data-load index is not applied here.
     *
     * @param metaClass entity metaclass
     * @return {@code true} if the entity must not be exposed to the AI
     */
    public boolean isExcluded(MetaClass metaClass) {
        if (isExcludedFromAi(metaClass)) {
            return true;
        }
        if (isExplicitlyExcluded(metaClass)) {
            return true;
        }
        if (isExplicitlyIncluded(metaClass)) {
            return false;
        }
        if (Boolean.TRUE.equals(dataLoadProperties.getExcludeSystemLevelEntities())
                && metadataTools.isSystemLevel(metaClass)) {
            return true;
        }
        return matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(),
                dataLoadProperties.getExcludePackages());
    }

    /**
     * Checks whether the entity is listed in {@code includeEntities} or matches {@code includePackages}.
     *
     * @param metaClass entity metaclass
     * @return {@code true} if the entity is explicitly force-included
     */
    public boolean isExplicitlyIncluded(MetaClass metaClass) {
        return matchesEntityName(metaClass, dataLoadProperties.getIncludeEntities())
                || matchesAnyPackagePrefix(metaClass.getJavaClass().getPackageName(),
                dataLoadProperties.getIncludePackages());
    }

    /**
     * Checks whether the attribute is always hidden from the AI: annotated with {@link ExcludeFromAi} or
     * {@code @Secret}. A {@code @SystemLevel} attribute is not covered, as hiding it is configurable.
     *
     * @param property attribute
     * @return {@code true} if the attribute must never be exposed
     */
    public boolean isHidden(MetaProperty property) {
        return isExcludedFromAi(property) || metadataTools.isSecret(property);
    }

    /**
     * Checks whether the attribute is hidden from the AI as a {@code @SystemLevel} attribute, which depends on
     * {@code excludeSystemLevelAttributes}.
     *
     * @param property attribute
     * @return {@code true} if the attribute is system-level and such attributes must not be exposed
     */
    public boolean isHiddenSystemLevel(MetaProperty property) {
        return Boolean.TRUE.equals(dataLoadProperties.getExcludeSystemLevelAttributes())
                && metadataTools.isSystemLevel(property);
    }

    /**
     * Checks whether a value read through the path must be kept from the AI: the path starts at, or passes
     * through, an excluded entity, or contains a hidden attribute, or a {@code @SystemLevel} attribute while
     * {@code excludeSystemLevelAttributes} is on. The root of a KeyValue entity is not checked as an entity,
     * being a synthetic class that lives in the excluded {@code io.jmix} package.
     *
     * @param path property path, e.g. of a grid column
     * @return {@code true} if the path must not be exposed to the AI
     */
    public boolean isExcluded(MetaPropertyPath path) {
        MetaClass root = path.getMetaClass();
        if (!KeyValueEntity.class.equals(root.getJavaClass()) && isExcluded(root)) {
            return true;
        }
        for (MetaProperty property : path.getMetaProperties()) {
            if (isHidden(property) || isHiddenSystemLevel(property)) {
                return true;
            }
            if (property.getRange().isClass() && isExcluded(property.getRange().asClass())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isExplicitlyExcluded(MetaClass metaClass) {
        return matchesEntityName(metaClass, dataLoadProperties.getExcludeEntities());
    }

    protected boolean isExcludedFromAi(MetaClass metaClass) {
        return metaClass.getAnnotations().containsKey(ExcludeFromAi.class.getName());
    }

    protected boolean isExcludedFromAi(MetaProperty metaProperty) {
        return metaProperty.getAnnotations().containsKey(ExcludeFromAi.class.getName());
    }

    protected boolean matchesEntityName(MetaClass metaClass, List<String> entityNames) {
        return entityNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .anyMatch(metaClass.getName()::equals);
    }

    protected boolean matchesAnyPackagePrefix(String packageName, List<String> packagePrefixes) {
        return packagePrefixes.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(prefix -> !prefix.isEmpty())
                .anyMatch(prefix -> packageName.equals(prefix) || packageName.startsWith(prefix + "."));
    }
}
