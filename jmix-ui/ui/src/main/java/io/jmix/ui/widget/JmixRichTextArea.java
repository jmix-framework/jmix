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

package io.jmix.ui.widget;

import com.vaadin.ui.RichTextArea;
import elemental.json.Json;
import io.jmix.ui.widget.client.richtextarea.JmixRichTextAreaServerRpc;
import io.jmix.ui.widget.client.richtextarea.JmixRichTextAreaState;

import java.util.Map;

public class JmixRichTextArea extends RichTextArea {

    protected boolean lastUserActionSanitized;

    protected JmixRichTextAreaServerRpc rpc = new JmixRichTextAreaServerRpc() {
        @Override
        public void setText(String text, boolean lastUserActionSanitized) {
            setLastUserActionSanitized(lastUserActionSanitized);
            updateDiffstate("value", Json.create(text));
            if (!setValue(text, true)) {
                // The value was not updated, this could happen if the field has
                // been set to readonly on the server and the client does not
                // know about it yet. Must re-send the correct state back.
                markAsDirty();
            }
        }
    };

    public JmixRichTextArea() {
        registerRpc(rpc);
        setValue("");
    }

    @Override
    public JmixRichTextAreaState getState() {
        return (JmixRichTextAreaState) super.getState();
    }

    public void setLocaleMap(Map<String, String> localeMap) {
        getState().localeMap = localeMap;
    }

    public void setLastUserActionSanitized(boolean lastUserActionSanitized) {
        this.lastUserActionSanitized = lastUserActionSanitized;
    }

    public boolean isLastUserActionSanitized() {
        return lastUserActionSanitized;
    }
}
