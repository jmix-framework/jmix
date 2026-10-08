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

package io.jmix.flowui.action.entitypicker;

import com.vaadin.flow.component.HasValue;
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
import io.jmix.flowui.icon.Icons;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.icon.JmixFontIcon;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.DialogWindow;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.ReadWindowBuilder;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;

/**
 * Shows the entity set to an entity picker component in a read view.
 * <p>
 * The view is resolved as {@link io.jmix.flowui.action.list.ReadAction} resolves it: the view annotated with
 * {@link io.jmix.flowui.view.PrimaryReadView}, otherwise the view with the {@code <entity>.read} id, otherwise
 * the entity's detail view opened in the read-only mode. In contrast to {@link EntityOpenAction} the action
 * never saves: it requires no UPDATE permission and stays enabled on a read-only view.
 *
 * @param <E> entity type
 */
@ActionType(EntityReadAction.ID)
public class EntityReadAction<E> extends PickerAction<EntityReadAction<E>, EntityPickerComponent<E>, E>
        implements ViewOpeningAction {

    private static final Logger log = LoggerFactory.getLogger(EntityReadAction.class);

    public static final String ID = "entity_read";

    protected Messages messages;
    protected Notifications notifications;
    protected DialogWindows dialogWindows;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();

    public EntityReadAction() {
        this(ID);
    }

    public EntityReadAction(String id) {
        super(id);
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
        this.text = messages.getMessage("actions.entityPicker.read.description");
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        setShortcutCombination(KeyCombination.create(uiComponentProperties.getPickerReadShortcut()));
    }

    @Autowired
    protected void setIcons(Icons icons) {
        // Check for 'null' for backward compatibility because 'icon' can be set in
        // the 'initAction()' method which is called before injection.
        if (this.icon == null) {
            this.icon = icons.get(JmixFontIcon.READ_ACTION);
        }
    }

    @Override
    public void setTarget(@Nullable EntityPickerComponent<E> target) {
        checkState(target == null || target instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        super.setTarget(target);
    }

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        // Read view opens in a dialog window only
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
        // Read view opens in a dialog window only
        return null;
    }

    @Override
    public void setRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        log.warn("{} doesn't support setting {}", ID, RouteParametersProvider.class.getSimpleName());
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        // Read view opens in a dialog window only
        return null;
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        log.warn("{} doesn't support setting {}", ID, QueryParametersProvider.class.getSimpleName());
    }

    @Override
    public <V extends View<?>> void setAfterCloseHandler(
            @Nullable Consumer<DialogWindow.AfterCloseEvent<V>> afterCloseHandler) {
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
            notifications.show(messages.getMessage("actions.entityPicker.read.isDeleted"));
            return;
        }

        MetaClass metaClass = target.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the " + target.getClass().getSimpleName(), "action ID", getId());
        }

        ReadWindowBuilder<E, View<?>> builder = dialogWindows.read(target);

        builder = viewInitializer.initWindowBuilder(builder);

        builder.build().open();
    }

    /**
     * Sets the id of the view to open and returns the action for chaining.
     *
     * @param viewId id of the view to open
     * @return this instance for chaining
     */
    public EntityReadAction<E> withViewId(@Nullable String viewId) {
        setViewId(viewId);
        return this;
    }

    /**
     * Sets the class of the view to open and returns the action for chaining.
     *
     * @param viewClass class of the view to open
     * @return this instance for chaining
     */
    public EntityReadAction<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        setViewClass(viewClass);
        return this;
    }

    /**
     * Sets the handler that configures the view before it is shown and returns the action for chaining.
     *
     * @param viewConfigurer handler to set
     * @param <V>            view type
     * @return this instance for chaining
     */
    public <V extends View<?>> EntityReadAction<E> withViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        setViewConfigurer(viewConfigurer);
        return this;
    }

    @SuppressWarnings("unchecked")
    protected boolean isEmpty() {
        return ((HasValue<?, E>) target).isEmpty();
    }
}
