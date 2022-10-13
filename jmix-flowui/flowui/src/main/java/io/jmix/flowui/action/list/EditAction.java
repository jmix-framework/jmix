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

package io.jmix.flowui.action.list;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.Messages;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.EntityOp;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import io.jmix.flowui.sys.ActionViewInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

@ActionType(EditAction.ID)
public class EditAction<E> extends SecuredListDataComponentAction<EditAction<E>, E>
        implements AdjustWhenViewReadOnly, ViewOpeningAction {

    public static final String ID = "edit";

    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();
    protected Consumer<E> afterSaveHandler;
    protected Function<E, E> transformation;

    protected boolean textInitialized = false;
    protected Messages messages;

    protected OpenMode openMode;

    public EditAction() {
        this(ID);
    }

    public EditAction(String id) {
        super(id);
    }

    @Override
    protected void initAction() {
        super.initAction();

        setConstraintEntityOp(EntityOp.UPDATE);
        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.PENCIL);
    }

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        return openMode;
    }

    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        this.openMode = openMode;
    }

    /**
     * Returns the detail view id if it was set by {@link #setViewId(String)} or in the view XML.
     * Otherwise, returns null.
     */
    @Nullable
    @Override
    public String getViewId() {
        return viewInitializer.getViewId();
    }

    /**
     * Sets the detail view id.
     */
    @Override
    public void setViewId(@Nullable String viewId) {
        viewInitializer.setViewId(viewId);
    }

    /**
     * Returns the detail view class if it was set by {@link #setViewClass(Class)} or in the view XML.
     * Otherwise returns null.
     */
    @Nullable
    @Override
    public Class<? extends View> getViewClass() {
        return viewInitializer.getViewClass();
    }

    /**
     * Sets the detail view id.
     */
    @Override
    public void setViewClass(@Nullable Class<? extends View> viewClass) {
        viewInitializer.setViewClass(viewClass);
    }

    @Nullable
    @Override
    public RouteParametersProvider getRouteParametersProvider() {
        return viewInitializer.getRouteParametersProvider();
    }

    @Override
    public void setRouteParametersProvider(@Nullable RouteParametersProvider routeParameters) {
        viewInitializer.setRouteParametersProvider(routeParameters);
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        return viewInitializer.getQueryParametersProvider();
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider queryParameters) {
        viewInitializer.setQueryParametersProvider(queryParameters);
    }

    @Override
    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<V>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    /**
     * Sets the handler to be invoked when the detail view saves the entity.
     * <p>
     * Note that handler is invoked if the detail is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "afterSaveHandler")
     * protected void petsTableEditAfterSaveHandler(Pet entity) {
     *     System.out.println("Saved " + entity);
     * }
     * </pre>
     */
    public void setAfterSaveHandler(@Nullable Consumer<E> afterSaveHandler) {
        this.afterSaveHandler = afterSaveHandler;
    }

    /**
     * Sets the function to transform the saved in the detail view entity before setting it to the target data
     * container.
     * <p>
     * Note that transformation function is invoked if the detail is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the function is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "transformation")
     * protected Pet petsTableEditTransformation(Pet entity) {
     *     return doTransform(entity);
     * }
     * </pre>
     *
     * @param transformation transformation function to set
     */
    public void setTransformation(@Nullable Function<E, E> transformation) {
        this.transformation = transformation;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.text = messages.getMessage("actions.Edit");
    }

    @Autowired
    protected void setFlowUiComponentProperties(FlowuiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getGridEditShortcut());
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Autowired
    public void setDialogWindowBuilders(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Override
    public void setText(@Nullable String text) {
        super.setText(text);
        this.textInitialized = true;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null
                || target.getSingleSelectedItem() == null
                || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        InMemoryCrudEntityContext inMemoryCrudEntityContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
        accessManager.applyRegisteredConstraints(inMemoryCrudEntityContext);

        if (!entityContext.isViewPermitted() && !entityContext.isEditPermitted()) {
            return false;
        }

        if (inMemoryCrudEntityContext.updatePredicate() != null) {
            return true;
        }

        return super.isPermitted();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return;
        }
        if (!textInitialized) {
            MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();

            if (metaClass != null) {
                FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
                accessManager.applyRegisteredConstraints(entityContext);
                InMemoryCrudEntityContext inMemoryContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
                accessManager.applyRegisteredConstraints(inMemoryContext);

                Object entity = target.getSingleSelectedItem();
                if (entityContext.isEditPermitted()
                        && (inMemoryContext.updatePredicate() == null
                        || entity != null && inMemoryContext.isUpdatePermitted(entity))) {
                    super.setText(messages.getMessage("actions.Edit"));
                } else {
                    super.setText(messages.getMessage("actions.Read"));
                }
            }
        }
    }

    @Override
    public boolean isDisabledWhenViewReadOnly() {
        if (!(target.getItems() instanceof EntityDataUnit)) {
            return true;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass != null) {
            // Even though the screen is read-only, this edit action may remain active
            // because the related entity cannot be edited and the corresponding edit screen
            // will be opened in read-only mode either.
            FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            return entityContext.isEditPermitted();
        }

        return true;
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        E editedEntity = target.getSingleSelectedItem();
        if (editedEntity == null) {
            throw new IllegalStateException(String.format("There is not selected item in %s target",
                    getClass().getSimpleName()));
        }

        if (openMode == OpenMode.DIALOG
                || UiComponentUtils.isComponentAttachedToDialog((Component) target)) {
            openDialog(editedEntity);
        } else {
            navigate(editedEntity);
        }
    }

    protected void navigate(E editedEntity) {
        DetailViewNavigator<E> navigator = viewNavigators.detailView((target))
                .editEntity(editedEntity)
                .withBackwardNavigation(true);

        navigator = viewInitializer.initNavigator(navigator);

        navigator.navigate();
    }

    @SuppressWarnings("unchecked")
    protected void openDialog(E editedEntity) {
        DetailWindowBuilder<E, View<?>> builder = dialogWindows.detail(target);

        builder = viewInitializer.initWindowBuilder(builder);

        builder = builder.editEntity(editedEntity);

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        DialogWindow<View<?>> dialogWindow = builder.build();
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
    public EditAction<E> withViewId(@Nullable String viewId) {
        setViewId(viewId);
        return this;
    }

    /**
     * @see #setViewClass(Class)
     */
    public EditAction<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        setViewClass(viewClass);
        return this;
    }

    /**
     * @see #setRouteParametersProvider(RouteParametersProvider)
     */
    public EditAction<E> withRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        setRouteParametersProvider(provider);
        return this;
    }

    /**
     * @see #setQueryParametersProvider(QueryParametersProvider)
     */
    public EditAction<E> withQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        setQueryParametersProvider(provider);
        return this;
    }

    /**
     * @see #setOpenMode(OpenMode)
     */
    public EditAction<E> withOpenMode(@Nullable OpenMode openMode) {
        setOpenMode(openMode);
        return this;
    }

    /**
     * @see #setAfterCloseHandler(Consumer)
     */
    public <V extends View<?>> EditAction<E> withAfterCloseHandler(Consumer<AfterCloseEvent<V>> afterCloseHandler) {
        setAfterCloseHandler(afterCloseHandler);
        return this;
    }

    /**
     * @see #setAfterSaveHandler(Consumer)
     */
    public EditAction<E> withAfterSaveHandler(@Nullable Consumer<E> afterSaveHandler) {
        setAfterSaveHandler(afterSaveHandler);
        return this;
    }

    /**
     * @see #setTransformation(Function)
     */
    public EditAction<E> withTransformation(@Nullable Function<E, E> transformation) {
        setTransformation(transformation);
        return this;
    }
}
