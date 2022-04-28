package io.jmix.flowui.action.list;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.flowui.DialogWindowBuilders;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.ScreenNavigators;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.AdjustWhenScreenReadOnly;
import io.jmix.flowui.action.ExecutableAction;
import io.jmix.flowui.action.ScreenOpeningAction;
import io.jmix.flowui.data.EntityDataUnit;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.kit.action.ActionVariant;
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
import java.util.function.Supplier;

import static io.jmix.flowui.component.UiComponentUtils.isComponentAttachedToDialog;


@ActionType(CreateAction.ID)
public class CreateAction<E> extends ListDataComponentAction<CreateAction<E>, E>
        implements AdjustWhenScreenReadOnly, ScreenOpeningAction, ExecutableAction {

    public static final String ID = "create";

    protected ScreenNavigators screenNavigators;
    protected DialogWindowBuilders dialogWindowBuilders;
    protected AccessManager accessManager;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Supplier<E> newEntitySupplier;
    protected Consumer<E> initializer;
    protected Consumer<E> afterCommitHandler;
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
        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.PLUS);
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
     * Otherwise returns null.
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
     * Sets the editor screen class.
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
     * Sets the new entity initializer. The initializer accepts the new entity instance and can perform its
     * initialization.
     * <p>
     * Note that initializer is invoked if the editor is opened in {@link OpenMode#DIALOG} mode.
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
     * Sets the handler to be invoked when the editor screen commits the new entity.
     * <p>
     * Note that handler is invoked if the editor is opened in {@link OpenMode#DIALOG} mode.
     * <p>
     * The preferred way to set the handler is using a controller method annotated with {@link Install}, e.g.:
     * <pre>
     * &#64;Install(to = "petsTable.create", subject = "afterCommitHandler")
     * protected void petsTableCreateAfterCommitHandler(Pet entity) {
     *     System.out.println("Created " + entity);
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
     * Note that supplier is invoked if the editor is opened in {@link OpenMode#DIALOG} mode.
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
    protected void setFlowUiComponentProperties(FlowUiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getGridCreateShortcut());
    }

    @Autowired
    public void setScreenNavigators(ScreenNavigators screenNavigators) {
        this.screenNavigators = screenNavigators;
    }

    @Autowired
    public void setAccessManager(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Autowired
    public void setDialogWindowBuilders(DialogWindowBuilders dialogWindowBuilders) {
        this.dialogWindowBuilders = dialogWindowBuilders;
    }

    @Override
    protected boolean isPermitted() {
        if (target == null /*|| !(target.getItems() instanceof EntityDataUnit)*/) {
            return false;
        }

        // TODO: add security
/*        MetaClass metaClass = ((EntityDataUnit) target.getItems()).getEntityMetaClass();
        if (metaClass == null) {
            return true;
        }

        UiEntityContext entityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(entityContext);

        if (!entityContext.isCreatePermitted()) {
            return false;
        }*/

        return super.isPermitted();
    }

    /**
     * Executes the action.
     */
    @Override
    public void execute() {
        checkTarget();

        if (!(target.getItems() instanceof EntityDataUnit)) {
            throw new IllegalStateException(String.format("%s target items is null or does not implement %s",
                    getClass().getSimpleName(), EntityDataUnit.class.getSimpleName()));
        }

        if (openMode == OpenMode.DIALOG
                || isComponentAttachedToDialog((Component) target)) {
            openDialog();
        } else {
            navigateToScreen();
        }
    }

    protected void navigateToScreen() {
        EditorNavigator<E> navigator = screenNavigators.editor((target));

        navigator = navigator.newEntity();

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
    protected void openDialog() {
        EditorWindowBuilder<E, Screen> editorBuilder = dialogWindowBuilders.editor(target);

        editorBuilder = screenInitializer.initWindowBuilder(editorBuilder);

        if (newEntitySupplier != null) {
            E entity = newEntitySupplier.get();
            editorBuilder.newEntity(entity);
        } else {
            editorBuilder.newEntity();
        }

        if (initializer != null) {
            editorBuilder = editorBuilder.withInitializer(initializer);
        }

        if (transformation != null) {
            editorBuilder.withTransformation(transformation);
        }

        DialogWindow<?> dialogWindow = editorBuilder.build();
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
    public CreateAction<E> withScreenId(@Nullable String screenId) {
        setScreenId(screenId);
        return this;
    }

    /**
     * @see #setScreenClass(Class)
     */
    public CreateAction<E> withScreenClass(@Nullable Class<? extends Screen> screenClass) {
        setScreenClass(screenClass);
        return this;
    }

    /**
     * @see #setRouteParameters(RouteParameters)
     */
    public CreateAction<E> withRouteParameters(@Nullable RouteParameters routeParameters) {
        setRouteParameters(routeParameters);
        return this;
    }

    /**
     * @see #setQueryParameters(QueryParameters)
     */
    public CreateAction<E> withQueryParameters(@Nullable QueryParameters queryParameters) {
        setQueryParameters(queryParameters);
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
    public <S extends Screen> CreateAction<E> withAfterCloseHandler(
            @Nullable Consumer<AfterCloseEvent<S>> afterCloseHandler) {
        setAfterCloseHandler(afterCloseHandler);
        return this;
    }

    /**
     * @see #setAfterCommitHandler(Consumer)
     */
    public CreateAction<E> withAfterCommitHandler(@Nullable Consumer<E> afterCommitHandler) {
        setAfterCommitHandler(afterCommitHandler);
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
