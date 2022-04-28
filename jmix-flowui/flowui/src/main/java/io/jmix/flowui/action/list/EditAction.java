package io.jmix.flowui.action.list;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.Messages;
import io.jmix.core.security.EntityOp;
import io.jmix.flowui.DialogWindowBuilders;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.ScreenNavigators;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenScreenReadOnly;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.ScreenOpeningAction;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.*;
import io.jmix.flowui.screen.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.screen.builder.EditorWindowBuilder;
import io.jmix.flowui.screen.navigation.EditorNavigator;
import io.jmix.flowui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

@ActionType(EditAction.ID)
public class EditAction<E> extends SecuredListDataComponentAction<EditAction<E>, E>
        implements AdjustWhenScreenReadOnly, ScreenOpeningAction, ExecutableAction {

    public static final String ID = "edit";

    protected ScreenNavigators screenNavigators;
    protected DialogWindowBuilders dialogWindowBuilders;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();
    protected Consumer<E> afterCommitHandler;
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
        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.PENCIL);
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
     * Returns the editor screen id if it was set by {@link #setScreenId(String)} or in the screen XML.
     * Otherwise, returns null.
     */
    @Nullable
    @Override
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    /**
     * Sets the editor screen id.
     */
    @Override
    public void setScreenId(@Nullable String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    /**
     * Returns the editor screen class if it was set by {@link #setScreenClass(Class)} or in the screen XML.
     * Otherwise returns null.
     */
    @Nullable
    @Override
    public Class<? extends Screen> getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    /**
     * Sets the editor screen id.
     */
    @Override
    public void setScreenClass(@Nullable Class<? extends Screen> screenClass) {
        screenInitializer.setScreenClass(screenClass);
    }

    @Nullable
    @Override
    public RouteParameters getRouteParameters() {
        return screenInitializer.getRouteParameters();
    }

    @Override
    public void setRouteParameters(@Nullable RouteParameters routeParameters) {
        screenInitializer.setRouteParameters(routeParameters);
    }

    @Nullable
    @Override
    public QueryParameters getQueryParameters() {
        return screenInitializer.getQueryParameters();
    }

    @Override
    public void setQueryParameters(@Nullable QueryParameters queryParameters) {
        screenInitializer.setQueryParameters(queryParameters);
    }

    @Override
    public <S extends Screen> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<S>> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    /**
     * Sets the handler to be invoked when the editor screen commits the entity.
     * <p>
     * Note that handler is invoked if the editor is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.edit", subject = "afterCommitHandler")
     * protected void petsTableEditAfterCommitHandler(Pet entity) {
     *     System.out.println("Committed " + entity);
     * }
     * </pre>
     */
    public void setAfterCommitHandler(@Nullable Consumer<E> afterCommitHandler) {
        this.afterCommitHandler = afterCommitHandler;
    }

    /**
     * Sets the function to transform the committed in the editor screen entity before setting it to the target data
     * container.
     * <p>
     * Note that transformation function is invoked if the editor is opened in {@link OpenMode#DIALOG} mode.
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
    protected void setFlowUiComponentProperties(FlowUiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getGridEditShortcut());
    }

    @Autowired
    public void setScreenNavigators(ScreenNavigators screenNavigators) {
        this.screenNavigators = screenNavigators;
    }

    @Autowired
    public void setDialogWindowBuilders(DialogWindowBuilders dialogWindowBuilders) {
        this.dialogWindowBuilders = dialogWindowBuilders;
    }

    @Override
    public void setText(@Nullable String text) {
        super.setText(text);
        this.textInitialized = true;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null /*|| target.getSingleSelected() == null || !(target.getItems() instanceof EntityDataUnit)*/) {
            return false;
        }

        // TODO: add security
/*        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);
        InMemoryCrudEntityContext inMemoryCrudEntityContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
        accessManager.applyRegisteredConstraints(inMemoryCrudEntityContext);

        if (!entityContext.isViewPermitted() && !entityContext.isEditPermitted()) {
            return false;
        }

        if (inMemoryCrudEntityContext.updatePredicate() != null) {
            return true;
        }*/

        return super.isPermitted();
    }

    @Override
    public void refreshState() {
        super.refreshState();

        if (target == null /*|| !(target.getItems() instanceof EntityDataUnit)*/) {
            return;
        }
        if (!textInitialized) {
            // TODO: add security
/*            MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();

            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);
            InMemoryCrudEntityContext inMemoryContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
            accessManager.applyRegisteredConstraints(inMemoryContext);

            if (metaClass != null) {
                Object entity = target.getSingleSelected();
                if (entityContext.isEditPermitted()
                        && (inMemoryContext.updatePredicate() == null
                        || entity != null && inMemoryContext.isUpdatePermitted(entity))) {
                    super.setText(messages.getMessage("actions.Edit"));
                } else {
                    super.setText(messages.getMessage("actions.View"));
                }
            }*/
        }
    }

    @Override
    public boolean isDisabledWhenScreenReadOnly() {
        // TODO: add security
/*        if (!(target.getItems() instanceof EntityDataUnit)) {
            return true;
        }

        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass != null) {
            // Even though the screen is read-only, this edit action may remain active
            // because the related entity cannot be edited and the corresponding edit screen
            // will be opened in read-only mode either.
            UiEntityContext entityContext = new UiEntityContext(metaClass);
            accessManager.applyRegisteredConstraints(entityContext);

            return entityContext.isEditPermitted();
        }*/

        return true;
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        checkTarget();

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException(String.format("%s target dataSource is null or does not implement %s",
                    getClass().getSimpleName(), EntityDataUnit.class.getSimpleName()));
        }

        E editedEntity = target.getSingleSelectedItem();
        if (editedEntity == null) {
            throw new IllegalStateException(String.format("There is not selected item in %s target",
                    getClass().getSimpleName()));
        }

        if (openMode == OpenMode.DIALOG
                || UiComponentUtils.isComponentAttachedToDialog((Component) target)) {
            openDialog(editedEntity);
        } else {
            navigateToScreen(editedEntity);
        }
    }

    protected void navigateToScreen(E editedEntity) {
        EditorNavigator<E> navigator = screenNavigators.editor((target))
                .editEntity(editedEntity);

        if (target instanceof Component) {
            Screen parent = UiComponentUtils.findScreen((Component) target);
            if (parent != null) {
                navigator = navigator.withBackNavigationTarget(parent.getClass());
            }
        }

        screenInitializer.initNavigator(navigator);

        navigator.navigate();
    }

    @SuppressWarnings("unchecked")
    protected void openDialog(E editedEntity) {
        EditorWindowBuilder<E, Screen> editorBuilder = dialogWindowBuilders.editor(target);

        editorBuilder = screenInitializer.initWindowBuilder(editorBuilder);

        editorBuilder.editEntity(editedEntity);

        if (transformation != null) {
            editorBuilder.withTransformation(transformation);
        }

        DialogWindow<Screen> dialogWindow = editorBuilder.build();
        if (afterCommitHandler != null) {
            dialogWindow.addAfterCloseListener(event -> {
                if (event.closedWith(StandardOutcome.COMMIT)
                        && event.getScreen() instanceof EditorScreen) {
                    E committedEntity = ((EditorScreen<E>) event.getScreen()).getEditedEntity();
                    afterCommitHandler.accept(committedEntity);
                }
            });
        }

        dialogWindow.open();
    }

    /**
     * @see #setScreenId(String)
     */
    public EditAction<E> withScreenId(@Nullable String screenId) {
        setScreenId(screenId);
        return this;
    }

    /**
     * @see #setScreenClass(Class)
     */
    public EditAction<E> withScreenClass(@Nullable Class<? extends Screen> screenClass) {
        setScreenClass(screenClass);
        return this;
    }

    /**
     * @see #setRouteParameters(RouteParameters)
     */
    public EditAction<E> withRouteParameters(@Nullable RouteParameters routeParameters) {
        setRouteParameters(routeParameters);
        return this;
    }

    /**
     * @see #setQueryParameters(QueryParameters)
     */
    public EditAction<E> withQueryParameters(@Nullable QueryParameters queryParameters) {
        setQueryParameters(queryParameters);
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
    public <S extends Screen> EditAction<E> withAfterCloseHandler(Consumer<AfterCloseEvent<S>> afterCloseHandler) {
        setAfterCloseHandler(afterCloseHandler);
        return this;
    }

    /**
     * @see #setAfterCommitHandler(Consumer)
     */
    public EditAction<E> withAfterCommitHandler(@Nullable Consumer<E> afterCommitHandler) {
        setAfterCommitHandler(afterCommitHandler);
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
