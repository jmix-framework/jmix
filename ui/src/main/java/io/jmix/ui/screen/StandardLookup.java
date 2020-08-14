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
import io.jmix.core.JmixEntity;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.LookupComponent;
import io.jmix.ui.component.LookupComponent.LookupSelectionChangeNotifier;
import io.jmix.ui.component.Window;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.icon.Icons;
import io.jmix.ui.util.OperationResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Base class for lookup screens.
 *
 * @param <T> type of entity
 */
public class StandardLookup<T extends JmixEntity> extends Screen implements LookupScreen<T>, MultiSelectLookupScreen {
    protected Consumer<Collection<T>> selectHandler;
    protected Predicate<ValidationContext<T>> selectValidator;

    public StandardLookup() {
        addInitListener(this::initActions);
        addBeforeShowListener(this::beforeShow);
    }

    protected void initActions(@SuppressWarnings("unused") InitEvent event) {
        Window window = getWindow();

        Messages messages = getApplicationContext().getBean(Messages.class);
        Icons icons = (Icons) getApplicationContext().getBean(Icons.NAME);

        String commitShortcut = getApplicationContext().getBean(UiProperties.class).getCommitShortcut();

        Action commitAction = new BaseAction(LOOKUP_SELECT_ACTION_ID)
                .withCaption(messages.getMessage("actions.Select"))
                .withIcon(icons.get(JmixIcon.LOOKUP_OK))
                .withPrimary(true)
                .withShortcut(commitShortcut)
                .withHandler(this::select);

        window.addAction(commitAction);

        Action closeAction = new BaseAction(LOOKUP_CANCEL_ACTION_ID)
                .withCaption(messages.getMessage("actions.Cancel"))
                .withIcon(icons.get(JmixIcon.LOOKUP_CANCEL))
                .withHandler(this::cancel);

        window.addAction(closeAction);
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
    protected LookupComponent<T> getLookupComponent() {
        io.jmix.ui.screen.LookupComponent annotation =
                getClass().getAnnotation(io.jmix.ui.screen.LookupComponent.class);
        if (annotation == null || Strings.isNullOrEmpty(annotation.value())) {
            throw new IllegalStateException(
                    String.format("StandardLookup %s does not declare @LookupComponent", getClass())
            );
        }
        return (LookupComponent) getWindow().getComponentNN(annotation.value());
    }

    protected void select(@SuppressWarnings("unused") Action.ActionPerformedEvent event) {
        if (selectHandler == null) {
            // window opened not as Lookup
            return;
        }

        LookupComponent<T> lookupComponent = getLookupComponent();
        Collection<T> lookupSelectedItems = lookupComponent.getLookupSelectedItems();
        select(lookupSelectedItems);
    }

    protected void cancel(@SuppressWarnings("unused") Action.ActionPerformedEvent event) {
        close(WINDOW_DISCARD_AND_CLOSE_ACTION);
    }

    protected void select(Collection<T> items) {
        boolean valid = true;
        if (selectValidator != null) {
            valid = selectValidator.test(new ValidationContext<>(this, items));
        }

        if (valid) {
            OperationResult result = close(LOOKUP_SELECT_CLOSE_ACTION);
            if (selectHandler != null) {
                result.then(() -> selectHandler.accept(items));
            }
        }
    }

    @Override
    public void setLookupComponentMultiSelect(boolean multiSelect) {
        // LookupComponent<T> lookupComponent = getLookupComponent();

        // todo implement
        /*if (lookupComponent instanceof Table) {
            ((Table<T>) lookupComponent).setMultiSelect(multiSelect);
        } else if (lookupComponent instanceof DataGrid) {
            ((DataGrid<T>) lookupComponent).setSelectionMode(multiSelect
                    ? DataGrid.SelectionMode.MULTI
                    : DataGrid.SelectionMode.SINGLE);
        } else if (lookupComponent instanceof Tree) {
            ((Tree<T>) lookupComponent).setSelectionMode(multiSelect
                    ? Tree.SelectionMode.MULTI
                    : Tree.SelectionMode.SINGLE);
        }*/
    }
}