/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioElementsGroup;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Base interface for actions aware dialog facets.
 *
 * @see OptionDialogFacet
 * @see InputDialogFacet
 *
 * @param <T> dialog facet type
 */
public interface ActionsAwareDialogFacet<T> {

    /**
     * Sets dialog actions.
     *
     * @param actions actions
     */
    @StudioElementsGroup(xmlElement = "actions", icon = "icon/actions.svg")
    void setActions(@Nullable Collection<DialogAction<T>> actions);

    /**
     * @return dialog actions
     */
    @Nullable
    Collection<DialogAction<T>> getActions();

    /**
     * The event that is fired when {@link DialogAction#actionHandler} is triggered.
     */
    class DialogActionPerformedEvent<T> {

        protected T dialog;
        protected DialogAction dialogAction;

        public DialogActionPerformedEvent(T dialog, DialogAction dialogAction) {
            this.dialog = dialog;
            this.dialogAction = dialogAction;
        }

        public T getDialog() {
            return dialog;
        }

        public DialogAction getDialogAction() {
            return dialogAction;
        }
    }

    /**
     * Immutable POJO that stores dialog action settings.
     */
    @StudioElement(xmlElement = "action", icon = "icon/action.svg")
    class DialogAction<T> {

        protected final String id;
        protected final String caption;
        protected final String description;
        protected final String icon;
        protected final boolean primary;

        protected Consumer<DialogActionPerformedEvent<T>> actionHandler;

        public DialogAction(String id, @Nullable String caption, @Nullable String description, @Nullable String icon, boolean primary) {
            this.id = id;
            this.caption = caption;
            this.description = description;
            this.icon = icon;
            this.primary = primary;
        }

        @StudioProperty(type = PropertyType.COMPONENT_ID, required = true)
        public String getId() {
            return id;
        }

        @StudioProperty(type = PropertyType.LOCALIZED_STRING)
        @Nullable
        public String getCaption() {
            return caption;
        }

        @StudioProperty(type = PropertyType.LOCALIZED_STRING)
        @Nullable
        public String getDescription() {
            return description;
        }

        @StudioProperty(type = PropertyType.ICON_ID)
        @Nullable
        public String getIcon() {
            return icon;
        }

        @StudioProperty(name = "primary", defaultValue = "false")
        public boolean isPrimary() {
            return primary;
        }

        @Nullable
        public Consumer<DialogActionPerformedEvent<T>> getActionHandler() {
            return actionHandler;
        }

        /**
         * INTERNAL.
         * <p>
         * Intended to set handlers via {@code @Install} annotation.
         *
         * @param actionHandler action handler
         */
        @Internal
        public void setActionHandler(Consumer<DialogActionPerformedEvent<T>> actionHandler) {
            this.actionHandler = actionHandler;
        }
    }
}
