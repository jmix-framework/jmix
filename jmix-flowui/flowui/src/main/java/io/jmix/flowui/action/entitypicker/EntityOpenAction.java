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

package io.jmix.flowui.action.entitypicker;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

/**
 * Represents an action that allows opening a {@link DetailView} for the entity associated with
 * an {@link EntityPickerComponent}.
 *
 * @param <E> the type of entity being handled by this action
 */
@ActionType(EntityOpenAction.ID)
public class EntityOpenAction<E> extends PickerAction<EntityOpenAction<E>, EntityPickerComponent<E>, E>
        implements ViewOpeningAction {

    private static final Logger log = LoggerFactory.getLogger(EntityOpenAction.class);

    public static final String ID = "entity_open";

    protected Messages messages;
    protected Notifications notifications;
    protected DialogWindows dialogWindows;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();

    protected Consumer<E> afterSaveHandler;
    protected Function<E, E> transformation;

    public EntityOpenAction() {
        this(ID);
    }

    public EntityOpenAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.SEARCH);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage("actions.entityPicker.open.description");
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(uiComponentProperties.getPickerOpenShortcut());
    }

    @Override
    public void setTarget(@Nullable EntityPickerComponent<E> target) {
        checkState(target == null || target instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        super.setTarget(target);
    }

    /**
     * Sets a handler that will be executed after the entity is saved.
     *
     * @param afterSaveHandler a {@link Consumer} that defines the action to be performed
     *                         with the entity after it is saved
     */
    public void setAfterSaveHandler(Consumer<E> afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    /**
     * Sets a transformation function to be applied to the entity.
     *
     * @param transformation a {@link Function} that takes an entity as input and returns the transformed entity.
     */
    public void setTransformation(Function<E, E> transformation) {
        this.transformation = transformation;
    }

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        // Lookup view opens in a dialog window only
        return OpenMode.DIALOG;
    }

    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        log.warn("{} doesn't support setting {}", ID, OpenMode.class.getSimpleName());
    }

    @Nullable
    @Override
    public String getViewId() {
        return viewInitializer.getViewId();
    }

    @Override
    public void setViewId(@Nullable String viewId) {
        viewInitializer.setViewId(viewId);
    }

    @Nullable
    @Override
    public Class<? extends View> getViewClass() {
        return viewInitializer.getViewClass();
    }

    @Override
    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        viewInitializer.setViewClass(viewClass);
    }

    @Nullable
    @Override
    public RouteParametersProvider getRouteParametersProvider() {
        // Lookup view opens in a dialog window only
        return null;
    }

    @Override
    public void setRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        log.warn("{} doesn't support setting {}", ID, RouteParametersProvider.class.getSimpleName());
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        // Lookup view opens in a dialog window only
        return null;
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        log.warn("{} doesn't support setting {}", ID, QueryParametersProvider.class.getSimpleName());
    }

    @Override
    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<DialogWindow.AfterCloseEvent<V>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Override
    public <V extends View<?>> Consumer<DialogWindow.AfterCloseEvent<V>> getAfterCloseHandler() {
        return viewInitializer.getAfterCloseHandler();
    }

    @Override
    public <V extends View<?>> void setViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        viewInitializer.setViewConfigurer(viewConfigurer);
    }

    @Override
    public <V extends View<?>> Consumer<V> getViewConfigurer() {
        return viewInitializer.getViewConfigurer();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        if (isEmpty()) {
            return;
        }

        E entity = ((HasValue<?, E>) target).getValue();

        if (entity != null && EntityValues.isSoftDeleted(entity)) {
            notifications.show(messages.getMessage("actions.entityPicker.open.isDeleted"));
            return;
        }

        MetaClass metaClass = target.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the " + target.getClass().getSimpleName(), "action ID", getId());
        }

        DetailWindowBuilder<E, View<?>> builder = dialogWindows.detail(target);

        builder = viewInitializer.initWindowBuilder(builder);

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        DialogWindow<?> dialogWindow = builder.build();
        if (afterSaveHandler != null) {
            dialogWindow.addAfterCloseListener(event -> {
                if (event.closedWith(StandardOutcome.SAVE)
                        && event.getView() instanceof DetailView) {
                    E savedEntity = ((DetailView<E>) event.getView()).getEditedEntity();
                    afterSaveHandler.accept(savedEntity);
                }
            });
        }

        dialogWindow.open();
    }

    /**
     * @see #setViewId(String)
     */
    public EntityOpenAction<E> withViewId(@Nullable String viewId) {
        setViewId(viewId);
        return this;
    }

    /**
     * @see #setViewClass(Class)
     */
    public EntityOpenAction<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        setViewClass(viewClass);
        return this;
    }

    /**
     * @see #setAfterSaveHandler(Consumer)
     */
    public EntityOpenAction<E> withAfterSaveHandler(Consumer<E> afterSaveHandler) {
        setAfterSaveHandler(afterSaveHandler);
        return this;
    }

    /**
     * @see #setTransformation(Function)
     */
    public EntityOpenAction<E> withTransformation(Function<E, E> transformation) {
        setTransformation(transformation);
        return this;
    }

    /**
     * @see #setViewConfigurer(Consumer)
     */
    public <V extends View<?>> EntityOpenAction<E> withViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        setViewConfigurer(viewConfigurer);
        return this;
    }

    @SuppressWarnings("unchecked")
    protected boolean isEmpty() {
        return ((HasValue<?, E>) target).isEmpty();
    }
}
