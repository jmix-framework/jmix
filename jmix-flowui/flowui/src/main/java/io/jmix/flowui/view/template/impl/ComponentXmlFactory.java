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

package io.jmix.flowui.view.template.impl;

import io.jmix.core.FileRef;
import io.jmix.core.MetadataTools;
import io.jmix.core.Stores;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.entitypicker.EntityOpenCompositionAction;
import io.jmix.flowui.component.factory.EffectiveLookupConfig;
import io.jmix.flowui.component.factory.EffectiveLookupConfig.ItemsMode;
import io.jmix.flowui.component.factory.ItemsFetchCallbackSupport;
import io.jmix.flowui.component.factory.LookupFieldSupport;
import jakarta.persistence.Lob;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Factory for creating XML representations of UI components based on entity metadata properties.
 *
 * @see #createComponentXml(MetaProperty, String)
 */
@Component("flowui_ComponentXmlFactory")
public class ComponentXmlFactory {

    private static final Logger log = LoggerFactory.getLogger(ComponentXmlFactory.class);

    protected final LookupFieldSupport lookupFieldSupport;
    protected final ItemsFetchCallbackSupport itemsFetchCallbackSupport;
    protected final MetadataTools metadataTools;

    public ComponentXmlFactory(LookupFieldSupport lookupFieldSupport,
                                ItemsFetchCallbackSupport itemsFetchCallbackSupport,
                                MetadataTools metadataTools) {
        this.lookupFieldSupport = lookupFieldSupport;
        this.itemsFetchCallbackSupport = itemsFetchCallbackSupport;
        this.metadataTools = metadataTools;
    }

    /**
     * Creates an XML representation of a UI component for the given entity property.
     * <p>
     * The component type is determined based on the property's range and type. For entity properties,
     * the effective {@code @LookupField} configuration is resolved and used to choose between an
     * {@code entityComboBox} and an {@code entityPicker}, with actions added accordingly. An eager
     * {@code entityComboBox} is bound to a data container via {@code itemsContainer} (see
     * {@link #createItemsContainerXml(MetaProperty)}); a lazy one carries an {@code itemsQuery}.
     *
     * @param metaProperty    the entity property for which to create a component
     * @param dataContainerId optional data container identifier for data binding, may be null
     * @return XML string representation of the component
     * @throws UnsupportedOperationException if the property is collection-valued
     */
    public String createComponentXml(MetaProperty metaProperty, @Nullable String dataContainerId) {
        Range range = metaProperty.getRange();

        if (range.getCardinality().isMany()) {
            throw new UnsupportedOperationException(
                    "Collection-valued property '%s' is not supported".formatted(metaProperty.getName()));
        }

        if (metaProperty.getType() == MetaProperty.Type.EMBEDDED) {
            return "";
        }

        if (range.isClass()) {
            return createReferenceComponentXml(metaProperty, range.asClass(), dataContainerId);
        }

        Element element = createElement(metaProperty);
        initDataBinding(element, metaProperty, dataContainerId);
        return element.asXML();
    }

    protected String createReferenceComponentXml(MetaProperty metaProperty, MetaClass referencedEntity,
                                                  @Nullable String dataContainerId) {
        EffectiveLookupConfig config = lookupFieldSupport.resolve(metaProperty, referencedEntity);

        if (isDropdown(config, referencedEntity)) {
            Element element = DocumentHelper.createElement("entityComboBox");
            initDataBinding(element, metaProperty, dataContainerId);
            if (config.itemsMode() == ItemsMode.EAGER) {
                element.addAttribute("itemsContainer", itemsContainerId(metaProperty));
            } else {
                addItemsQuery(element, config, referencedEntity);
            }
            addResolvedActions(element, config.actions());
            return element.asXML();
        }

        Element element = DocumentHelper.createElement("entityPicker");
        initDataBinding(element, metaProperty, dataContainerId);
        if (config.actions().isEmpty()) {
            addDefaultPickerActions(element, metaProperty);
        } else {
            addResolvedActions(element, config.actions());
        }
        return element.asXML();
    }

    /**
     * A DROPDOWN renders as a combobox only when it can source items in XML: the entity must have a
     * usable store and, for the byInstanceName mode, string instance-name properties. Otherwise it
     * degrades to a view lookup.
     */
    protected boolean isDropdown(EffectiveLookupConfig config, MetaClass referencedEntity) {
        if (config.componentType() != LookupType.DROPDOWN) {
            return false;
        }
        if (Stores.NOOP.equals(referencedEntity.getStore().getName())) {
            log.warn("@LookupField DROPDOWN for entity '{}' degraded to a view lookup in a generated view: " +
                    "the entity has no data store to load items from", referencedEntity.getName());
            return false;
        }
        if (config.itemsMode() == ItemsMode.BY_INSTANCE_NAME
                && itemsFetchCallbackSupport.resolveInstanceNameSearchProperties(referencedEntity).isEmpty()) {
            log.warn("@LookupField byInstanceName DROPDOWN for entity '{}' degraded to a view lookup in a " +
                    "generated view: its instance name is not based on string attributes", referencedEntity.getName());
            return false;
        }
        return true;
    }

    protected boolean isEagerDropdown(EffectiveLookupConfig config, MetaClass referencedEntity) {
        return config.itemsMode() == ItemsMode.EAGER && isDropdown(config, referencedEntity);
    }

    protected String itemsContainerId(MetaProperty metaProperty) {
        return metaProperty.getName() + "ItemsDc";
    }

    /**
     * Creates the top-level {@code collection} data container XML that backs an eager
     * {@code entityComboBox} generated for the given reference property, or an empty string if the
     * property is not an eager dropdown. For JPA entities the loader carries a JPQL query ordered by the
     * instance name; for non-JPA entities the query is omitted and the store loads all instances.
     *
     * @param metaProperty a single-value entity reference property
     * @return the {@code <collection>} XML, or {@code ""} if not an eager dropdown
     */
    public String createItemsContainerXml(MetaProperty metaProperty) {
        Range range = metaProperty.getRange();
        if (!range.isClass()) {
            return "";
        }
        MetaClass referencedEntity = range.asClass();
        EffectiveLookupConfig config = lookupFieldSupport.resolve(metaProperty, referencedEntity);
        if (!isEagerDropdown(config, referencedEntity)) {
            return "";
        }

        String containerId = itemsContainerId(metaProperty);
        Element collection = DocumentHelper.createElement("collection");
        collection.addAttribute("id", containerId);
        collection.addAttribute("class", referencedEntity.getJavaClass().getName());
        collection.addElement("fetchPlan").addAttribute("extends", "_instance_name");

        Element loader = collection.addElement("loader");
        loader.addAttribute("id", metaProperty.getName() + "ItemsDl");
        loader.addAttribute("readOnly", Boolean.TRUE.toString());

        if (metadataTools.isJpaEntity(referencedEntity)) {
            loader.addElement("query").addCDATA(buildItemsQuery(referencedEntity));
        }
        return collection.asXML();
    }

    protected String buildItemsQuery(MetaClass referencedEntity) {
        String orderBy = itemsFetchCallbackSupport.getInstanceNameSortOrders(referencedEntity).stream()
                .map(order -> "e." + order.getProperty())
                .collect(Collectors.joining(", "));
        String query = "select e from " + referencedEntity.getName() + " e";
        return orderBy.isEmpty() ? query : query + " order by " + orderBy;
    }

    protected void addItemsQuery(Element element, EffectiveLookupConfig config, MetaClass referencedEntity) {
        Element itemsQuery = element.addElement("itemsQuery");
        itemsQuery.addAttribute("class", referencedEntity.getJavaClass().getName());
        if (config.fetchPlanName() != null) {
            itemsQuery.addAttribute("fetchPlan", config.fetchPlanName());
        }

        if (config.itemsMode() == ItemsMode.QUERY) {
            if (config.searchStringFormat() != null) {
                itemsQuery.addAttribute("searchStringFormat", config.searchStringFormat());
            }
            if (config.escapeValueForLike()) {
                itemsQuery.addAttribute("escapeValueForLike", Boolean.TRUE.toString());
            }
            itemsQuery.addElement("query").addCDATA(config.query());
        } else {
            // BY_INSTANCE_NAME
            itemsQuery.addAttribute("byInstanceName", Boolean.TRUE.toString());
        }
    }

    protected void addResolvedActions(Element element, List<String> actionIds) {
        if (actionIds.isEmpty()) {
            return;
        }
        Element actions = element.addElement("actions");
        for (String actionId : actionIds) {
            addAction(actions, actionId, actionId);
        }
    }

    protected void addDefaultPickerActions(Element element, MetaProperty metaProperty) {
        if (metaProperty.getType() == MetaProperty.Type.ASSOCIATION) {
            Element actions = element.addElement("actions");
            addAction(actions, "entityLookup", EntityLookupAction.ID);
            addAction(actions, "entityClear", EntityClearAction.ID);
        } else if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
            Element actions = element.addElement("actions");
            addAction(actions, "entityOpenComposition", EntityOpenCompositionAction.ID);
            addAction(actions, "entityClear", EntityClearAction.ID);
        }
    }

    protected Element createElement(MetaProperty metaProperty) {
        Range range = metaProperty.getRange();

        if (range.isDatatype()) {
            return DocumentHelper.createElement(getDatatypeComponentName(metaProperty));
        } else if (range.isEnum()) {
            return DocumentHelper.createElement("select");
        }

        return DocumentHelper.createElement("textField");
    }

    protected String getDatatypeComponentName(MetaProperty metaProperty) {
        Class<?> type = metaProperty.getRange().asDatatype().getJavaClass();

        if (type.equals(String.class) || type.equals(UUID.class)) {
            return isLob(metaProperty) ? "textArea" : "textField";
        } else if (type.equals(Boolean.class)) {
            return "checkbox";
        } else if (type.equals(java.sql.Date.class) || type.equals(LocalDate.class)) {
            return "datePicker";
        } else if (type.equals(Time.class) || type.equals(LocalTime.class) || type.equals(OffsetTime.class)) {
            return "timePicker";
        } else if (type.equals(Date.class) || type.equals(LocalDateTime.class) || type.equals(OffsetDateTime.class)) {
            return "dateTimePicker";
        } else if (Number.class.isAssignableFrom(type)) {
            return "textField";
        } else if (type.equals(FileRef.class)) {
            return "fileStorageUploadField";
        } else if (type.equals(byte[].class)) {
            return "fileUploadField";
        }

        return isLob(metaProperty) ? "textArea" : "textField";
    }

    protected boolean isLob(MetaProperty metaProperty) {
        return metaProperty.getAnnotatedElement().getAnnotation(Lob.class) != null;
    }

    protected void initDataBinding(Element element, MetaProperty metaProperty, @Nullable String dataContainerId) {
        element.addAttribute("id", metaProperty.getName() + "Field");
        element.addAttribute("property", metaProperty.getName());

        if (dataContainerId != null) {
            element.addAttribute("dataContainer", dataContainerId);
        }

        if ("fileStorageUploadField".equals(element.getName()) || "fileUploadField".equals(element.getName())) {
            element.addAttribute("fileNameVisible", Boolean.TRUE.toString());
            element.addAttribute("clearButtonVisible", Boolean.TRUE.toString());
        }
    }

    protected void addAction(Element actions, String id, String type) {
        actions.addElement("action")
                .addAttribute("id", id)
                .addAttribute("type", type);
    }
}
