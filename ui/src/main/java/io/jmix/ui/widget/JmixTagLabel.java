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

package io.jmix.ui.widget;

import com.vaadin.ui.AbstractComponent;
import io.jmix.ui.widget.client.tagpickerlabel.JmixTagLabelServerRpc;
import io.jmix.ui.widget.client.tagpickerlabel.JmixTagLabelState;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class JmixTagLabel extends AbstractComponent {

    protected Consumer<JmixTagLabel> removeHandler;
    protected Consumer<JmixTagLabel> clickHandler;

    protected JmixTagLabelServerRpc rpc = new JmixTagLabelServerRpc() {
        @Override
        public void removeItem() {
            fireRemoveEvent();
        }

        @Override
        public void itemClick() {
            fireClickEvent();
        }
    };

    public JmixTagLabel() {
        registerRpc(rpc);
    }

    @Override
    protected JmixTagLabelState getState() {
        return (JmixTagLabelState) super.getState();
    }

    @Override
    protected JmixTagLabelState getState(boolean markAsDirty) {
        return (JmixTagLabelState) super.getState(markAsDirty);
    }

    @Nullable
    public Consumer<JmixTagLabel> getRemoveHandler() {
        return removeHandler;
    }

    public void setRemoveHandler(@Nullable Consumer<JmixTagLabel> removeHandler) {
        this.removeHandler = removeHandler;
    }

    @Nullable
    public Consumer<JmixTagLabel> getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(@Nullable Consumer<JmixTagLabel> clickHandler) {
        this.clickHandler = clickHandler;
        getState().clickable = clickHandler != null;
    }

    public void setText(String text) {
        getState().text = text;
    }

    public String getText() {
        return getState(false).text;
    }

    public boolean isEditable() {
        return getState(false).editable;
    }

    public void setEditable(boolean editable) {
        getState().editable = editable;
    }

    protected void fireRemoveEvent() {
        if (removeHandler != null) {
            removeHandler.accept(this);
        }
    }

    protected void fireClickEvent() {
        if (clickHandler != null) {
            clickHandler.accept(this);
        }
    }
}
