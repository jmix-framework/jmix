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

import io.jmix.ui.widget.client.fieldgroup.JmixFieldGroupState;
import com.vaadin.ui.Layout;

import javax.annotation.Nullable;

public class JmixFieldGroup extends JmixGroupBox {
    public JmixFieldGroup() {
        setLayout(new JmixFormLayout());
        setSizeUndefined();
    }

    public boolean isBorderVisible() {
        return getState(false).borderVisible;
    }

    public void setBorderVisible(boolean borderVisible) {
        if (getState().borderVisible != borderVisible) {
            getState().borderVisible = borderVisible;
            markAsDirty();
        }
    }

    @Override
    protected JmixFieldGroupState getState() {
        return (JmixFieldGroupState) super.getState();
    }

    @Override
    protected JmixFieldGroupState getState(boolean markAsDirty){
        return (JmixFieldGroupState) super.getState(markAsDirty);
    }

    public JmixFormLayout getLayout() {
        return (JmixFormLayout) super.getContent();
    }

    public void setLayout(@Nullable Layout newLayout) {
        if (newLayout == null) {
            newLayout = new JmixFormLayout();
        }
        if (newLayout instanceof JmixFormLayout) {
            super.setContent(newLayout);
        } else {
            throw new IllegalArgumentException("FieldGroup supports only JmixFormLayout");
        }
    }
}