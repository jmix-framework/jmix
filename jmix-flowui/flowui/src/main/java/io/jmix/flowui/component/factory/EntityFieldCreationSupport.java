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
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
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
import io.jmix.flowui.component.factory.EffectiveLookupConfig.ItemsMode;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.data.SupportsItemsContainer;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.DataComponents;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SuppressWarnings({"rawtypes"})
@org.springframework.stereotype.Component("flowui_EntityFieldCreationSupport")
public class EntityFieldCreationSupport {

    private static final Logger log = LoggerFactory.getLogger(EntityFieldCreationSupport.class);

    protected final UiComponents uiComponents;
    protected final Actions actions;
    protected final MetadataTools metadataTools;
    protected final UiComponentProperties componentProperties;
    protected final DataComponents dataComponents;
    protected final DataManager dataManager;
    protected final ApplicationContext applicationContext;
    protected final ItemsFetchCallbackSupport itemsFetchCallbackSupport;
    protected final FetchPlanRepository fetchPlanRepository;
    protected final LookupFieldSupport lookupFieldSupport;

    public EntityFieldCreationSupport(UiComponents uiComponents,
                                      Actions actions,
                                      MetadataTools metadataTools,
                                      UiComponentProperties componentProperties,
                                      DataComponents dataComponents,
                                      DataManager dataManager, ApplicationContext applicationContext,
                                      ItemsFetchCallbackSupport itemsFetchCallbackSupport,
                                      FetchPlanRepository fetchPlanRepository,
                                      LookupFieldSupport lookupFieldSupport) {
        this.uiComponents = uiComponents;
        this.actions = actions;
        this.metadataTools = metadataTools;
        this.componentProperties = componentProperties;
        this.dataComponents = dataComponents;
        this.dataManager = dataManager;
        this.applicationContext = applicationContext;
        this.itemsFetchCallbackSupport = itemsFetchCallbackSupport;
        this.fetchPlanRepository = fetchPlanRepository;
        this.lookupFieldSupport = lookupFieldSupport;
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

        EffectiveLookupConfig config = lookupFieldSupport.resolve(
                metaPropertyPath.getMetaProperty(), propertyMetaClass);
        String componentFqn = componentProperties.getEntityFieldFqn().get(propertyMetaClass.getName());

        // Field-level annotation wins over the entity-field-fqn property; the property wins over a
        // class-level annotation.
        boolean useAnnotationComponent = config.componentType() != null
                && (config.fieldLevel() || componentFqn == null);

        EntityPickerComponent field;
        DropdownItemsConfig itemsConfig = null;

        if (useAnnotationComponent) {
            itemsConfig = config.componentType() == LookupType.DROPDOWN && collectionItems == null
                    ? resolveDropdownItemsConfig(config, propertyMetaClass)
                    : null;
            field = createComponentByLookupType(
                    resolveEffectiveLookupType(config, propertyMetaClass, collectionItems != null));
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
                        searchString -> itemsFetchCallbackSupport.buildInstanceNameCondition(
                                conditionConfig.searchProperties(), searchString),
                        conditionConfig.sort(), conditionConfig.fetchPlan()));
            } else {
                supportsItemsContainer.setItems(createCollectionContainer(propertyMetaClass));
            }
        }

        field.setMetaClass(propertyMetaClass);
        createFieldActions(propertyMetaClass, metaPropertyPath.getMetaProperty().getType(), field,
                considerComposition, config.actions());

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

    protected EntityPickerComponent<?> createComponentByLookupType(LookupType type) {
        return type == LookupType.DROPDOWN
                ? uiComponents.create(EntityComboBox.class)
                : uiComponents.create(EntityPicker.class);
    }

    protected LookupType resolveEffectiveLookupType(EffectiveLookupConfig config,
                                                    MetaClass propertyMetaClass,
                                                    boolean hasCollectionItems) {
        if (config.componentType() == LookupType.VIEW) {
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

    @Nullable
    protected DropdownItemsConfig resolveDropdownItemsConfig(EffectiveLookupConfig config, MetaClass metaClass) {
        if (config.itemsMode() == ItemsMode.QUERY) {
            return new QueryItemsConfig(config.query(),
                    config.searchStringFormat(),
                    config.escapeValueForLike(),
                    loadItemsFetchPlan(metaClass, config.fetchPlanName(), null));
        }
        if (config.itemsMode() == ItemsMode.BY_INSTANCE_NAME) {
            List<String> searchProperties = itemsFetchCallbackSupport.resolveInstanceNameSearchProperties(metaClass);
            if (searchProperties.isEmpty()) {
                log.warn("Cannot build @LookupField items condition for entity '{}': its instance name " +
                        "is not based on string attributes, items are loaded eagerly", metaClass.getName());
                return null;
            }
            return new ConditionItemsConfig(searchProperties,
                    Sort.by(itemsFetchCallbackSupport.getInstanceNameSortOrders(metaClass)),
                    loadItemsFetchPlan(metaClass, config.fetchPlanName(), FetchPlan.INSTANCE_NAME));
        }
        return null; // EAGER
    }

    @Nullable
    protected FetchPlan loadItemsFetchPlan(MetaClass metaClass, String fetchPlanName,
                                           @Nullable String defaultFetchPlanName) {
        String name = Strings.isNullOrEmpty(fetchPlanName) ? defaultFetchPlanName : fetchPlanName;
        return name == null ? null : fetchPlanRepository.getFetchPlan(metaClass, name);
    }

    @SuppressWarnings("unchecked")
    protected CollectionContainer createCollectionContainer(MetaClass metaClass) {
        CollectionContainer container = dataComponents.createCollectionContainer(metaClass.getJavaClass());

        List list = dataManager.load(metaClass.getJavaClass())
                .all()
                .fetchPlan(FetchPlan.INSTANCE_NAME)
                .sort(Sort.by(itemsFetchCallbackSupport.getInstanceNameSortOrders(metaClass)))
                .list();
        container.setItems(list);
        return container;
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
