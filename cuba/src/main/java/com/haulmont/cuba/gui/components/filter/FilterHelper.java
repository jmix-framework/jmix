/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.gui.components.filter;

import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.entity.Folder;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.LookupField;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.Tree;
import io.jmix.ui.presentation.TablePresentations;

import java.util.Map;

/**
 * Interface to be implemented by classes with client-specific behaviour that cannot be placed into
 * {@link com.haulmont.cuba.gui.components.filter.FilterDelegate}
 */
public interface FilterHelper {
    String NAME = "cuba_FilterHelper";

    interface TextChangeListener {
        void textChanged(String text);
    }

    abstract class ShortcutListener {
        protected String caption;
        private KeyCombination keyCombination;

        public ShortcutListener(String caption, KeyCombination keyCombination) {
            this.caption = caption;
            this.keyCombination = keyCombination;
        }

        public abstract void handleShortcutPressed();

        public String getCaption() {
            return caption;
        }

        public KeyCombination getKeyCombination() {
            return keyCombination;
        }
    }

    void setLookupNullSelectionAllowed(LookupField lookupField, boolean value);

    void setLookupTextInputAllowed(LookupField lookupField, boolean value);

    /**
     * Saves a folder to a FoldersPane
     *
     * @return saved folder or null if foldersPane not found
     */
    AbstractSearchFolder saveFolder(AbstractSearchFolder folder);

    void openFolderEditWindow(boolean isAppFolder, AbstractSearchFolder folder, TablePresentations presentations, Runnable commitHandler);

    boolean isFolderActionsEnabled();

    boolean isFolderActionsAllowed(Frame frame);

    boolean mainScreenHasFoldersPane(Frame currentFrame);

    void initConditionsDragAndDrop(Tree tree, ConditionsTree conditions);

    Object getFoldersPane();

    void removeFolderFromFoldersPane(Folder folder);

    boolean isTableActionsEnabled();

    void initTableFtsTooltips(ListComponent listComponent, MetaClass metaClass, String searchTerm);

    void removeTableFtsTooltips(ListComponent listComponent);

    void setFieldReadOnlyFocusable(TextField textField, boolean readOnlyFocusable);

    void setComponentFocusable(Component component, boolean focusable);

    void setLookupCaptions(LookupField lookupField, Map<Object, String> captions);

    void addTextChangeListener(TextField textField, TextChangeListener listener);

    void addShortcutListener(TextField textField, ShortcutListener listener);

    void setLookupFieldPageLength(LookupField lookupField, int pageLength);

    void setInternalDebugId(Component component, String id);

    ComponentContainer createSearchButtonGroupContainer();
}
