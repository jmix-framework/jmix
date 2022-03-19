/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.dynamicattributes;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.dynattr.*;
import io.jmix.dynattr.model.Categorized;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrui.impl.AttributeValidators;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.core.entity.EntitySystemAccess.getExtraState;
import static java.lang.String.format;

@Component(DynamicAttributesGuiTools.NAME)
public class DynamicAttributesGuiTools {
    public static final String NAME = "cuba_DynamicAttributesGuiTools";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DynAttrMetadata dynAttrMetadata;
    @Autowired
    protected Security security;
    @Autowired
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected AttributeValidators attributeValidators;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected FetchPlans fetchPlans;

    /**
     * Method checks whether any class in the view hierarchy contains dynamic attributes that must be displayed on the
     * current screen
     */
    public boolean screenContainsDynamicAttributes(FetchPlan fetchPlan, String screenId) {
        Set<Class> classesWithDynamicAttributes = collectEntityClassesWithDynamicAttributes(fetchPlan);
        for (Class classWithDynamicAttributes : classesWithDynamicAttributes) {
            MetaClass metaClass = metadata.getClassNN(classWithDynamicAttributes);
            if (!getAttributesToShowOnTheScreen(metaClass, screenId, null).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public void initDefaultAttributeValues(Entity item, MetaClass metaClass) {
        Preconditions.checkNotNullArgument(metaClass, "metaClass is null");
        Collection<AttributeDefinition> attributes = dynAttrMetadata.getAttributes(metaClass);

        DynamicAttributesState state = getExtraState(item, DynamicAttributesState.class);
        if (state != null) {
            DynamicAttributes dynamicAttributes = state.getDynamicAttributes();
            if (dynamicAttributes == null) {
                state.setDynamicAttributes(new DynamicAttributes());
            }
        }

        ZonedDateTime currentTimestamp = AppBeans.get(TimeSource.class).now();
        boolean entityIsCategorized = item instanceof Categorized && ((Categorized) item).getCategory() != null;

        for (AttributeDefinition attribute : attributes) {
            setDefaultAttributeValue(item, attribute, entityIsCategorized, currentTimestamp);
        }
    }

    public void listenCategoryChanges(Datasource ds) {
        ds.addItemPropertyChangeListener(e -> {
            if ("category".equals(e.getProperty())) {
                initDefaultAttributeValues(e.getItem(), metadata.getClassNN(e.getItem().getClass()));
            }
        });
    }

    /**
     * Reload dynamic attributes on the entity
     */
    @SuppressWarnings("unchecked")
    public void reloadDynamicAttributes(Entity entity) {
        MetaClass metaClass = metadata.getClassNN(entity.getClass());
        //noinspection ConstantConditions
        FetchPlan fetchPlan = fetchPlans.builder(metaClass.getJavaClass())
                .add(metadataTools.getPrimaryKeyName(metaClass))
                .build();
        LoadContext loadContext = new LoadContext(metaClass)
                .setFetchPlan(fetchPlan)
                .setHint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                .setId(EntityValues.getId(entity));
        Entity reloadedEntity = (Entity) dataManager.load(loadContext);
        if (reloadedEntity != null) {
            DynamicAttributesState state = getExtraState(entity, DynamicAttributesState.class);
            DynamicAttributesState reloadedState = getExtraState(reloadedEntity, DynamicAttributesState.class);

            if (state != null && reloadedState != null) {
                //noinspection ConstantConditions
                state.setDynamicAttributes(reloadedState.getDynamicAttributes());
            }
        }
    }

    public boolean hasDynamicAttributes(Entity entity) {
        DynamicAttributesState state = getExtraState(entity, DynamicAttributesState.class);
        if (state != null) {
            return state.getDynamicAttributes() != null;
        }
        return false;
    }

    public Set<AttributeDefinition> getAttributesToShowOnTheScreen(MetaClass metaClass, String windowId, String componentId) {
        Collection<AttributeDefinition> attributes = dynAttrMetadata.getAttributes(metaClass);

        Set<AttributeDefinition> result = new LinkedHashSet<>();

        for (AttributeDefinition attribute : attributes) {
            if (attributeShouldBeShownOnTheScreen(windowId, componentId, attribute)) {
                result.add(attribute);
            }
        }

        return result;
    }

    /**
     * Returns validators for a dynamic attribute
     *
     * @return collection of validators
     */
    public Collection<Consumer<?>> createValidators(AttributeDefinition attribute) {
        return attributeValidators.getValidators(attribute).stream()
                .map(validator -> (Consumer<?>) validator)
                .collect(Collectors.toList());
    }

    public void listenDynamicAttributesChanges(Datasource datasource) {
        if (datasource != null && datasource.getLoadDynamicAttributes()) {
            datasource.addItemPropertyChangeListener(e -> {
                if (DynAttrUtils.isDynamicAttributeProperty(e.getProperty())) {
                    ((DatasourceImplementation) datasource).modified(e.getItem());
                }
            });
        }
    }

    protected Set<Class> collectEntityClassesWithDynamicAttributes(@Nullable FetchPlan fetchPlan) {
        if (fetchPlan == null) {
            return Collections.emptySet();
        }
        return collectEntityClasses(fetchPlan, new HashSet<>()).stream()
                .filter(aClass -> !dynAttrMetadata.getAttributes(metadata.getClassNN(aClass)).isEmpty())
                .collect(Collectors.toSet());
    }

    protected Set<Class> collectEntityClasses(FetchPlan fetchPlan, Set<FetchPlan> visited) {
        if (visited.contains(fetchPlan)) {
            return Collections.emptySet();
        } else {
            visited.add(fetchPlan);
        }

        HashSet<Class> classes = new HashSet<>();
        classes.add(fetchPlan.getEntityClass());
        for (FetchPlanProperty fetchPlanProperty : fetchPlan.getProperties()) {
            if (fetchPlanProperty.getFetchPlan() != null) {
                classes.addAll(collectEntityClasses(fetchPlanProperty.getFetchPlan(), visited));
            }
        }
        return classes;
    }

    protected boolean attributeShouldBeShownOnTheScreen(String screen, String component, AttributeDefinition attribute) {
        Set<String> screens = attribute.getConfiguration().getScreens();
        return (screens.contains(screen) || screens.contains(screen + "#" + component))
                && checkUserPermissionForAttribute(attribute);
    }

    protected boolean checkUserPermissionForAttribute(AttributeDefinition attribute) {
        if (attribute.getDataType() != AttributeType.ENTITY) {
            return true;
        }
        MetaClass entityClass = metadata.getClassNN(attribute.getJavaType());
        return security.isEntityOpPermitted(entityClass, EntityOp.READ);
    }

    protected void setDefaultAttributeValue(Entity item, AttributeDefinition attribute,
                                            boolean entityIsCategorized, ZonedDateTime currentTimestamp) {
        String propertyName = DynAttrUtils.getPropertyFromAttributeCode(attribute.getCode());
        if (entityIsCategorized) {
            Category entityCategory = ((Categorized) item).getCategory();
            Category attributeCategory = ((CategoryAttribute) attribute.getSource()).getCategory();
            if (!Objects.equals(entityCategory, attributeCategory)) {
                EntityValues.setValue(item, propertyName, null);//cleanup attributes from not dedicated category
                return;
            }
        }

        if (EntityValues.getValue(item, propertyName) != null) {
            return;//skip not null attributes
        }

        if (attribute.getDefaultValue() != null) {
            if (attribute.getDataType() == AttributeType.ENTITY) {
                MetaClass entityMetaClass = metadata.getClassNN(attribute.getJavaType());
                LoadContext<Object> lc = new LoadContext<>(entityMetaClass).setFetchPlan(
                        fetchPlanRepository.getFetchPlan(entityMetaClass, FetchPlan.INSTANCE_NAME));
                String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(entityMetaClass);
                lc.setQueryString(format("select e from %s e where e.%s = :entityId", entityMetaClass.getName(), pkName))
                        .setParameter("entityId", attribute.getDefaultValue());
                Entity defaultEntity = (Entity) dataManager.load(lc);
                EntityValues.setValue(item, propertyName, defaultEntity);
            } else if (attribute.isCollection()) {
                List<Object> list = new ArrayList<>();
                list.add(attribute.getDefaultValue());
                EntityValues.setValue(item, propertyName, list);
            } else {
                EntityValues.setValue(item, propertyName, attribute.getDefaultValue());
            }
        } else if (Boolean.TRUE.equals(attribute.isDefaultDateCurrent())) {
            if (attribute.getDataType() == AttributeType.DATE_WITHOUT_TIME) {
                EntityValues.setValue(item, propertyName, currentTimestamp.toLocalDate());
            } else {
                EntityValues.setValue(item, propertyName, Date.from(currentTimestamp.toInstant()));
            }
        }
    }

    /**
     * Creates the lookup action that will open the lookup screen with the dynamic filter applied. This filter contains
     * a condition with join and where clauses
     */
    public PickerField.LookupAction createLookupAction(PickerField pickerField,
                                                       String joinClause,
                                                       String whereClause) {
        FilteringLookupAction filteringLookupAction = new FilteringLookupAction(pickerField, joinClause, whereClause);
        Map<String, Object> params = new HashMap<>();
        WindowParams.DISABLE_RESUME_SUSPENDED.set(params, true);
        WindowParams.DISABLE_AUTO_REFRESH.set(params, true);
        filteringLookupAction.setLookupScreenParams(params);
        return filteringLookupAction;
    }

    /**
     * Creates the collection datasource that is used for selecting the dynamic attribute value. If the
     * CategoryAttribute has "where" or "join" clauses then only items that satisfy these clauses will be presented in
     * the options datasource
     */
    public CollectionDatasource createOptionsDatasourceForLookup(MetaClass metaClass, String joinClause, String whereClause) {
        CollectionDatasource optionsDatasource = DsBuilder.create()
                .setMetaClass(metaClass)
                .setViewName(View.MINIMAL)
                .buildCollectionDatasource();

        String query = "select e from " + metaClass.getName() + " e";

        if (!Strings.isNullOrEmpty(joinClause)) {
            query += " " + joinClause;
        }
        if (!Strings.isNullOrEmpty(whereClause)) {
            query += " where " + whereClause.replaceAll("\\{E\\}", "e");
        }

        optionsDatasource.setQuery(query);
        optionsDatasource.refresh();
        return optionsDatasource;
    }
}