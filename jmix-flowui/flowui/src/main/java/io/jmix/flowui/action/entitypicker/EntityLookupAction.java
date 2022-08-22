package io.jmix.flowui.action.entitypicker;

import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.DialogWindowBuilders;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.ViewOpeningAction;
import io.jmix.flowui.action.valuepicker.PickerAction;
import io.jmix.flowui.component.EntityPickerComponent;
import io.jmix.flowui.kit.component.FlowuiComponentUtils;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.sys.ActionViewInitializer;
import io.jmix.flowui.view.DialogWindow.AfterCloseEvent;
import io.jmix.flowui.view.LookupView;
import io.jmix.flowui.view.OpenMode;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.builder.LookupWindowBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@ActionType(EntityLookupAction.ID)
public class EntityLookupAction<E> extends PickerAction<EntityLookupAction<E>, EntityPickerComponent<E>, E>
        implements ViewOpeningAction {

    public static final String ID = "entity_lookup";

    protected DialogWindowBuilders dialogBuilders;

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

        this.icon = FlowuiComponentUtils.convertToIcon(VaadinIcon.ELLIPSIS_DOTS_H);
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
    protected void setFlowUiComponentProperties(FlowuiComponentProperties flowUiComponentProperties) {
        this.shortcutCombination = KeyCombination.create(flowUiComponentProperties.getPickerLookupShortcut());
    }

    public void setSelectValidator(Predicate<LookupView.ValidationContext<E>> selectValidator) {
        this.selectValidator = selectValidator;
    }

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
        throw new UnsupportedOperationException("Lookup view opens in a dialog window only");
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
        throw new UnsupportedOperationException("Lookup view opens in a dialog window only");
    }

    @Nullable
    @Override
    public QueryParametersProvider getQueryParametersProvider() {
        // Lookup view opens in a dialog window only
        return null;
    }

    @Override
    public void setQueryParametersProvider(@Nullable QueryParametersProvider provider) {
        throw new UnsupportedOperationException("Lookup view opens in a dialog window only");
    }

    @Override
    public <S extends View<?>> void setAfterCloseHandler(@Nullable Consumer<AfterCloseEvent<S>> afterCloseHandler) {
        viewInitializer.setAfterCloseHandler(afterCloseHandler);
    }

    @Override
    public void execute() {
        MetaClass metaClass = target.getMetaClass();
        if (metaClass == null) {
            throw new DevelopmentException("Neither metaClass nor dataContainer/property is specified " +
                    "for the " + target.getClass().getSimpleName(), "action ID", getId());
        }

        LookupWindowBuilder<E, View<?>> builder = dialogBuilders.lookup(target);

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

    public EntityLookupAction<E> withSelectValidator(Predicate<LookupView.ValidationContext<E>> selectValidator) {
        setSelectValidator(selectValidator);
        return this;
    }

    public EntityLookupAction<E> withTransformation(Function<Collection<E>, Collection<E>> transformation) {
        setTransformation(transformation);
        return this;
    }
}
