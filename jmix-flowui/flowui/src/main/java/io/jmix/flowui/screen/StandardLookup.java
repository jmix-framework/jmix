package io.jmix.flowui.screen;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.component.LookupComponent.MultiSelectLookupComponent;
import io.jmix.flowui.component.SelectionChangeNotifier;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StandardLookup<T> extends Screen implements LookupScreen<T>, MultiSelectLookupScreen {

    protected static final String LOOKUP_ACTIONS_LAYOUT_DEFAULT_ID = "lookupActions";
    protected static final String SELECT_ACTION_DEFAULT_ID = "selectAction";
    protected static final String DISCARD_ACTION_DEFAULT_ID = "discardAction";

    protected Consumer<Collection<T>> selectionHandler;
    protected Predicate<ValidationContext<T>> selectionValidator;

    public StandardLookup() {
        addBeforeShowListener(this::onBeforeShow);
    }

    private void onBeforeShow(BeforeShowEvent event) {
        setupLookupComponent();
        setupCommitShortcut();
    }

    protected void setupLookupComponent() {
        if (selectionHandler != null) {
            // TODO: gg, implement
//            getLookupComponent().setLookupSelectHandler(this::select);
        }
    }

    protected void setupCommitShortcut() {
        if (selectionHandler == null) {
            // window was not opened as Lookup
            getSelectAction()
                    .ifPresent(selectAction -> selectAction.setShortcutCombination(null));
        }
    }

    @Override
    public Optional<Consumer<Collection<T>>> getSelectionHandler() {
        return Optional.ofNullable(selectionHandler);
    }

    @Override
    public void setSelectionHandler(@Nullable Consumer<Collection<T>> selectionHandler) {
        this.selectionHandler = selectionHandler;

        getLookupActionsLayout().ifPresent(lookupActionsLayout -> {
            lookupActionsLayout.setVisible(true);

            LookupComponent<T> lookupComponent = getLookupComponent();
            if (lookupComponent instanceof SelectionChangeNotifier) {
                SelectionChangeNotifier<?, ?> selectionNotifier = (SelectionChangeNotifier<?, ?>) lookupComponent;

                getSelectAction()
                        .ifPresent(selectAction -> {
                            selectionNotifier.addSelectionListener(selectionEvent ->
                                    selectAction.refreshState());
                            selectAction.refreshState();
                        });
            }
        });
    }

    protected Optional<Component> getLookupActionsLayout() {
        return getContent().findComponent(LOOKUP_ACTIONS_LAYOUT_DEFAULT_ID);
    }

    @Override
    public LookupComponent<T> getLookupComponent() {
        return findLookupComponent()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("%s does not declare @%s", getClass(),
                                io.jmix.flowui.screen.LookupComponent.class.getSimpleName())
                ));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<LookupComponent<T>> findLookupComponent() {
        io.jmix.flowui.screen.LookupComponent annotation =
                getClass().getAnnotation(io.jmix.flowui.screen.LookupComponent.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            return Optional.empty();
        }

        return getContent()
                .findComponent(annotation.value())
                .map(component -> (LookupComponent<T>) component);
    }

    @Override
    public Optional<Predicate<ValidationContext<T>>> getSelectionValidator() {
        return Optional.ofNullable(selectionValidator);
    }

    @Override
    public void setSelectionValidator(@Nullable Predicate<ValidationContext<T>> selectionValidator) {
        this.selectionValidator = selectionValidator;
    }

    @Override
    public void setLookupComponentMultiSelect(boolean multiSelect) {
        LookupComponent<T> lookupComponent = getLookupComponent();

        if (lookupComponent instanceof MultiSelectLookupComponent) {
            ((MultiSelectLookupComponent<T>) lookupComponent).enableMultiSelect();
        }
    }

    @Override
    public OperationResult handleSelection() {
        if (selectionHandler == null) {
            throw new IllegalStateException("Can't handle selection. " +
                    "Window was not opened as Lookup");
        }

        Collection<T> selectedItems = getLookupComponent().getSelectedItems();

        return validateSelectedItems(selectedItems)
                .compose(() -> close(StandardOutcome.SELECT))
                .compose(() -> doSelect(selectedItems));
    }

    protected OperationResult validateSelectedItems(Collection<T> items) {
        return getSelectionValidator()
                .map(validator -> {
                    boolean valid = validator.test(new ValidationContext<>(this, items));
                    return valid ? OperationResult.success() : OperationResult.fail();
                })
                .orElse(OperationResult.success());
    }

    protected OperationResult doSelect(Collection<T> items) {
        getSelectionHandler().ifPresent(selectHandler ->
                selectHandler.accept(items));

        return OperationResult.success();
    }

    @Override
    public OperationResult closeWithDiscard() {
        return close(StandardOutcome.DISCARD);
    }

    protected Optional<Action> getSelectAction() {
        return Optional.ofNullable(getScreenActions().getAction(SELECT_ACTION_DEFAULT_ID));
    }

    protected Optional<Action> getDiscardAction() {
        return Optional.ofNullable(getScreenActions().getAction(DISCARD_ACTION_DEFAULT_ID));
    }
}
