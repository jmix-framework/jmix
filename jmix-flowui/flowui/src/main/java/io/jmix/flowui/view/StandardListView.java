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

package io.jmix.flowui.view;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.LookupComponent;
import io.jmix.flowui.component.LookupComponent.MultiSelectLookupComponent;
import io.jmix.flowui.kit.component.SelectionChangeNotifier;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Base class of entity list views.
 *
 * @param <E> entity class
 */
public class StandardListView<E> extends StandardView implements LookupView<E>, MultiSelectLookupView {

    protected static final String LOOKUP_ACTIONS_LAYOUT_DEFAULT_ID = "lookupActions";
    protected static final String SELECT_ACTION_DEFAULT_ID = "selectAction";
    protected static final String DISCARD_ACTION_DEFAULT_ID = "discardAction";

    protected Consumer<Collection<E>> selectionHandler;
    protected Predicate<ValidationContext<E>> selectionValidator;

    public StandardListView() {
        addBeforeShowListener(this::onBeforeShow);
    }

    private void onBeforeShow(BeforeShowEvent event) {
        setupLookupComponent();
        setupSaveShortcut();
    }

    protected void setupLookupComponent() {
        if (selectionHandler != null) {
            // TODO: gg, implement
//            getLookupComponent().setLookupSelectHandler(this::select);
        }
    }

    protected void setupSaveShortcut() {
        if (selectionHandler == null) {
            // window was not opened as Lookup
            getSelectAction()
                    .ifPresent(selectAction -> selectAction.setShortcutCombination(null));
        }
    }

    @Override
    public Optional<Consumer<Collection<E>>> getSelectionHandler() {
        return Optional.ofNullable(selectionHandler);
    }

    @Override
    public void setSelectionHandler(@Nullable Consumer<Collection<E>> selectionHandler) {
        this.selectionHandler = selectionHandler;

        getLookupActionsLayout().ifPresent(lookupActionsLayout -> {
            lookupActionsLayout.setVisible(true);

            LookupComponent<E> lookupComponent = getLookupComponent();
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
    public LookupComponent<E> getLookupComponent() {
        return findLookupComponent()
                .orElseThrow(() -> new IllegalStateException(
                        String.format("%s does not declare @%s", getClass(),
                                io.jmix.flowui.view.LookupComponent.class.getSimpleName())
                ));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<LookupComponent<E>> findLookupComponent() {
        io.jmix.flowui.view.LookupComponent annotation =
                getClass().getAnnotation(io.jmix.flowui.view.LookupComponent.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            return Optional.empty();
        }

        return getContent()
                .findComponent(annotation.value())
                .map(component -> (LookupComponent<E>) component);
    }

    @Override
    public Optional<Predicate<ValidationContext<E>>> getSelectionValidator() {
        return Optional.ofNullable(selectionValidator);
    }

    @Override
    public void setSelectionValidator(@Nullable Predicate<ValidationContext<E>> selectionValidator) {
        this.selectionValidator = selectionValidator;
    }

    @Override
    public void setLookupComponentMultiSelect(boolean multiSelect) {
        LookupComponent<E> lookupComponent = getLookupComponent();

        if (lookupComponent instanceof MultiSelectLookupComponent) {
            ((MultiSelectLookupComponent<E>) lookupComponent).enableMultiSelect();
        }
    }

    @Override
    public OperationResult handleSelection() {
        if (selectionHandler == null) {
            throw new IllegalStateException("Can't handle selection. " +
                    "Window was not opened as Lookup");
        }

        Collection<E> selectedItems = getLookupComponent().getSelectedItems();

        return validateSelectedItems(selectedItems)
                .compose(() -> close(StandardOutcome.SELECT))
                .compose(() -> doSelect(selectedItems));
    }

    protected OperationResult validateSelectedItems(Collection<E> items) {
        return getSelectionValidator()
                .map(validator -> {
                    boolean valid = validator.test(new ValidationContext<>(this, items));
                    return valid ? OperationResult.success() : OperationResult.fail();
                })
                .orElse(OperationResult.success());
    }

    protected OperationResult doSelect(Collection<E> items) {
        getSelectionHandler().ifPresent(selectHandler ->
                selectHandler.accept(items));

        return OperationResult.success();
    }

    @Override
    public OperationResult closeWithDiscard() {
        return close(StandardOutcome.DISCARD);
    }

    protected Optional<Action> getSelectAction() {
        return Optional.ofNullable(getViewActions().getAction(SELECT_ACTION_DEFAULT_ID));
    }

    protected Optional<Action> getDiscardAction() {
        return Optional.ofNullable(getViewActions().getAction(DISCARD_ACTION_DEFAULT_ID));
    }
}
