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

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.EntityMultiPickerComponent;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.LookupView;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An action that sets an entity to the entity picker using the entity lookup view.
 *
 * @param <E> entity type
 */
@ActionType(EntityLookupAction.ID)
public class EntityLookupAction<E> extends PickerAction<EntityLookupAction<E>, EntityPickerComponent<E>, E>
        implements ViewOpeningAction {

    private static final Logger log = LoggerFactory.getLogger(EntityLookupAction.class);

    public static final String ID = "entity_lookup";

    protected DialogWindows dialogWindows;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();

    protected Predicate<LookupView.ValidationContext<E>> selectValidator;
    protected Function<Collection<E>, Collection<E>> transformation;

    public EntityLookupAction() {
        this(ID);
    }

    public EntityLookupAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        this.icon = ComponentUtils.convertToIcon(VaadinIcon.ELLIPSIS_DOTS_H);
    }

    @Autowired
    public void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.entityPicker.lookup.description");
    }

    @Autowired
    protected void setUiComponentProperties(UiComponentProperties uiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(uiComponentProperties.getPickerLookupShortcut());
    }

    /**
     * Sets a validator for validating the selection in the lookup view.
     *
     * @param selectValidator a predicate that determines if the selection is valid.
     *                        It takes a {@link LookupView.ValidationContext} as input and returns a boolean
     */
    public void setSelectValidator(Predicate<LookupView.ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    /**
     * Sets the transformation function that processes a collection of entities.
     *
     * @param transformation a function that takes a collection of entities as input
     *                       and returns a transformed collection of entities
     */
    public void setTransformation(Function<Collection<E>, Collection<E>> transformation) {
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
    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<V>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Override
    public <V extends View<?>> Consumer<AfterCloseEvent<V>> getAfterCloseHandler() {
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

    @Override
    public void execute() {
        MetaClass metaClass = target.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the " + target.getClass().getSimpleName(), "action ID", getId());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        LookupWindowBuilder<E, View<?>> builder = target instanceof EntityMultiPickerComponent
                ? dialogWindows.lookup((EntityMultiPickerComponent) target)
                : dialogWindows.lookup(target);

        builder = viewInitializer.initWindowBuilder(builder);

        if (selectValidator != null) {
            builder = builder.withSelectValidator(selectValidator);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        builder.open();
    }

    /**
     * @see #setViewId(String)
     */
    public EntityLookupAction<E> withViewId(@Nullable String viewId) {
        setViewId(viewId);
        return this;
    }

    /**
     * @see #setViewClass(Class)
     */
    public EntityLookupAction<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        setViewClass(viewClass);
        return this;
    }

    /**
     * @see #setSelectValidator(Predicate)
     */
    public EntityLookupAction<E> withSelectValidator(Predicate<LookupView.ValidationContext<E>> selectValidator) {
        setSelectValidator(selectValidator);
        return this;
    }

    /**
     * @see #setTransformation(Function)
     */
    public EntityLookupAction<E> withTransformation(Function<Collection<E>, Collection<E>> transformation) {
        setTransformation(transformation);
        return this;
    }

    /**
     * @see #setViewConfigurer(Consumer)
     */
    public <V extends View<?>> EntityLookupAction<E> withViewConfigurer(@Nullable Consumer<V> viewConfigurer) {
        setViewConfigurer(viewConfigurer);
        return this;
    }
}
