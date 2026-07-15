/*
 * Copyright 2022 Haulmont.
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
import com.vaadin.flow.component.Component;
import io.jmix.core.*;
import io.jmix.core.entity.annotation.LookupField;
import io.jmix.core.entity.annotation.LookupItemsQuery;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.flowui.Actions;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.action.entitypicker.EntityClearAction;
import io.jmix.flowui.action.entitypicker.EntityLookupAction;
import io.jmix.flowui.action.entitypicker.EntityOpenCompositionAction;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.component.SupportsItemsFetchCallback;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes"})
@org.springframework.stereotype.Component("flowui_EntityFieldCreationSupport")
public class EntityFieldCreationSupport {

    private static final Logger log = LoggerFactory.getLogger(EntityFieldCreationSupport.class);

    protected static final String SEARCH_STRING_PARAMETER_REF = ":searchString";

    protected final UiComponents uiComponents;
    protected final Actions actions;
    protected final MetadataTools metadataTools;
    protected final UiComponentProperties componentProperties;
    protected final DataComponents dataComponents;
    protected final DataManager dataManager;
    protected final ApplicationContext applicationContext;
    protected final ItemsFetchCallbackSupport itemsFetchCallbackSupport;
    protected final FetchPlanRepository fetchPlanRepository;

    public EntityFieldCreationSupport(UiComponents uiComponents,
                                      Actions actions,
                                      MetadataTools metadataTools,
                                      UiComponentProperties componentProperties,
                                      DataComponents dataComponents,
                                      DataManager dataManager, ApplicationContext applicationContext,
                                      ItemsFetchCallbackSupport itemsFetchCallbackSupport,
                                      FetchPlanRepository fetchPlanRepository) {
        this.uiComponents = uiComponents;
        this.actions = actions;
        this.metadataTools = metadataTools;
        this.componentProperties = componentProperties;
        this.dataComponents = dataComponents;
        this.dataManager = dataManager;
        this.applicationContext = applicationContext;
        this.itemsFetchCallbackSupport = itemsFetchCallbackSupport;
        this.fetchPlanRepository = fetchPlanRepository;
    }

    @Nullable
    public Component createEntityField(ComponentGenerationContext context) {
        return createEntityField(context, false);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public Component createEntityField(ComponentGenerationContext context,
                                       boolean considerComposition) {
        MetaClass metaClass = context.getMetaClass();
        MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(
                metaClass,
                context.getProperty()
        );

        if (metaPropertyPath == null) {
            return null;
        }

        MetaClass propertyMetaClass = metaPropertyPath.getMetaProperty().getRange().asClass();
        CollectionContainer<?> collectionItems = context.getCollectionItems();

        LookupFieldSettings fieldSettings =
                resolveLookupFieldSettings(metaPropertyPath.getMetaProperty().getAnnotations());
        LookupFieldSettings classSettings = resolveLookupFieldSettings(propertyMetaClass.getAnnotations());
        String componentFqn = componentProperties.getEntityFieldFqn().get(propertyMetaClass.getName());

        // Precedence: field-level annotation > application property > class-level annotation > defaults
        LookupFieldSettings settings = fieldSettings != null
                ? fieldSettings
                : (componentFqn == null ? classSettings : null);

        EntityPickerComponent field;
        DropdownItemsConfig itemsConfig = null;

        if (settings != null) {
            itemsConfig = settings.type == LookupType.DROPDOWN && collectionItems == null
                    ? resolveDropdownItemsConfig(settings.itemsQuery, propertyMetaClass)
                    : null;
            field = createComponentByLookupType(
                    resolveEffectiveLookupType(settings, propertyMetaClass, collectionItems != null));
        } else if (componentFqn != null) {
            Class<?> aClass = applicationContext.getBean(ClassManager.class).loadClass(componentFqn);
            if (!Component.class.isAssignableFrom(aClass)) {
                throw new DevelopmentException("Class '%s' is not a component".formatted(componentFqn));
            }

            // cast is required for the compiler
            //noinspection RedundantCast
            field = (Component & EntityPickerComponent) uiComponents.create(aClass.asSubclass(Component.class));
        } else if (collectionItems != null) {
            field = uiComponents.create(EntityComboBox.class);
        } else {
            field = uiComponents.create(EntityPicker.class);
        }

        if (field instanceof SupportsItemsContainer supportsItemsContainer) {
            if (collectionItems != null) {
                if (!collectionItems.getEntityMetaClass().equals(propertyMetaClass)) {
                    throw new IllegalStateException("Wrong collection metaClass provided for container");
                }
                supportsItemsContainer.setItems(collectionItems);
            } else if (itemsConfig instanceof QueryItemsConfig queryConfig
                    && field instanceof SupportsItemsFetchCallback fetchCallbackField) {
                //noinspection unchecked
                fetchCallbackField.setItemsFetchCallback(itemsFetchCallbackSupport.createEntityFetchCallback(
                        propertyMetaClass.getJavaClass(), queryConfig.query(), queryConfig.searchStringFormat(),
                        queryConfig.escapeValueForLike(), queryConfig.fetchPlan()));
            } else if (itemsConfig instanceof ConditionItemsConfig conditionConfig
                    && field instanceof SupportsItemsFetchCallback fetchCallbackField) {
                //noinspection unchecked
                fetchCallbackField.setItemsFetchCallback(itemsFetchCallbackSupport.createEntityFetchCallback(
                        propertyMetaClass.getJavaClass(),
                        searchString -> buildInstanceNameCondition(conditionConfig.searchProperties(), searchString),
                        conditionConfig.sort(), conditionConfig.fetchPlan()));
            } else {
                supportsItemsContainer.setItems(createCollectionContainer(propertyMetaClass));
            }
        }

        field.setMetaClass(propertyMetaClass);
        createFieldActions(propertyMetaClass, metaPropertyPath.getMetaProperty().getType(), field,
                considerComposition, resolveLookupActions(fieldSettings, classSettings, propertyMetaClass));

        return (Component) field;
    }

    protected void createFieldActions(MetaClass metaClass, MetaProperty.Type metaPropertyType,
                                      EntityPickerComponent field, boolean considerComposition,
                                      List<String> resolvedActionIds) {
        if (resolvedActionIds.isEmpty()) {
            if (!(field instanceof EntityComboBox)) {
                if (metaPropertyType == MetaProperty.Type.ASSOCIATION || considerComposition) {
                    field.addAction(actions.create(EntityLookupAction.ID));
                    field.addAction(actions.create(EntityClearAction.ID));
                } else if (metaPropertyType == MetaProperty.Type.COMPOSITION) {
                    field.addAction(actions.create(EntityOpenCompositionAction.ID));
                    field.addAction(actions.create(EntityClearAction.ID));
                }
            }
        } else {
            for (String actionId : resolvedActionIds) {
                field.addAction(actions.create(actionId));
            }
        }
    }

    // Precedence: field annotation actions > application property > class annotation actions
    protected List<String> resolveLookupActions(@Nullable LookupFieldSettings fieldSettings,
                                                @Nullable LookupFieldSettings classSettings,
                                                MetaClass propertyMetaClass) {
        if (fieldSettings != null && !fieldSettings.actions.isEmpty()) {
            return fieldSettings.actions;
        }
        List<String> propertyActions = componentProperties.getEntityFieldActions()
                .get(propertyMetaClass.getName());
        if (propertyActions != null && !propertyActions.isEmpty()) {
            return propertyActions;
        }
        if (classSettings != null && !classSettings.actions.isEmpty()) {
            return classSettings.actions;
        }
        return List.of();
    }

    protected EntityPickerComponent<?> createComponentByLookupType(LookupType type) {
        return type == LookupType.DROPDOWN
                ? uiComponents.create(EntityComboBox.class)
                : uiComponents.create(EntityPicker.class);
    }

    protected LookupType resolveEffectiveLookupType(LookupFieldSettings settings,
                                                    MetaClass propertyMetaClass,
                                                    boolean hasCollectionItems) {
        if (settings.type == LookupType.VIEW) {
            if (isItemsQueryConfigured(settings.itemsQuery)) {
                log.warn("itemsQuery of @LookupField is ignored for type VIEW (entity '{}')",
                        propertyMetaClass.getName());
            }
            return LookupType.VIEW;
        }

        // DROPDOWN: if the entity has no usable store, items cannot be loaded at all
        // (eagerly or lazily), degrade to a view lookup which requires no items source
        if (!hasCollectionItems
                && Stores.NOOP.equals(propertyMetaClass.getStore().getName())) {
            log.warn("@LookupField DROPDOWN for entity '{}' degraded to a view lookup: " +
                    "the entity has no data store to load items from", propertyMetaClass.getName());
            return LookupType.VIEW;
        }

        return LookupType.DROPDOWN;
    }

    protected boolean isItemsQueryConfigured(@Nullable LookupItemsQuery itemsQuery) {
        return itemsQuery != null && (itemsQuery.byInstanceName() || !itemsQuery.query().isEmpty());
    }

    @Nullable
    protected DropdownItemsConfig resolveDropdownItemsConfig(@Nullable LookupItemsQuery itemsQuery,
                                                             MetaClass metaClass) {
        if (itemsQuery == null || !isItemsQueryConfigured(itemsQuery)) {
            return null;
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
                return null;
            }
            return new QueryItemsConfig(explicitQuery,
                    Strings.emptyToNull(itemsQuery.searchStringFormat()),
                    itemsQuery.escapeValueForLike(),
                    loadItemsFetchPlan(metaClass, itemsQuery.fetchPlan(), null));
        }

        // byInstanceName
        if (!Strings.isNullOrEmpty(itemsQuery.searchStringFormat())) {
            log.warn("searchStringFormat of @LookupField itemsQuery for entity '{}' is ignored " +
                    "in byInstanceName mode: matching is always a case-insensitive substring search",
                    metaClass.getName());
        }
        List<String> searchProperties = resolveInstanceNameSearchProperties(metaClass);
        if (searchProperties.isEmpty()) {
            log.warn("Cannot build @LookupField items condition for entity '{}': its instance name " +
                    "is not based on string attributes, items are loaded eagerly", metaClass.getName());
            return null;
        }
        return new ConditionItemsConfig(searchProperties, Sort.by(getInstanceNameSortOrders(metaClass)),
                loadItemsFetchPlan(metaClass, itemsQuery.fetchPlan(), FetchPlan.INSTANCE_NAME));
    }

    @Nullable
    protected FetchPlan loadItemsFetchPlan(MetaClass metaClass, String fetchPlanName,
                                           @Nullable String defaultFetchPlanName) {
        String name = Strings.isNullOrEmpty(fetchPlanName) ? defaultFetchPlanName : fetchPlanName;
        return name == null ? null : fetchPlanRepository.getFetchPlan(metaClass, name);
    }

    /**
     * Returns names of string-typed instance-name-related properties of the given metaClass,
     * used as the search properties for a byInstanceName condition.
     */
    protected List<String> resolveInstanceNameSearchProperties(MetaClass metaClass) {
        return metadataTools.getInstanceNameRelatedProperties(metaClass, true)
                .stream()
                .filter(property -> property.getRange().isDatatype()
                        && String.class.equals(property.getRange().asDatatype().getJavaClass()))
                .map(MetaProperty::getName)
                .toList();
    }

    /**
     * Builds a case-insensitive substring-match condition over the given search properties,
     * combined with "or" if there are several.
     */
    protected Condition buildInstanceNameCondition(List<String> searchProperties, String searchString) {
        String escaped = QueryUtils.escapeForLike(searchString);
        if (searchProperties.size() == 1) {
            return PropertyCondition.contains(searchProperties.get(0), escaped);
        }
        return LogicalCondition.or(searchProperties.stream()
                .map(property -> (Condition) PropertyCondition.contains(property, escaped))
                .toArray(Condition[]::new));
    }

    @Nullable
    protected LookupFieldSettings resolveLookupFieldSettings(Map<String, Object> annotations) {
        Map<String, Object> attributes = metadataTools.getMetaAnnotationAttributes(annotations, LookupField.class);
        if (attributes.isEmpty()) {
            return null;
        }

        LookupType type = (LookupType) attributes.get("type");
        String[] actions = (String[]) attributes.get("actions");
        LookupItemsQuery itemsQuery = (LookupItemsQuery) attributes.get("itemsQuery");

        return new LookupFieldSettings(type,
                actions != null ? List.of(actions) : List.of(),
                itemsQuery);
    }

    @SuppressWarnings("unchecked")
    protected CollectionContainer createCollectionContainer(MetaClass metaClass) {
        CollectionContainer container = dataComponents.createCollectionContainer(metaClass.getJavaClass());

        List list = dataManager.load(metaClass.getJavaClass())
                .all()
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .sort(Sort.by(getInstanceNameSortOrders(metaClass)))
                .list();
        container.setItems(list);
        return container;
    }

    protected List<Sort.Order> getInstanceNameSortOrders(MetaClass metaClass) {
        return metadataTools.getInstanceNameRelatedProperties(metaClass, true)
                .stream()
                .filter(metaProperty -> !metaProperty.getRange().isClass())
                .map(metaProperty -> Sort.Order.asc(metaProperty.getName()))
                .collect(Collectors.toList());
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

    /**
     * Configuration of lazily loaded DROPDOWN items, either built from an explicit JPQL query
     * ({@link QueryItemsConfig}) or from instance-name-based query conditions
     * ({@link ConditionItemsConfig}).
     */
    protected sealed interface DropdownItemsConfig {

        @Nullable
        FetchPlan fetchPlan();
    }

    protected record QueryItemsConfig(String query, @Nullable String searchStringFormat,
                                      boolean escapeValueForLike, @Nullable FetchPlan fetchPlan)
            implements DropdownItemsConfig {
    }

    protected record ConditionItemsConfig(List<String> searchProperties, Sort sort, @Nullable FetchPlan fetchPlan)
            implements DropdownItemsConfig {
    }
}
