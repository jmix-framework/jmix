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

package io.jmix.ui.relatedentities;

import com.google.common.base.Strings;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.JpqlFilter;
import io.jmix.ui.component.filter.FilterUtils;
import io.jmix.ui.component.filter.configuration.DesignTimeConfiguration;
import io.jmix.ui.component.jpqlfilter.JpqlFilterSupport;
import io.jmix.ui.component.propertyfilter.SingleFilterSupport;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings({"unchecked", "rawtypes"})
@Internal
@Component("ui_RelatedEntities")
public class RelatedEntitiesSupportImpl implements RelatedEntitiesSupport {

    protected Messages messages;
    protected MessageTools messageTools;
    protected Metadata metadata;
    protected WindowConfig windowConfig;
    protected ExtendedEntities extendedEntities;
    protected UiComponents uiComponents;
    protected JpqlFilterSupport jpqlFilterSupport;
    protected SingleFilterSupport singleFilterSupport;
    protected MetadataTools metadataTools;

    public RelatedEntitiesSupportImpl(Metadata metadata,
                                      MessageTools messageTools,
                                      WindowConfig windowConfig,
                                      Messages messages,
                                      ExtendedEntities extendedEntities,
                                      UiComponents uiComponents,
                                      JpqlFilterSupport jpqlFilterSupport,
                                      SingleFilterSupport singleFilterSupport,
                                      MetadataTools metadataTools) {
        this.messages = messages;
        this.messageTools = messageTools;
        this.metadata = metadata;
        this.windowConfig = windowConfig;
        this.extendedEntities = extendedEntities;
        this.uiComponents = uiComponents;
        this.jpqlFilterSupport = jpqlFilterSupport;
        this.singleFilterSupport = singleFilterSupport;
        this.metadataTools = metadataTools;
    }

    @Override
    public RelatedEntitiesBuilder builder(FrameOwner frameOwner) {
        return new RelatedEntitiesBuilder(frameOwner, this::buildScreen);
    }

    protected Screen buildScreen(RelatedEntitiesBuilder builder) {
        MetaClass metaClass = getMetaClass(builder);
        MetaProperty metaProperty = getMetaProperty(builder, metaClass);

        Screen screen = createScreen(builder, metaClass, metaProperty);

        boolean found = ComponentsHelper.walkComponents(screen.getWindow(), screenComponent -> {
            if (screenComponent instanceof Filter) {
                Filter filter = (Filter) screenComponent;
                DataLoader dataLoader = filter.getDataLoader();
                MetaClass actualMetaClass = dataLoader.getContainer().getEntityMetaClass();
                MetaClass relatedMetaClass = metaProperty.getRange().asClass();
                MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(relatedMetaClass);

                if (Objects.equals(actualMetaClass, effectiveMetaClass)) {
                    Collection selectedEntities = builder.getSelectedEntities() == null
                            ? Collections.emptyList()
                            : builder.getSelectedEntities();

                    String configurationName = generateConfigurationName(builder, metaProperty);
                    DesignTimeConfiguration configuration = createFilterConfiguration((Filter) screenComponent,
                            configurationName);

                    JpqlFilter jpqlFilter = createJpqlFilter(filter.getDataLoader(), metaProperty, metaClass,
                            selectedEntities);
                    configuration.getRootLogicalFilterComponent().add(jpqlFilter);
                    configuration.setFilterComponentDefaultValue(jpqlFilter.getParameterName(), jpqlFilter.getValue());

                    filter.setCurrentConfiguration(configuration);

                    return true;
                }
            }
            return false;
        });

        if (!found) {
            screen.addAfterShowListener(event -> {
                ScreenContext screenContext = UiControllerUtils.getScreenContext(event.getSource());
                screenContext.getNotifications()
                        .create(Notifications.NotificationType.WARNING)
                        .withCaption(messages.getMessage("actions.Related.FilterNotFound"))
                        .show();
            });
        }

        return screen;
    }

    protected MetaClass getMetaClass(RelatedEntitiesBuilder builder) {
        MetaClass metaClass = builder.getMetaClass();
        if (metaClass != null) {
            return metaClass;
        }

        Class<?> entityClass = builder.getEntityClass();
        if (entityClass != null) {
            return metadata.getClass(entityClass);
        }

        throw new IllegalStateException("'metaClass' or 'entityClass' can't be null");
    }

    protected MetaProperty getMetaProperty(RelatedEntitiesBuilder builder, MetaClass metaClass) {
        MetaProperty metaProperty = builder.getMetaProperty();
        if (metaProperty != null) {
            return metaProperty;
        }

        String property = builder.getProperty();
        if (!Strings.isNullOrEmpty(property)) {
            return metaClass.getProperty(property);
        }

        throw new IllegalStateException("'metaProperty' or 'property' can't be null");
    }

    protected Screen createScreen(RelatedEntitiesBuilder builder, MetaClass metaClass, MetaProperty metaProperty) {
        FrameOwner origin = builder.getOrigin();
        Screens screens = UiControllerUtils.getScreenContext(origin).getScreens();

        if (builder instanceof RelatedEntitiesClassBuilder) {
            return screens.create(((RelatedEntitiesClassBuilder<?>) builder).getScreenClass(),
                    builder.getOpenMode(), builder.getOptions());
        } else {
            String screenId = builder.getScreenId();
            if (Strings.isNullOrEmpty(screenId)) {
                // try to get default browse screen id
                screenId = windowConfig.getBrowseScreenId(metaProperty.getRange().asClass());
                if (Strings.isNullOrEmpty(screenId)) {
                    String message = String.format("Can't create related entities screen: passed screen id is null and " +
                            "there is no default browse screen for %s", metaClass.getName());
                    throw new IllegalStateException(message);
                }
            }

            return screens.create(screenId, builder.getOpenMode(), builder.getOptions());
        }
    }

    protected DesignTimeConfiguration createFilterConfiguration(Filter filter, String configurationName) {
        String configurationId = FilterUtils.generateConfigurationId(configurationName);
        return filter.addConfiguration(configurationId, configurationName);
    }

    protected String generateConfigurationName(RelatedEntitiesBuilder builder, MetaProperty metaProperty) {
        String configurationName = builder.getConfigurationName();
        if (StringUtils.isEmpty(configurationName)) {
            configurationName = messages.getMessage("actions.Related.Filter") + " "
                    + messageTools.getPropertyCaption(metaProperty.getDomain(), metaProperty.getName());
        }

        return configurationName;
    }

    protected JpqlFilter createJpqlFilter(DataLoader dataLoader,
                                          MetaProperty metaProperty,
                                          MetaClass parentMetaClass,
                                          Collection selectedParentEntities) {
        JpqlFilter jpqlFilter = uiComponents.create(JpqlFilter.NAME);
        jpqlFilter.setDataLoader(dataLoader);
        jpqlFilter.setHasInExpression(true);
        jpqlFilter.setVisible(false);

        Class parameterClass = parentMetaClass.getJavaClass();
        jpqlFilter.setParameterClass(parameterClass);
        jpqlFilter.setParameterName(jpqlFilterSupport.generateParameterName(null, parameterClass.getSimpleName()));

        String where = getWhereExpression(metaProperty, parentMetaClass);
        jpqlFilter.setCondition(where, null);

        HasValue valueComponent = singleFilterSupport.generateValueComponent(metaProperty.getDomain(),
                true, parameterClass);
        jpqlFilter.setValueComponent(valueComponent);
        jpqlFilter.setValue(selectedParentEntities);

        return jpqlFilter;
    }

    protected String getWhereExpression(MetaProperty metaProperty,
                                        MetaClass parentMetaClass) {
        Range.Cardinality cardinality = metaProperty.getRange().getCardinality();

        switch (cardinality) {
            case MANY_TO_ONE:
                return getManyToOneJpqlCondition(metaProperty, parentMetaClass);
            case ONE_TO_ONE:
            case ONE_TO_MANY:
                return getOneToManyJpqlCondition(metaProperty, parentMetaClass);
            case MANY_TO_MANY:
                return getManyToManyJpqlCondition(metaProperty, parentMetaClass);
            default:
                throw new IllegalArgumentException("Unsupported cardinality: " + cardinality);
        }
    }

    protected String getManyToOneJpqlCondition(MetaProperty metaProperty,
                                               MetaClass parentMetaClass) {
        String entityAlias = RandomStringUtils.randomAlphabetic(6);
        String inExpression = String.format("select %s.%s.%s from %s %s where %s.%s in ?",
                entityAlias, metaProperty.getName(), metadataTools.getPrimaryKeyName(metaProperty.getDomain()),
                parentMetaClass.getName(), entityAlias,
                entityAlias, metadataTools.getPrimaryKeyName(parentMetaClass));
        return String.format("{E}.%s in (%s)", metadataTools.getPrimaryKeyName(metaProperty.getDomain()), inExpression);
    }

    protected String getOneToManyJpqlCondition(MetaProperty metaProperty,
                                               MetaClass parentMetaClass) {
        MetaProperty inverseProperty = metaProperty.getInverse();
        if (inverseProperty == null) {
            throw new IllegalStateException(String.format("Unable to find inverse property for property %s",
                    metaProperty.getName()));
        }

        return String.format("{E}.%s.%s in ?", inverseProperty.getName(),
                metadataTools.getPrimaryKeyName(parentMetaClass));
    }

    protected String getManyToManyJpqlCondition(MetaProperty metaProperty,
                                                MetaClass parentMetaClass) {
        String parentEntityAlias = RandomStringUtils.randomAlphabetic(6);
        String entityAlias = RandomStringUtils.randomAlphabetic(6);

        String select = String.format("select %s.%s from %s %s ", entityAlias,
                metadataTools.getPrimaryKeyName(metaProperty.getDomain()), parentMetaClass.getName(), parentEntityAlias);

        String joinWhere = String.format("join %s.%s %s where %s.%s in ?", parentEntityAlias, metaProperty.getName(),
                entityAlias, parentEntityAlias, metadataTools.getPrimaryKeyName(parentMetaClass));

        return String.format("{E}.%s in (%s)", metadataTools.getPrimaryKeyName(metaProperty.getDomain()),
                select + joinWhere);
    }
}
