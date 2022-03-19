/*
 * Copyright 2019 Haulmont.
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

import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.EntityLinkField;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.component.data.meta.EntityValueSource;
import io.jmix.ui.component.data.value.ContainerValueSource;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import io.jmix.ui.widget.JmixButtonField;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EntityLinkFieldImpl<V> extends AbstractField<JmixButtonField<V>, V, V>
        implements EntityLinkField<V>, InitializingBean {

    protected static final String EMPTY_VALUE_STYLENAME = "empty-value";

    protected Consumer<EntityLinkField> clickHandler;

    protected String screen;
    protected OpenMode screenOpenMode = OpenMode.THIS_TAB;
    protected Map<String, Object> screenParams;

    protected MetaClass metaClass;
    protected ListComponent owner;

    protected Subscription closeListenerSubscription;

    /* Beans */
    protected Metadata metadata;
    protected MetadataTools metadataTools;
    protected ScreenBuilders screenBuilders;
    protected DatatypeRegistry datatypeRegistry;
    protected Messages messages;
    protected WindowConfig windowConfig;

    public EntityLinkFieldImpl() {
        component = createComponent();
        attachValueChangeListener(component);
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setScreenBuilders(ScreenBuilders screenBuilders) {
        this.screenBuilders = screenBuilders;
    }

    @Autowired
    public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry) {
        this.datatypeRegistry = datatypeRegistry;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setWindowConfig(WindowConfig windowConfig) {
        this.windowConfig = windowConfig;
    }

    protected JmixButtonField<V> createComponent() {
        return new JmixButtonField<>();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent();
    }

    protected void initComponent() {
        component.addClickListener(event -> {
            if (clickHandler != null) {
                clickHandler.accept(EntityLinkFieldImpl.this);
            } else {
                openEntityEditor();
            }
        });
        component.setCaptionFormatter((value, locale) -> {
            if (value == null) {
                return "";
            }

            if (value instanceof Entity) {
                return metadataTools.getInstanceName(value);
            }

            Datatype datatype = datatypeRegistry.get(value.getClass());

            if (locale != null) {
                return datatype.format(value, locale);
            }

            return datatype.format(value);
        });
    }

    @Nullable
    @Override
    public MetaClass getMetaClass() {
        MetaProperty metaProperty = getMetaPropertyForEditedValue();
        if (metaProperty != null && metaProperty.getRange().isClass()) {
            return metaProperty.getRange().asClass();
        }
        return metaClass;
    }

    @Override
    public void setMetaClass(@Nullable MetaClass metaClass) {
        ValueSource<V> valueSource = getValueSource();

        if (valueSource instanceof EntityValueSource) {
            throw new IllegalStateException("ValueSource is not null");
        }
        this.metaClass = metaClass;
    }

    @Nullable
    @Override
    public ListComponent getOwner() {
        return owner;
    }

    @Override
    public void setOwner(ListComponent owner) {
        this.owner = owner;
    }

    @Override
    public void setValue(@Nullable V value) {
        super.setValue(value);

        if (value != null) {
            if (getValueSource() == null && metaClass == null) {
                throw new IllegalStateException("ValueSource or metaclass must be set for field");
            }

            component.removeStyleName(EMPTY_VALUE_STYLENAME);

            MetaClass fieldMetaClass = getMetaClass();
            if (fieldMetaClass != null) {
                Class fieldClass = fieldMetaClass.getJavaClass();
                Class<?> valueClass = value.getClass();
                //noinspection unchecked
                if (!fieldClass.isAssignableFrom(valueClass)) {
                    throw new IllegalArgumentException(
                            String.format("Could not set value with class %s to field with class %s",
                                    fieldClass.getCanonicalName(),
                                    valueClass.getCanonicalName())
                    );
                }
            }
        } else {
            component.addStyleName("empty-value");
        }
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(EMPTY_VALUE_STYLENAME, ""));
    }

    @Override
    public String getScreen() {
        return screen;
    }

    @Override
    public void setScreen(String screen) {
        this.screen = screen;
    }

    @Nullable
    @Override
    public Consumer<EntityLinkField> getCustomClickHandler() {
        return clickHandler;
    }

    @Override
    public void setCustomClickHandler(@Nullable Consumer<EntityLinkField> clickHandler) {
        this.clickHandler = clickHandler;
    }

    @Override
    public OpenMode getOpenMode() {
        return screenOpenMode;
    }

    @Override
    public void setOpenMode(OpenMode openMode) {
        this.screenOpenMode = openMode;
    }

    @Nullable
    @Override
    public Map<String, Object> getScreenParams() {
        return screenParams;
    }

    @Override
    public void setScreenParams(@Nullable Map<String, Object> screenParams) {
        this.screenParams = screenParams;
    }

    @Override
    public Subscription addEditorCloseListener(Consumer<EditorCloseEvent> editorCloseListener) {
        return getEventHub().subscribe(EditorCloseEvent.class, editorCloseListener);
    }

    protected void openEntityEditor() {
        V value = getValue();

        Object entity = null;
        if (value instanceof Entity) {
            entity = value;
        } else if (getValueSource() instanceof EntityValueSource) {
            entity = ((EntityValueSource) getValueSource()).getItem();
        }

        if (entity == null) {
            return;
        }

        Window window = ComponentsHelper.getWindow(this);
        if (window == null) {
            throw new IllegalStateException("Please specify Frame for EntityLinkField");
        }

        ScreenContext context = ComponentsHelper.getScreenContext(this);

        if (EntityValues.isSoftDeleted(entity)) {
            context.getNotifications().create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage("OpenAction.objectIsDeleted"))
                    .show();
            return;
        }

        DataManager dataManager = applicationContext.getBean(DataManager.class);
        entity = dataManager.load(Id.of(entity))
                .fetchPlan(applicationContext.getBean(FetchPlanRepository.class).getFetchPlan(entity.getClass(), FetchPlan.INSTANCE_NAME))
                .one();

        String windowAlias = screen;

        MetaClass metaClass = metadata.getClass(entity);
        if (windowAlias == null) {
            windowAlias = windowConfig.getEditorScreenId(metaClass);
        }

        Screen screenEditor = screenBuilders.editor(metaClass.getJavaClass(), window.getFrameOwner())
                .withScreenId(windowAlias)
                .editEntity(entity)
                .withOpenMode(screenOpenMode)
                .withOptions(new MapScreenOptions(screenParams != null ? screenParams : new HashMap<>()))
                .build();

        screenEditor.addAfterCloseListener(event -> {
            // move focus to component
            component.focus();

            String closeActionId = null;
            CloseAction closeAction = event.getCloseAction();
            if (closeAction instanceof StandardCloseAction) {
                closeActionId = ((StandardCloseAction) closeAction).getActionId();
            }

            Screen screenSource = null;
            if (StringUtils.isNotEmpty(closeActionId)
                    && Window.COMMIT_ACTION_ID.equals(closeActionId)) {
                Object item = null;
                screenSource = event.getSource();
                if (screenSource instanceof EditorScreen) {
                    item = ((EditorScreen) screenSource).getEditedEntity();
                }

                if (item != null) {
                    afterCommitOpenedEntity(item);
                }
            }

            fireEditorCloseEvent(screenSource == null ?
                    null : (EditorScreen) screenSource, closeActionId);
        });
        screenEditor.show();
    }

    protected void fireEditorCloseEvent(@Nullable EditorScreen editorScreen, String closeActionId) {
        publish(EditorCloseEvent.class,
                new EditorCloseEvent<>(this, editorScreen, closeActionId));
    }

    protected void afterCommitOpenedEntity(Object item) {
        MetaProperty metaProperty = getMetaPropertyForEditedValue();
        if (metaProperty != null && metaProperty.getRange().isClass()) {
            if (getValueSource() != null) {
                CollectionContainer ownerCollectionCont = null;

                if (getCollectionContainerFromOwner() != null) {
                    ownerCollectionCont = ((ContainerDataUnit) owner.getItems()).getContainer();
                    ownerCollectionCont.mute();
                }

                //noinspection unchecked
                setValueSilently((V) item);

                if (ownerCollectionCont != null) {
                    ownerCollectionCont.unmute();
                }
            } else {
                //noinspection unchecked
                setValue((V) item);
            }
            // if we edit property with non Entity type and set ListComponent owner
        } else if (owner != null) {
            if (getCollectionContainerFromOwner() != null) {
                //do not listen changes in collection
                getCollectionContainerFromOwner().mute();

                //noinspection unchecked
                getCollectionContainerFromOwner().replaceItem(item);
                MetaPropertyPath metaPropertyPath = ((ContainerValueSource) getValueSource()).getMetaPropertyPath();
                setValueSilently(EntityValues.getValueEx(item, metaPropertyPath));

                //listen changes
                getCollectionContainerFromOwner().unmute();
            }

            if (owner instanceof Focusable) {
                // focus owner
                ((Focusable) owner).focus();
            }
            // if we edit property with non Entity type
        } else {
            //noinspection unchecked
            setValueSilently((V) item);
        }
    }

    @Nullable
    protected CollectionContainer getCollectionContainerFromOwner() {
        if (owner != null && owner.getItems() != null) {
            if (owner.getItems() instanceof ContainerDataUnit) {
                return ((ContainerDataUnit) owner.getItems()).getContainer();
            }
        }
        return null;
    }

    @Nullable
    protected MetaProperty getMetaPropertyForEditedValue() {
        ValueSource<V> valueSource = getValueSource();
        if (valueSource instanceof EntityValueSource) {
            MetaPropertyPath metaPropertyPath = ((EntityValueSource) valueSource).getMetaPropertyPath();
            return metaPropertyPath.getMetaProperty();
        }
        return null;
    }

    /**
     * Sets value to the component without triggering change listeners for ContainerValueSource.
     *
     * @param item value
     */
    protected void setValueSilently(@Nullable V item) {
        ((ContainerValueSource) getValueSource()).getContainer().mute();
        setValue(item);
        ((ContainerValueSource) getValueSource()).getContainer().unmute();
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }
}
