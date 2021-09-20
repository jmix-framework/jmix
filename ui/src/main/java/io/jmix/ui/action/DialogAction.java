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
package io.jmix.ui.action;

import io.jmix.ui.Dialogs;
import io.jmix.ui.component.KeyCombination;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.icon.Icons;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Standard action for option dialogs.
 * <br>
 * You can use fluent API to create instances of DialogAction and assign handlers to them:
 * <pre>{@code
 *     showOptionDialog(
 *             "Select options",
 *             "Do you want to print all rows?",
 *             MessageType.CONFIRMATION,
 *             new Action[]{
 *                     new DialogAction(Type.YES).withHandler(event -> {
 *                         // add action logic here
 *                     }),
 *                     new DialogAction(Type.NO)
 *                             .withCaption("Print selected")
 *                             .withIcon(JmixIcon.PRINT.source())
 *                             .withStyleName("print-selected")
 *                             .withHandler(event -> {
 *                         // add action logic here
 *                     }),
 *                     new DialogAction(Type.CANCEL)
 *             });
 * }</pre>
 *
 * @see Dialogs
 */
public class DialogAction extends BaseAction {

    public enum Type {
        OK("ok", "actions.Ok", JmixIcon.DIALOG_OK),
        CANCEL("cancel", "actions.Cancel", JmixIcon.DIALOG_CANCEL),
        YES("yes", "actions.Yes", JmixIcon.DIALOG_YES),
        NO("no", "actions.No", JmixIcon.DIALOG_NO),
        CLOSE("close", "actions.Close", JmixIcon.DIALOG_CLOSE);

        private String id;
        private String msgKey;
        private Icons.Icon iconKey;

        Type(String id, String msgKey, Icons.Icon iconKey) {
            this.id = id;
            this.msgKey = msgKey;
            this.iconKey = iconKey;
        }

        public String getId() {
            return id;
        }

        public String getMsgKey() {
            return msgKey;
        }

        public Icons.Icon getIconKey() {
            return iconKey;
        }
    }

    protected Type type;
    protected String styleName;

    public DialogAction(String id) {
        super(id);
    }

    public DialogAction(Type type) {
        super(type.id);

        this.type = type;
    }

    public DialogAction(Type type, boolean primary) {
        this(type);
        this.primary = primary;
    }

    public DialogAction(Type type, Status status) {
        this(type);
        this.primary = status == Status.PRIMARY;
    }

    @Nullable
    public Type getType() {
        return type;
    }

    /**
     * @return style name or {@code null} if not set
     */
    @Nullable
    public String getStyleName() {
        return styleName;
    }

    /**
     * Sets style name that will be used in the corresponding button of the dialog.
     *
     * @param styleName style name
     * @return current instance of action
     */
    public DialogAction withStyleName(@Nullable String styleName) {
        this.styleName = styleName;
        return this;
    }

    /**
     * Set caption using fluent API method.
     *
     * @param caption caption
     * @return current instance of action
     */
    public DialogAction withCaption(@Nullable String caption) {
        this.caption = caption;
        return this;
    }

    /**
     * Set description using fluent API method.
     *
     * @param description description
     * @return current instance of action
     */
    public DialogAction withDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    /**
     * Set icon using fluent API method.
     *
     * @param icon icon
     * @return current instance of action
     */
    public DialogAction withIcon(@Nullable String icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Set shortcut using fluent API method.
     *
     * @param shortcut shortcut
     * @return current instance of action
     */
    public DialogAction withShortcut(@Nullable String shortcut) {
        if (shortcut != null) {
            this.shortcut = KeyCombination.create(shortcut);
        }
        return this;
    }

    /**
     * Set action performed event handler using fluent API method. Can be used instead of subclassing BaseAction class.
     *
     * @param handler action performed handler
     * @return current instance of action
     */
    public DialogAction withHandler(Consumer<ActionPerformedEvent> handler) {
        getEventHub().subscribe(ActionPerformedEvent.class, handler);
        return this;
    }

    /**
     * Set whether this action is primary using fluent API method. Can be used instead of subclassing BaseAction class.
     *
     * @param primary primary
     * @return current instance of action
     */
    public DialogAction withPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }
}