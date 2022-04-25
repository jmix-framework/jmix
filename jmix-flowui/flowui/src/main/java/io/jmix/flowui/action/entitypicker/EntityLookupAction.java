package io.jmix.flowui.action.entitypicker;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindowBuilders;
import io.jmix.flowui.FlowUiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ScreenOpeningAction;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.kit.component.FlowUiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.screen.*;
import io.jmix.flowui.screen.builder.LookupWindowBuilder;
import io.jmix.flowui.sys.ActionScreenInitializer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@ActionType(EntityLookupAction.ID)
public class EntityLookupAction<E> extends PickerAction<EntityLookupAction<E>, EntityPickerComponent<E>, E>
        implements ScreenOpeningAction {

    public static final String ID = "entity_lookup";

    protected DialogWindowBuilders dialogBuilders;

    protected ActionScreenInitializer screenInitializer = new ActionScreenInitializer();

    protected Predicate<LookupScreen.ValidationContext<E>> selectValidator;
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

        this.icon = FlowUiComponentUtils.iconToSting(VaadinIcon.ELLIPSIS_DOTS_H);
    }

    @Autowired
    public void setDialogBuilders(DialogWindowBuilders dialogBuilders) {
        this.dialogBuilders = dialogBuilders;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.text = messages.getMessage("actions.entityPicker.lookup.description");
    }

    @Autowired
    protected void setFlowUiComponentProperties(FlowUiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getPickerLookupShortcut());
    }

    public void setSelectValidator(Predicate<LookupScreen.ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    public void setTransformation(Function<Collection<E>, Collection<E>> transformation) {
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

    // TODO: gg, editable?

    @Override
    public void execute() {
        MetaClass metaClass = target.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the " + target.getClass().getSimpleName(), "action ID", getId());
        }

        LookupWindowBuilder<E, Screen> builder = dialogBuilders.lookup(target);

        builder = screenInitializer.initWindowBuilder(builder);

        if (selectValidator != null) {
            builder = builder.withSelectValidator(selectValidator);
        }

        if (transformation != null) {
            builder = builder.withTransformation(transformation);
        }

        builder.open();
    }

    public EntityLookupAction<E> withSelectValidator(Predicate<LookupScreen.ValidationContext<E>> selectValidator) {
        setSelectValidator(selectValidator);
        return this;
    }

    public EntityLookupAction<E> withTransformation(Function<Collection<E>, Collection<E>> transformation) {
        setTransformation(transformation);
        return this;
    }
}
