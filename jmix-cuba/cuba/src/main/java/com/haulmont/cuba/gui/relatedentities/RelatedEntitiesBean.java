/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.relatedentities;

import com.haulmont.cuba.core.app.RelatedEntitiesService;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.CubaComponentsHelper;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.components.Frame.NotificationType;
import com.haulmont.cuba.gui.components.filter.ConditionParamBuilder;
import com.haulmont.cuba.gui.components.filter.ConditionsTree;
import com.haulmont.cuba.gui.components.filter.FilterParser;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.condition.CustomCondition;
import com.haulmont.cuba.gui.components.filter.condition.PropertyCondition;
import com.haulmont.cuba.gui.components.filter.descriptor.PropertyConditionDescriptor;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.data.impl.DsContextImplementation;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import com.haulmont.cuba.security.entity.FilterEntity;
import com.haulmont.cuba.web.sys.CubaScreens;
import io.jmix.core.Entity;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.datastruct.Node;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.FilterImplementation;
import com.haulmont.cuba.core.global.filter.Op;
import io.jmix.ui.relatedentities.RelatedEntitiesSupport;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.sys.ValuePathHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@Component(RelatedEntitiesAPI.NAME)
public class RelatedEntitiesBean implements RelatedEntitiesAPI {

    @Inject
    protected RelatedEntitiesSupport relatedEntitiesSupport;

    @Inject
    protected Messages messages;
    @Inject
    protected MessageTools messageTools;
    @Inject
    protected WindowConfig windowConfig;
    @Inject
    protected WindowManagerProvider windowManagerProvider;
    @Inject
    protected UserSessionSource userSessionSource;
    @Inject
    protected Metadata metadata;
    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected ExtendedEntities extendedEntities;
    @Inject
    protected RelatedEntitiesService relatedEntitiesService;
    @Inject
    protected FilterParser filterParser;
    @Inject
    protected ConditionParamBuilder paramBuilder;

    @Override
    public RelatedEntitiesBuilder builder(FrameOwner frameOwner) {
        return new RelatedEntitiesBuilder(relatedEntitiesSupport.builder(frameOwner));
    }

    @Override
    public <T extends Entity> void openRelatedScreen(Collection<T> selectedEntities,
                                                     Class<T> clazz, String property,
                                                     RelatedScreenDescriptor descriptor) {
        checkNotNullArgument(clazz, "Class can't be null");

        if (StringUtils.isEmpty(property)) {
            throw new IllegalArgumentException("Property can't be null");
        }

        MetaClass metaClass = metadata.getClassNN(clazz);
        MetaProperty metaProperty = metaClass.getProperty(property);

        openRelatedScreen(selectedEntities, metaClass, metaProperty, descriptor);
    }

    @Override
    public void openRelatedScreen(Collection<? extends Entity> selectedEntities,
                                  MetaClass metaClass, MetaProperty metaProperty,
                                  @Nullable RelatedScreenDescriptor descriptor) {
        checkNotNullArgument(metaClass, "MetaClass can't be null");
        checkNotNullArgument(metaProperty, "MetaProperty can't be null");

        WindowManager windowManager = windowManagerProvider.get();
        if (!selectedEntities.isEmpty()) {
            Map<String, Object> params = new HashMap<>();

            WindowParams.DISABLE_AUTO_REFRESH.set(params, true);
            WindowParams.DISABLE_RESUME_SUSPENDED.set(params, true);

            if (descriptor != null && descriptor.getScreenParams() != null) {
                params.putAll(descriptor.getScreenParams());
            }

            String screenId;
            if (descriptor != null && StringUtils.isNotEmpty(descriptor.getScreenId())) {
                screenId = descriptor.getScreenId();
            } else {
                screenId = windowConfig.getBrowseScreenId(metaProperty.getRange().asClass());
            }

            if (StringUtils.isEmpty(screenId)) {
                String message = String.format("Can't show related entities: passed screenId is null and " +
                        "there is no default browse screen for %s", metaClass.getName());
                throw new IllegalStateException(message);
            }


            WindowManager.OpenType openType = WindowManager.OpenType.THIS_TAB;
            if (descriptor != null) {
                openType = descriptor.getOpenType();
            }

            Screen screen = ((CubaScreens) windowManager).create(screenId,
                    openType.getOpenMode(),
                    new MapScreenOptions(params));

            boolean found = ComponentsHelper.walkComponents(screen.getWindow(), screenComponent -> {
                if (!(screenComponent instanceof Filter)) {
                    return false;
                } else {
                    MetaClass actualMetaClass = ((FilterImplementation) screenComponent).getEntityMetaClass();
                    MetaClass relatedMetaClass = metaProperty.getRange().asClass();
                    MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(relatedMetaClass);
                    if (Objects.equals(actualMetaClass, effectiveMetaClass)) {
                        MetaDataDescriptor metaDataDescriptor = new MetaDataDescriptor(metaClass, metaProperty);

                        applyFilter(((Filter) screenComponent), selectedEntities, descriptor, metaDataDescriptor);
                        return true;
                    }
                    return false;
                }
            });

            screen.show();

            if (!found) {
                windowManager.showNotification(messages.getMainMessage("actions.Related.FilterNotFound"),
                        NotificationType.WARNING);
            }
            if (screen instanceof LegacyFrame) {
                LegacyFrame legacyFrame = (LegacyFrame) screen;
                ((DsContextImplementation) legacyFrame.getDsContext()).resumeSuspended();
            }
        } else {
            windowManager.showNotification(messages.getMainMessage("actions.Related.NotSelected"),
                    NotificationType.HUMANIZED);
        }
    }

    protected void applyFilter(Filter component, Collection<? extends Entity> selectedParents,
                               RelatedScreenDescriptor descriptor, MetaDataDescriptor metaDataDescriptor) {
        FilterEntity filterEntity = metadata.create(FilterEntity.class);
        filterEntity.setComponentId(CubaComponentsHelper.getFilterComponentPath(component));

        if (StringUtils.isNotEmpty(descriptor.getFilterCaption())) {
            filterEntity.setName(descriptor.getFilterCaption());
        } else {
            MetaProperty metaProperty = metaDataDescriptor.getMetaProperty();
            filterEntity.setName(messages.getMainMessage("actions.Related.Filter") +
                    " " + messageTools.getPropertyCaption(metaProperty.getDomain(), metaProperty.getName()));
        }

        MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(metaDataDescriptor.getRelatedMetaClass());

        filterEntity.setXml(getRelatedEntitiesFilterXml(effectiveMetaClass, selectedParents, component, metaDataDescriptor));
        filterEntity.setUsername(userSessionSource.getUserSession().getUser().getUsername());

        component.setFilterEntity(filterEntity);
        component.apply(Filter.FilterOptions.create()
                .setNotifyInvalidConditions(true)
                .setLoadData(false));
    }

    protected String getRelatedEntitiesFilterXml(MetaClass relatedMetaCLass, Collection<? extends Entity> selectedEntities,
                                                 Filter component, MetaDataDescriptor descriptor) {
        ConditionsTree tree = new ConditionsTree();

        String filterComponentPath = CubaComponentsHelper.getFilterComponentPath(component);
        String[] strings = ValuePathHelper.parse(filterComponentPath);
        String filterComponentName = ValuePathHelper.pathSuffix(strings);

        MetaClass metaClass = getFilterMetaClass(component);
        String relatedPrimaryKey = metadataTools.getPrimaryKeyName(relatedMetaCLass);
        AbstractCondition condition = getOptimizedCondition(getParentIds(selectedEntities), metaClass,
                filterComponentName, relatedPrimaryKey, descriptor);

        if (condition == null) {
            condition = getNonOptimizedCondition(relatedMetaCLass, getRelatedIds(selectedEntities, descriptor), component,
                    filterComponentName, relatedPrimaryKey);
        }

        tree.setRootNodes(Collections.singletonList(new Node<>(condition)));

        return filterParser.getXml(tree, Param.ValueProperty.VALUE);
    }

    protected MetaClass getFilterMetaClass(Filter filter) {
        if (filter.getDataLoader() != null
                && filter.getDataLoader().getContainer() != null) {
            return filter.getDataLoader().getContainer().getEntityMetaClass();
        }

        if (filter.getDatasource() != null) {
            return filter.getDatasource().getMetaClass();
        }

        throw new IllegalStateException("No MetaClass related to a filter");
    }

    @Nullable
    protected AbstractCondition getOptimizedCondition(List<Object> parentIds, MetaClass metaClass,
                                                      String filterComponentName, String relatedPrimaryKey,
                                                      MetaDataDescriptor descriptor) {
        Range.Cardinality cardinality = descriptor.getMetaProperty().getRange().getCardinality();

        if (cardinality == Range.Cardinality.MANY_TO_ONE) {
            return getManyToOneCondition(parentIds, metaClass, filterComponentName, relatedPrimaryKey, descriptor);
        } else if (cardinality == Range.Cardinality.ONE_TO_MANY || cardinality == Range.Cardinality.ONE_TO_ONE) {
            return getOneToManyCondition(parentIds, metaClass, filterComponentName, descriptor);
        } else if (cardinality == Range.Cardinality.MANY_TO_MANY) {
            return getManyToManyCondition(parentIds, metaClass, filterComponentName, relatedPrimaryKey, descriptor);
        }

        return null;
    }

    @Nullable
    protected AbstractCondition getOneToManyCondition(List<Object> parentIds, MetaClass metaClass,
                                                      String filterComponentName, MetaDataDescriptor descriptor) {
        MetaProperty inverseField = descriptor.getMetaProperty().getInverse();
        if (inverseField == null) {
            return null;
        }

        MetaClass parentMetaClass = descriptor.getMetaClass();
        String parentPrimaryKey = metadataTools.getPrimaryKeyName(parentMetaClass);
        CustomCondition customCondition = getParentEntitiesCondition(parentIds, parentPrimaryKey, metaClass,
                filterComponentName, parentMetaClass);

        String whereString = String.format("{E}.%s.%s in :%s",
                inverseField.getName(), parentPrimaryKey, customCondition.getParam().getName());
        customCondition.setWhere(whereString);

        return customCondition;
    }

    @Nullable
    protected AbstractCondition getManyToManyCondition(List<Object> parentIds, MetaClass metaClass,
                                                       String filterComponentName, String relatedPrimaryKey,
                                                       MetaDataDescriptor descriptor) {
        MetaClass parentMetaClass = descriptor.getMetaClass();
        String parentPrimaryKey = metadataTools.getPrimaryKeyName(parentMetaClass);
        CustomCondition customCondition = getParentEntitiesCondition(parentIds, parentPrimaryKey, metaClass,
                filterComponentName, parentMetaClass);

        String parentEntityAlias = RandomStringUtils.randomAlphabetic(6);
        String entityAlias = RandomStringUtils.randomAlphabetic(6);
        String select = String.format("select %s.%s from %s %s ", entityAlias, relatedPrimaryKey, parentMetaClass, parentEntityAlias);

        String joinWhere = String.format("join %s.%s %s where %s.%s in :%s", parentEntityAlias, descriptor.getMetaProperty().getName(),
                entityAlias, parentEntityAlias, parentPrimaryKey, customCondition.getParam().getName());

        String whereString = String.format("{E}.%s in (%s)", relatedPrimaryKey, select + joinWhere);
        customCondition.setWhere(whereString);

        return customCondition;
    }

    @Nullable
    protected AbstractCondition getManyToOneCondition(List<Object> parentIds, MetaClass metaClass,
                                                      String filterComponentName, String relatedPrimaryKey,
                                                      MetaDataDescriptor descriptor) {
        MetaClass parentMetaClass = descriptor.getMetaClass();
        String parentPrimaryKey = metadataTools.getPrimaryKeyName(parentMetaClass);
        CustomCondition customCondition = getParentEntitiesCondition(parentIds, parentPrimaryKey, metaClass,
                filterComponentName, parentMetaClass);

        String entityAlias = RandomStringUtils.randomAlphabetic(6);
        String subQuery = String.format("select %s.%s.%s from %s %s where %s.%s in :%s", entityAlias,
                descriptor.getMetaProperty().getName(), relatedPrimaryKey, parentMetaClass.getName(), entityAlias,
                entityAlias, parentPrimaryKey, customCondition.getParam().getName());

        String whereString = String.format("{E}.%s in (%s)", relatedPrimaryKey, subQuery);
        customCondition.setWhere(whereString);

        return customCondition;
    }

    protected Param getParentEntitiesParam(List<Object> parentIds, String parentPrimaryKey, MetaClass metaClass,
                                           Class parentPrimaryKeyClass, String paramName, MetaClass parentMetaClass) {
        Param param = Param.Builder.getInstance().setName(paramName)
                .setJavaClass(parentPrimaryKeyClass)
                .setEntityWhere(StringUtils.EMPTY)
                .setEntityView(StringUtils.EMPTY)
                .setMetaClass(metaClass)
                .setProperty(parentMetaClass.getProperty(parentPrimaryKey))
                .setInExpr(true)
                .setRequired(true)
                .build();
        param.setValue(parentIds);
        return param;
    }

    protected CustomCondition getParentEntitiesCondition(List<Object> parentIds, String parentPrimaryKey,
                                                         MetaClass metaClass, String filterComponentName,
                                                         MetaClass parentMetaClass) {
        String conditionName = String.format("related_%s", RandomStringUtils.randomAlphabetic(6));
        CustomCondition condition = new CustomCondition(
                getConditionXmlElement(conditionName, parentMetaClass),
                null, filterComponentName, metaClass);


        Class<?> parentPrimaryKeyClass = parentMetaClass.getProperty(parentPrimaryKey).getJavaType();
        condition.setJavaClass(parentPrimaryKeyClass);
        condition.setHidden(true);
        condition.setInExpr(true);

        int randInt = new Random().nextInt((99999 - 11111) + 1) + 11111;
        String paramName = String.format("component$%s.%s%s", filterComponentName, conditionName, randInt);

        condition.setParam(getParentEntitiesParam(parentIds, parentPrimaryKey, metaClass,
                parentPrimaryKeyClass, paramName, parentMetaClass));

        return condition;
    }

    protected Element getConditionXmlElement(String conditionName, MetaClass metaClass) {
        Element conditionElement = DocumentHelper.createDocument().addElement("c");
        conditionElement.addAttribute("name", conditionName);
        conditionElement.addAttribute("width", "1");
        conditionElement.addAttribute("type", "CUSTOM");
        String entityName = metaClass.getName().contains("$") ?
                StringUtils.substringAfter(metaClass.getName(), "$") :
                StringUtils.substringAfter(metaClass.getName(), "_");
        String conditionCaption = String.format("%s ids", entityName);
        // condition will be hidden so we don't have to load localized condition caption
        conditionElement.addAttribute("locCaption", conditionCaption);
        return conditionElement;
    }

    protected PropertyCondition getNonOptimizedCondition(MetaClass metaClass, List<Object> ids, Filter component,
                                                         String filterComponentName, String primaryKey) {

        PropertyConditionDescriptor conditionDescriptor = new PropertyConditionDescriptor(primaryKey, primaryKey,
                null, filterComponentName, ((FilterImplementation) component).getEntityMetaClass(),
                ((FilterImplementation) component).getEntityAlias());

        PropertyCondition condition = (PropertyCondition) conditionDescriptor.createCondition();
        condition.setInExpr(true);
        condition.setHidden(true);
        condition.setOperator(Op.IN);

        Class idType = metaClass.getProperty(primaryKey).getJavaType();

        Param param = Param.Builder.getInstance().setName(paramBuilder.createParamName(condition))
                .setJavaClass(idType)
                .setEntityWhere("")
                .setEntityView("")
                .setMetaClass(((FilterImplementation) component).getEntityMetaClass())
                .setProperty(metaClass.getProperty(primaryKey))
                .setInExpr(true)
                .setRequired(true)
                .build();
        param.setValue(ids);

        condition.setParam(param);
        return condition;
    }

    protected List<Object> getRelatedIds(Collection<? extends Entity> selectedParents, MetaDataDescriptor descriptor) {
        if (selectedParents.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Object> parentIds = new ArrayList<>();
            for (Entity e : selectedParents) {
                parentIds.add(EntityValues.getId(e));
            }

            //noinspection UnnecessaryLocalVariable
            List<Object> relatedIds = relatedEntitiesService.getRelatedIds(parentIds, descriptor.getMetaClass().getName(),
                    descriptor.getMetaProperty().getName());
            return relatedIds;
        }
    }

    protected List<Object> getParentIds(Collection<? extends Entity> selectedParents) {
        if (selectedParents.isEmpty()) {
            return Collections.emptyList();
        } else {
            return selectedParents.stream().map(EntityValues::getId).collect(Collectors.toList());
        }
    }

    protected static class MetaDataDescriptor {

        protected final MetaClass metaClass;
        protected final MetaProperty metaProperty;
        protected final MetaClass relatedMetaClass;

        public MetaDataDescriptor(MetaClass metaClass, MetaProperty metaProperty) {
            this.metaClass = metaClass;
            this.metaProperty = metaProperty;

            this.relatedMetaClass = metaProperty.getRange().asClass();
        }

        protected MetaProperty getMetaProperty() {
            return metaProperty;
        }

        protected MetaClass getMetaClass() {
            return metaClass;
        }

        protected MetaClass getRelatedMetaClass() {
            return relatedMetaClass;
        }
    }
}
