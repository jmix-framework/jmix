/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.screen;

import com.google.common.base.Strings;
import io.jmix.core.Messages;
import io.jmix.ui.UiScreenProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.LookupComponent;
import io.jmix.ui.component.*;
import io.jmix.ui.component.LookupComponent.LookupSelectionChangeNotifier;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Base class for lookup screens.
 *
 * @param <T> type of entity
 */
public class StandardLookup<T> extends Screen implements LookupScreen<T>, MultiSelectLookupScreen {
    protected Consumer<Collection<T>> selectHandler;
    protected Predicate<ValidationContext<T>> selectValidator;

    public StandardLookup() {
        addInitListener(this::initActions);
        addBeforeShowListener(this::beforeShow);
    }

    protected void initActions(@SuppressWarnings("unused") InitEvent event) {
        Messages messages = getApplicationContext().getBean(Messages.class);
        Icons icons = getApplicationContext().getBean(Icons.class);

        BaseAction selectAction = (BaseAction) getWindowActionOptional(LOOKUP_SELECT_ACTION_ID)
                .orElseGet(() ->
                        addDefaultSelectAction(messages, icons));
        if (!isScreenAction(selectAction)) {
            selectAction.addActionPerformedListener(this::select);
        }

        BaseAction cancelAction = (BaseAction) getWindowActionOptional(LOOKUP_CANCEL_ACTION_ID)
                .orElseGet(() ->
                        addDefaultCancelAction(messages, icons));
        if (!isScreenAction(cancelAction)) {
            cancelAction.addActionPerformedListener(this::cancel);
        }
    }

    protected Optional<Action> getWindowActionOptional(String id) {
        Action action = getWindow().getAction(id);
        return Optional.ofNullable(action);
    }

    protected Action addDefaultSelectAction(Messages messages, Icons icons) {
        String commitShortcut = getApplicationContext().getBean(UiScreenProperties.class).getCommitShortcut();

        Action action = new BaseAction(LOOKUP_SELECT_ACTION_ID)
                .withCaption(messages.getMessage("actions.Select"))
                .withIcon(icons.get(JmixIcon.LOOKUP_OK))
                .withPrimary(true)
                .withShortcut(commitShortcut);

        getWindow().addAction(action);

        return action;
    }

    protected Action addDefaultCancelAction(Messages messages, Icons icons) {
        Action action = new BaseAction(LOOKUP_CANCEL_ACTION_ID)
                .withCaption(messages.getMessage("actions.Cancel"))
                .withIcon(icons.get(JmixIcon.LOOKUP_CANCEL));

        getWindow().addAction(action);

        return action;
    }

    private void beforeShow(@SuppressWarnings("unused") BeforeShowEvent beforeShowEvent) {
        setupLookupComponent();
        setupCommitShortcut();
    }

    protected void setupCommitShortcut() {
        if (selectHandler == null) {
            // window opened not as Lookup
            Action lookupAction = getWindow().getAction(LOOKUP_SELECT_ACTION_ID);
            if (lookupAction != null) {
                lookupAction.setShortcut(null);
            }
        }
    }

    protected void setupLookupComponent() {
        if (this.selectHandler != null) {
            getLookupComponent().setLookupSelectHandler(this::select);
        }
    }

    @Nullable
    @Override
    public Consumer<Collection<T>> getSelectHandler() {
        return selectHandler;
    }

    @Override
    public void setSelectHandler(@Nullable Consumer<Collection<T>> selectHandler) {
        this.selectHandler = selectHandler;

        Component lookupActionsLayout = getLookupActionsLayout();
        if (lookupActionsLayout != null) {
            lookupActionsLayout.setVisible(true);

            Component lookupComponent = getLookupComponent();
            if (lookupComponent instanceof LookupSelectionChangeNotifier) {
                LookupSelectionChangeNotifier selectionNotifier = (LookupSelectionChangeNotifier) lookupComponent;

                Action commitAction = getWindow().getAction(LOOKUP_SELECT_ACTION_ID);
                if (commitAction != null) {
                    //noinspection unchecked
                    selectionNotifier.addLookupValueChangeListener(valueChangeEvent ->
                            commitAction.setEnabled(!selectionNotifier.getLookupSelectedItems().isEmpty()));

                    commitAction.setEnabled(!selectionNotifier.getLookupSelectedItems().isEmpty());
                }
            }
        }
    }

    @Override
    public Predicate<ValidationContext<T>> getSelectValidator() {
        return selectValidator;
    }

    @Override
    public void setSelectValidator(Predicate<ValidationContext<T>> selectValidator) {
        this.selectValidator = selectValidator;
    }

    @Nullable
    protected Component getLookupActionsLayout() {
        return getWindow().getComponent("lookupActions");
    }

    @SuppressWarnings("unchecked")
    public LookupComponent<T> getLookupComponent() {
        io.jmix.ui.screen.LookupComponent annotation =
                getClass().getAnnotation(io.jmix.ui.screen.LookupComponent.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            throw new IllegalStateException(
                    String.format("StandardLookup %s does not declare @LookupComponent", getClass())
            );
        }
        return (LookupComponent) getWindow().getComponentNN(annotation.value());
    }

    public OperationResult select(@SuppressWarnings("unused") @Nullable Action.ActionPerformedEvent event) {
        if (selectHandler == null) {
            // window opened not as Lookup
            return OperationResult.fail();
        }

        LookupComponent<T> lookupComponent = getLookupComponent();
        Collection<T> lookupSelectedItems = lookupComponent.getLookupSelectedItems();
        return select(lookupSelectedItems);
    }

    public OperationResult cancel(@SuppressWarnings("unused") @Nullable Action.ActionPerformedEvent event) {
        return close(WINDOW_DISCARD_AND_CLOSE_ACTION);
    }

    protected OperationResult select(Collection<T> items) {
        boolean valid = true;
        if (selectValidator != null) {
            valid = selectValidator.test(new ValidationContext<>(this, items));
        }

        if (valid) {
            OperationResult result = close(LOOKUP_SELECT_CLOSE_ACTION);
            if (selectHandler != null) {
                result.then(() -> selectHandler.accept(items));
            }

            return result;
        }

        return OperationResult.fail();
    }

    @Override
    public void setLookupComponentMultiSelect(boolean multiSelect) {
        LookupComponent<T> lookupComponent = getLookupComponent();

        if (lookupComponent instanceof Table) {
            ((Table<T>) lookupComponent).setMultiSelect(multiSelect);
        } else if (lookupComponent instanceof DataGrid) {
            ((DataGrid<T>) lookupComponent).setSelectionMode(multiSelect
                    ? DataGrid.SelectionMode.MULTI
                    : DataGrid.SelectionMode.SINGLE);
        } else if (lookupComponent instanceof Tree) {
            ((Tree<T>) lookupComponent).setSelectionMode(multiSelect
                    ? Tree.SelectionMode.MULTI
                    : Tree.SelectionMode.SINGLE);
        }
    }
}
