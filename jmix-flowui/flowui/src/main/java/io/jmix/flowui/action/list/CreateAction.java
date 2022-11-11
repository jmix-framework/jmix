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
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.accesscontext.FlowuiEntityContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenViewReadOnly;
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.*;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.builder.DetailWindowBuilder;
import io.jmix.flowui.view.navigation.DetailViewNavigator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.jmix.flowui.component.UiComponentUtils.isComponentAttachedToDialog;


@ActionType(CreateAction.ID)
public class CreateAction<E> extends ListDataComponentAction<CreateAction<E>, E>
        implements AdjustWhenViewReadOnly, ViewOpeningAction {

    public static final String ID = "create";

    protected ViewNavigators viewNavigators;
    protected DialogWindows dialogWindows;
    protected AccessManager accessManager;

    protected ActionViewInitializer viewInitializer = new ActionViewInitializer();

    protected Supplier<E> newEntitySupplier;
    protected Consumer<E> initializer;
    protected Consumer<E> afterSaveHandler;
    protected Function<E, E> transformation;

    protected OpenMode openMode;

    public CreateAction() {
        this(ID);
    }

    public CreateAction(String id) {
        super(id);
    }

    protected void initAction() {
        super.initAction();

        this.variant = ActionVariant.PRIMARY;
        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.PLUS);
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
     * Returns the detail view id if it was set by {@link #setViewId(String)} or in the view XML,
     * otherwise returns null.
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
     * Sets the detail view class.
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
    public void setRouteParametersProvider(@Nullable RouteParametersProvider provider) {
        viewInitializer.setRouteParametersProvider(provider);
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        return viewInitializer.getQueryParametersProvider();
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        viewInitializer.setQueryParametersProvider(provider);
    }

    @Override
    public <V extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<V>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    /**
     * Sets the new entity initializer. The initializer accepts the new entity instance and can perform its
     * initialization.
     * <p>
     * Note that initializer is invoked if the detail is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the initializer is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.create", subject = "initializer")
     * protected void petsTableCreateInitializer(Pet entity) {
     *     entity.setName("a cat");
     * }
     * </pre>
     */
    public void setInitializer(@Nullable Consumer<E> initializer) {
        this.initializer = initializer;
    }

    /**
     * Sets the handler to be invoked when the detail view saves the new entity.
     * <p>
     * Note that handler is invoked if the detail is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.create", subject = "afterSaveHandler")
     * protected void petsTableCreateAfterSaveHandler(Pet entity) {
     *     System.out.println("Created " + entity);
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
     * &#64;Install(to = "petsTable.create", subject = "transformation")
     * protected Pet petsTableCreateTransformation(Pet entity) {
     *     return doTransform(entity);
     * }
     * </pre>
     */
    public void setTransformation(@Nullable Function<E, E> transformation) {
        this.transformation = transformation;
    }

    /**
     * Sets the new entity supplier. The supplier should return a new entity instance.
     * <p>
     * Note that supplier is invoked if the detail is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the supplier is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.create", subject = "newEntitySupplier")
     * protected Pet petsTableCreateNewEntitySupplier() {
     *     Pet pet = metadata.create(Pet.class);
     *     pet.setName("a cat");
     *     return pet;
     * }
     * </pre>
     */
    public void setNewEntitySupplier(@Nullable Supplier<E> newEntitySupplier) {
        this.newEntitySupplier = newEntitySupplier;
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.Create");
    }

    @Autowired
    protected void setFlowUiComponentProperties(FlowuiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getGridCreateShortcut());
    }

    @Autowired
    public void setViewNavigators(ViewNavigators viewNavigators) {
        this.viewNavigators = viewNavigators;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setDialogWindowBuilders(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null || !(target.getItems() instanceof EntityDataUnit)) {
            return false;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        FlowuiEntityContext entityContext = new FlowuiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isCreatePermitted()) {
            return false;
        }

        return super.isPermitted();
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        checkTarget();
        checkTargetItems(EntityDataUnit.class);

        if (openMode == OpenMode.DIALOG
                || isComponentAttachedToDialog((Component) target)) {
            openDialog();
        } else {
            navigate();
        }
    }

    protected void navigate() {
        DetailViewNavigator<E> navigator = viewNavigators.detailView((target))
                .newEntity()
                .withBackwardNavigation(true);

        navigator = viewInitializer.initNavigator(navigator);

        navigator.navigate();
    }

    @SuppressWarnings("unchecked")
    protected void openDialog() {
        DetailWindowBuilder<E, View<?>> builder = dialogWindows.detail(target);

        builder = viewInitializer.initWindowBuilder(builder);

        if (newEntitySupplier != null) {
            E entity = newEntitySupplier.get();
            builder = builder.newEntity(entity);
        } else {
            builder = builder.newEntity();
        }

        if (initializer != null) {
            builder = builder.withInitializer(initializer);
        }

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
    public CreateAction<E> withViewId(@Nullable String viewId) {
        setViewId(viewId);
        return this;
    }

    /**
     * @see #setViewClass(Class)
     */
    public CreateAction<E> withViewClass(@Nullable Class<? extends View> viewClass) {
        setViewClass(viewClass);
        return this;
    }

    /**
     * @see #setRouteParametersProvider(RouteParametersProvider)
     */
    public CreateAction<E> withRouteParameters(@Nullable RouteParametersProvider provider) {
        setRouteParametersProvider(provider);
        return this;
    }

    /**
     * @see #setQueryParametersProvider(QueryParametersProvider)
     */
    public CreateAction<E> withQueryParameters(@Nullable QueryParametersProvider provider) {
        setQueryParametersProvider(provider);
        return this;
    }

    /**
     * @see #setOpenMode(OpenMode)
     */
    public CreateAction<E> withOpenMode(@Nullable OpenMode openMode) {
        setOpenMode(openMode);
        return this;
    }

    /**
     * @see #setAfterCloseHandler(Consumer)
     */
    public <V extends View<?>> CreateAction<E> withAfterCloseHandler(
            @Nullable Consumer<AfterCloseEvent<V>> afterCloseHandler) {
        setAfterCloseHandler(afterCloseHandler);
        return this;
    }

    /**
     * @see #setAfterSaveHandler(Consumer)
     */
    public CreateAction<E> withAfterSaveHandler(@Nullable Consumer<E> afterSaveHandler) {
        setAfterSaveHandler(afterSaveHandler);
        return this;
    }

    /**
     * @see #setTransformation(Function)
     */
    public CreateAction<E> withTransformation(@Nullable Function<E, E> transformation) {
        setTransformation(transformation);
        return this;
    }

    /**
     * @see #withInitializer(Consumer)
     */
    public CreateAction<E> withInitializer(@Nullable Consumer<E> initializer) {
        setInitializer(initializer);
        return this;
    }

    /**
     * @see #setNewEntitySupplier(Supplier)
     */
    public CreateAction<E> withNewEntitySupplier(@Nullable Supplier<E> newEntitySupplier) {
        setNewEntitySupplier(newEntitySupplier);
        return this;
    }
}
