package io.jmix.flowui.action.entitypicker;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindowBuilders;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ScreenOpeningAction;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.*;
import io.jmix.flowui.screen.builder.EditorWindowBuilder;
import io.jmix.flowui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkState;

@ActionType(EntityOpenAction.ID)
public class EntityOpenAction<E> extends PickerAction<EntityOpenAction<E>, EntityPickerComponent<E>, E>
        implements ScreenOpeningAction {

    public static final String ID = "entity_open";

    protected Messages messages;
    protected Notifications notifications;
    protected DialogWindowBuilders dialogBuilders;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Consumer<E> afterCommitHandler;
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

        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.SEARCH);
    }

    @Autowired
    public void setDialogBuilders(DialogWindowBuilders dialogBuilders) {
        this.dialogBuilders = dialogBuilders;
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
    protected void setFlowUiComponentProperties(FlowUiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getPickerOpenShortcut());
    }

    @Override
    public void setTarget(@Nullable EntityPickerComponent<E> target) {
        checkState(target instanceof HasValue,
                "A component must implement " + HasValue.class.getSimpleName());

        super.setTarget(target);
    }

    public void setAfterCommitHandler(Consumer<E> afterCommitHandler) {
        this.afterCommitHandler = afterCommitHandler;
    }

    public void setTransformation(Function<E, E> transformation) {
        this.transformation = transformation;
    }

    @Nullable
    @Override
    public OpenMode getOpenMode() {
        // Lookup screen opens in a dialog window only
        return OpenMode.DIALOG;
    }

    @Override
    public void setOpenMode(@Nullable OpenMode openMode) {
        throw new UnsupportedOperationException("Lookup screen opens in a dialog window only");
    }

    @Nullable
    @Override
    public String getScreenId() {
        return screenInitializer.getScreenId();
    }

    @Override
    public void setScreenId(@Nullable String screenId) {
        screenInitializer.setScreenId(screenId);
    }

    @Nullable
    @Override
    public Class<? extends Screen> getScreenClass() {
        return screenInitializer.getScreenClass();
    }

    @Override
    public void setScreenClass(@Nullable Class<? extends Screen> screenClass) {
        screenInitializer.setScreenClass(screenClass);
    }

    @Nullable
    @Override
    public RouteParameters getRouteParameters() {
        // Lookup screen opens in a dialog window only
        return null;
    }

    @Override
    public void setRouteParameters(@Nullable RouteParameters routeParameters) {
        throw new UnsupportedOperationException("Lookup screen opens in a dialog window only");
    }

    @Nullable
    @Override
    public QueryParameters getQueryParameters() {
        // Lookup screen opens in a dialog window only
        return null;
    }

    @Override
    public void setQueryParameters(@Nullable QueryParameters queryParameters) {
        throw new UnsupportedOperationException("Lookup screen opens in a dialog window only");
    }

    @Override
    public <S extends Screen> void setAfterCloseHandler(@Nullable Consumer<DialogWindow.AfterCloseEvent<S>> afterCloseHandler) {
        screenInitializer.setAfterCloseHandler(afterCloseHandler);
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

        EditorWindowBuilder<E, Screen> builder = dialogBuilders.editor(target);

        builder = screenInitializer.initWindowBuilder(builder);

        if (transformation != null) {
            builder.withTransformation(transformation);
        }

        DialogWindow<?> dialogWindow = builder.build();
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

    public EntityOpenAction<E> withAfterCommitHandler(Consumer<E> afterCommitHandler) {
        setAfterCommitHandler(afterCommitHandler);
        return this;
    }

    public EntityOpenAction<E> withTransformation(Function<E, E> transformation) {
        setTransformation(transformation);
        return this;
    }

    @SuppressWarnings("unchecked")
    protected boolean isEmpty() {
        return ((HasValue<?, E>) target).isEmpty();
    }
}
