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

package io.jmix.flowui.action;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.icon.JmixFontIcon;

public class DialogAction extends SecuredBaseAction<DialogAction> {

    public enum Type {

        OK("ok", "actions.Ok", JmixFontIcon.DIALOG_OK.create()),
        CANCEL("cancel", "actions.Cancel", JmixFontIcon.DIALOG_CANCEL.create()),
        YES("yes", "actions.Yes", JmixFontIcon.DIALOG_YES.create()),
        NO("no", "actions.No", JmixFontIcon.DIALOG_NO.create()),
        CLOSE("close", "actions.Close", JmixFontIcon.DIALOG_CLOSE.create());

        private final String id;
        private final String msgKey;
        private final Component icon;

        Type(String id, String msgKey, Component icon) {
            this.id = id;
            this.msgKey = msgKey;
            this.icon = icon;
        }

        public String getId() {
            return id;
        }

        public String getMsgKey() {
            return msgKey;
        }

        public Component getIcon() {
            return ComponentUtils.copyIcon(icon);
        }
    }

    protected final Type type;

    public DialogAction(Type type) {
        super(type.id);

        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
