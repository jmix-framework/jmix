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

import io.jmix.ui.widget.client.tokenlistlabel.JmixTokenListLabelServerRpc;
import io.jmix.ui.widget.client.tokenlistlabel.JmixTokenListLabelState;
import com.vaadin.ui.CssLayout;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Is used for TokenList from compatibility module.
 */
@Deprecated
public class JmixTokenListLabel extends CssLayout {

    protected static final String LABEL_STYLENAME = "jmix-tokenlist-label";

    protected ClickListener clickListener;

    protected List<RemoveTokenListener> listeners;

    protected JmixTokenListLabelServerRpc rpc = new JmixTokenListLabelServerRpc() {
        @Override
        public void removeToken() {
            fireRemoveListeners();
        }

        @Override
        public void itemClick() {
            fireClick();
        }
    };

    public JmixTokenListLabel() {
        registerRpc(rpc);
        setStyleName(LABEL_STYLENAME);
    }

    @Override
    protected JmixTokenListLabelState getState() {
        return (JmixTokenListLabelState) super.getState();
    }

    @Override
    protected JmixTokenListLabelState getState(boolean markAsDirty) {
        return (JmixTokenListLabelState) super.getState(markAsDirty);
    }

    public void addListener(RemoveTokenListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeListener(RemoveTokenListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listeners = null;
            }
        }
    }

    public void setClickListener(@Nullable ClickListener clickListener) {
        this.clickListener = clickListener;
        getState().canOpen = clickListener != null;
    }

    private void fireRemoveListeners() {
        if (listeners != null) {
            for (final RemoveTokenListener listener : listeners) {
                listener.removeToken(this);
            }
        }
    }

    private void fireClick() {
        if (clickListener != null) {
            clickListener.onClick(this);
        }
    }

    public interface RemoveTokenListener {
        void removeToken(JmixTokenListLabel source);
    }

    public interface ClickListener {
        void onClick(JmixTokenListLabel source);
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
}