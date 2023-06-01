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

package io.jmix.ui.component.impl;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import io.jmix.core.AccessManager;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.ui.Actions;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import io.jmix.ui.accesscontext.UiEntityContext;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.RelatedAction;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.RelatedEntities;
import io.jmix.ui.component.data.meta.EntityDataUnit;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.sys.PropertyOption;
import io.jmix.ui.sys.ScreensHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class RelatedEntitiesImpl<E> extends PopupButtonImpl
        implements RelatedEntities<E> {

    protected Actions actions;
    protected Messages messages;
    protected MessageTools messageTools;
    protected AccessManager accessManager;
    protected MetadataTools metadataTools;
    protected ScreensHelper screensHelper;

    protected ListComponent<E> listComponent;
    protected OpenMode openMode = OpenMode.THIS_TAB;

    protected Map<String, PropertyOption> propertyOptions = new HashMap<>();

    protected String excludeRegex;

    public RelatedEntitiesImpl() {
    }

    @Autowired
    public void setActions(Actions actions) {
        this.actions = actions;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        setCaption(messages.getMessage("actions.Related"));
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setMessageTools(MessageTools messageTools) {
        this.messageTools = messageTools;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setScreensHelper(ScreensHelper screensHelper) {
        this.screensHelper = screensHelper;
    }

    @Override
    public OpenMode getOpenMode() {
        return openMode;
    }

    @Override
    public void setOpenMode(OpenMode openMode) {
        this.openMode = openMode;

        for (Action action : getActions()) {
            if (action instanceof RelatedAction) {
                ((RelatedAction) action).setOpenMode(openMode);
            }
        }
    }

    @Nullable
    @Override
    public String getExcludePropertiesRegex() {
        return excludeRegex;
    }

    @Override
    public void setExcludePropertiesRegex(@Nullable String excludeRegex) {
        if (!Objects.equals(this.excludeRegex, excludeRegex)) {
            this.excludeRegex = excludeRegex;
            refreshNavigationActions();
        }
    }

    @Override
    public void addPropertyOption(PropertyOption propertyOption) {
        propertyOptions.put(propertyOption.getName(), propertyOption);
        refreshNavigationActions();
    }

    @Override
    public void removePropertyOption(String property) {
        if (propertyOptions.remove(property) != null) {
            refreshNavigationActions();
        }
    }

    @Nullable
    @Override
    public ListComponent<E> getListComponent() {
        return listComponent;
    }

    @Override
    public void setListComponent(@Nullable ListComponent<E> listComponent) {
        if (!Objects.equals(this.listComponent, listComponent)) {
            this.listComponent = listComponent;
            refreshNavigationActions();
        }
    }

    protected void refreshNavigationActions() {
        ComponentContainer actionContainer = (ComponentContainer) vPopupComponent;

        actionContainer.removeAllComponents();
        actionOrder.clear();

        if (listComponent != null) {
            MetaClass metaClass = getMetaClass(listComponent);

            Pattern excludePattern = null;
            if (excludeRegex != null) {
                excludePattern = Pattern.compile(excludeRegex);
            }

            for (MetaProperty metaProperty : metaClass.getProperties()) {
                if (isSuitableProperty(metaClass, metaProperty, excludePattern)) {
                    addNavigationAction(metaProperty);
                }
            }

            if (actionContainer.getComponentCount() == 0) {
                Label label = new Label(messages.getMessage("actions.Related.Empty"));
                actionContainer.addComponent(label);
            }
        }
    }

    protected boolean isSuitableProperty(MetaClass metaClass, MetaProperty metaProperty,
                                         @Nullable Pattern excludePattern) {
        if (!metaProperty.getRange().isClass()) {
            return false;
        }

        // check that entities are placed in the same data store
        MetaClass propertyMetaClass = metaProperty.getRange().asClass();
        String propertyStore = propertyMetaClass.getStore().getName();
        String effectiveStore = metaClass.getStore().getName();
        if (!Objects.equals(effectiveStore, propertyStore)) {
            return false;
        }

        // apply security
        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        UiEntityAttributeContext attributeContext =
                new UiEntityAttributeContext(metaClass, metaProperty.getName());
        accessManager.applyRegisteredConstraints(attributeContext);

        return entityContext.isViewPermitted()
                && attributeContext.canView()
                && (excludePattern == null
                || !excludePattern.matcher(metaProperty.getName()).matches());
    }

    protected MetaClass getMetaClass(ListComponent<E> listComponent) {
        if (!(listComponent.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException("ListComponent items is null or does not implement EntityDataUnit");
        }

        MetaClass metaClass = ((EntityDataUnit) listComponent.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            throw new IllegalStateException("ListComponent is not bound to entity");
        }

        return metaClass;
    }

    protected void addNavigationAction(MetaProperty metaProperty) {
        // check if browse screen available
        PropertyOption propertyOption = propertyOptions.get(metaProperty.getName());

        WindowInfo defaultScreen = screensHelper.getDefaultBrowseScreen(metaProperty.getRange().asClass());
        if (defaultScreen != null
                || (propertyOption != null && StringUtils.isNotEmpty(propertyOption.getScreenId()))) {

            Action relatedAction = createRelatedAction(metaProperty, defaultScreen, propertyOption);

            addAction(relatedAction);
        }
    }

    protected Action createRelatedAction(MetaProperty metaProperty,
                                         @Nullable WindowInfo defaultScreen,
                                         @Nullable PropertyOption propertyOption) {
        RelatedAction relatedAction = actions.create(RelatedAction.ID, "related" + actionOrder.size());

        relatedAction.setTarget(listComponent);
        relatedAction.setMetaProperty(metaProperty);
        relatedAction.setOpenMode(openMode);
        relatedAction.setCaption(messageTools.getPropertyCaption(metaProperty));

        if (defaultScreen != null) {
            relatedAction.setScreenId(defaultScreen.getId());
        }

        if (propertyOption != null) {
            if (StringUtils.isNotEmpty(propertyOption.getCaption())) {
                relatedAction.setCaption(propertyOption.getCaption());
            }

            if (StringUtils.isNotEmpty(propertyOption.getConfigurationName())) {
                relatedAction.setConfigurationName(propertyOption.getConfigurationName());
            }

            if (StringUtils.isNotEmpty(propertyOption.getScreenId())) {
                relatedAction.setScreenId(propertyOption.getScreenId());
            }
        }

        return relatedAction;
    }
}
