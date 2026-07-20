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
package io.jmix.flowui.component.factory;

import com.google.common.base.Strings;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.LookupField;
import io.jmix.core.entity.annotation.LookupItemsQuery;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.component.factory.EffectiveLookupConfig.ItemsMode;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Resolves the effective {@code @LookupField} configuration for an entity reference: annotation
 * precedence (field annotation, then the {@code entity-field-actions} property, then class
 * annotation) and the validations that do not depend on runtime context. The single source of truth
 * shared by {@code EntityFieldCreationSupport} and {@code ComponentXmlFactory}.
 */
@Component("flowui_LookupFieldSupport")
public class LookupFieldSupport {

    private static final Logger log = LoggerFactory.getLogger(LookupFieldSupport.class);

    protected static final String SEARCH_STRING_PARAMETER_REF = ":searchString";

    protected final MetadataTools metadataTools;
    protected final UiComponentProperties componentProperties;

    public LookupFieldSupport(MetadataTools metadataTools, UiComponentProperties componentProperties) {
        this.metadataTools = metadataTools;
        this.componentProperties = componentProperties;
    }

    /**
     * Resolves the effective configuration for a reference attribute.
     *
     * @param referencingProperty   the attribute that references an entity
     * @param referencedEntityClass the referenced entity's metaClass
     */
    public EffectiveLookupConfig resolve(MetaProperty referencingProperty, MetaClass referencedEntityClass) {
        LookupFieldSettings fieldSettings = readSettings(referencingProperty.getAnnotations());
        LookupFieldSettings classSettings = readSettings(referencedEntityClass.getAnnotations());
        LookupFieldSettings componentSettings = fieldSettings != null ? fieldSettings : classSettings;
        boolean fieldLevel = fieldSettings != null;

        List<String> actions = resolveActions(fieldSettings, classSettings, referencedEntityClass);

        if (componentSettings == null) {
            return new EffectiveLookupConfig(null, false, actions, ItemsMode.EAGER,
                    null, null, false, null);
        }

        if (componentSettings.type == LookupType.VIEW) {
            if (isItemsQueryConfigured(componentSettings.itemsQuery)) {
                log.warn("itemsQuery of @LookupField is ignored for type VIEW (entity '{}')",
                        referencedEntityClass.getName());
            }
            return new EffectiveLookupConfig(LookupType.VIEW, fieldLevel, actions, ItemsMode.EAGER,
                    null, null, false, null);
        }

        // DROPDOWN
        return resolveDropdown(componentSettings.itemsQuery, referencedEntityClass, fieldLevel, actions);
    }

    protected EffectiveLookupConfig resolveDropdown(@Nullable LookupItemsQuery itemsQuery,
                                                    MetaClass metaClass, boolean fieldLevel,
                                                    List<String> actions) {
        if (itemsQuery == null || !isItemsQueryConfigured(itemsQuery)) {
            return dropdown(fieldLevel, actions, ItemsMode.EAGER, null, null, false, null);
        }

        String explicitQuery = itemsQuery.query();
        if (itemsQuery.byInstanceName() && !explicitQuery.isEmpty()) {
            log.warn("Both 'byInstanceName' and 'query' are set in @LookupField itemsQuery " +
                    "for entity '{}', the explicit query is used", metaClass.getName());
        }

        if (!explicitQuery.isEmpty()) {
            if (!explicitQuery.contains(SEARCH_STRING_PARAMETER_REF)) {
                log.warn("Query in @LookupField itemsQuery for entity '{}' has no {} parameter, " +
                        "items are loaded eagerly", metaClass.getName(), SEARCH_STRING_PARAMETER_REF);
                return dropdown(fieldLevel, actions, ItemsMode.EAGER, null, null, false, null);
            }
            return dropdown(fieldLevel, actions, ItemsMode.QUERY, explicitQuery,
                    Strings.emptyToNull(itemsQuery.searchStringFormat()),
                    itemsQuery.escapeValueForLike(),
                    Strings.emptyToNull(itemsQuery.fetchPlan()));
        }

        // byInstanceName
        if (!Strings.isNullOrEmpty(itemsQuery.searchStringFormat())) {
            log.warn("searchStringFormat of @LookupField itemsQuery for entity '{}' is ignored " +
                    "in byInstanceName mode: matching is always a case-insensitive substring search",
                    metaClass.getName());
        }
        return dropdown(fieldLevel, actions, ItemsMode.BY_INSTANCE_NAME, null, null, false,
                Strings.emptyToNull(itemsQuery.fetchPlan()));
    }

    protected EffectiveLookupConfig dropdown(boolean fieldLevel, List<String> actions, ItemsMode mode,
                                             @Nullable String query, @Nullable String searchStringFormat,
                                             boolean escapeValueForLike, @Nullable String fetchPlanName) {
        return new EffectiveLookupConfig(LookupType.DROPDOWN, fieldLevel, actions, mode,
                query, searchStringFormat, escapeValueForLike, fetchPlanName);
    }

    // Precedence: field annotation actions > entity-field-actions property > class annotation actions
    protected List<String> resolveActions(@Nullable LookupFieldSettings fieldSettings,
                                          @Nullable LookupFieldSettings classSettings,
                                          MetaClass referencedEntityClass) {
        if (fieldSettings != null && !fieldSettings.actions.isEmpty()) {
            return fieldSettings.actions;
        }
        List<String> propertyActions = componentProperties.getEntityFieldActions()
                .get(referencedEntityClass.getName());
        if (propertyActions != null && !propertyActions.isEmpty()) {
            return propertyActions;
        }
        if (classSettings != null && !classSettings.actions.isEmpty()) {
            return classSettings.actions;
        }
        return List.of();
    }

    protected boolean isItemsQueryConfigured(@Nullable LookupItemsQuery itemsQuery) {
        return itemsQuery != null && (itemsQuery.byInstanceName() || !itemsQuery.query().isEmpty());
    }

    @Nullable
    protected LookupFieldSettings readSettings(Map<String, Object> annotations) {
        Map<String, Object> attributes = metadataTools.getMetaAnnotationAttributes(annotations, LookupField.class);
        if (attributes.isEmpty()) {
            return null;
        }
        LookupType type = (LookupType) attributes.get("type");
        String[] actions = (String[]) attributes.get("actions");
        LookupItemsQuery itemsQuery = (LookupItemsQuery) attributes.get("itemsQuery");
        return new LookupFieldSettings(type, actions != null ? List.of(actions) : List.of(), itemsQuery);
    }

    protected static class LookupFieldSettings {

        protected final LookupType type;
        protected final List<String> actions;
        @Nullable
        protected final LookupItemsQuery itemsQuery;

        public LookupFieldSettings(LookupType type, List<String> actions, @Nullable LookupItemsQuery itemsQuery) {
            this.type = type;
            this.actions = actions;
            this.itemsQuery = itemsQuery;
        }
    }
}
