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

import io.jmix.ui.widget.client.popupview.JmixPopupViewState;
import io.jmix.ui.widget.client.popupview.PopupPosition;
import com.vaadin.ui.PopupView;

import javax.annotation.Nullable;
import java.util.Objects;

public class JmixPopupView extends PopupView {

    public JmixPopupView(PopupView.Content content) {
        super(content);
    }

    @Override
    protected JmixPopupViewState getState() {
        return (JmixPopupViewState) super.getState();
    }

    @Override
    protected JmixPopupViewState getState(boolean markAsDirty) {
        return (JmixPopupViewState) super.getState(markAsDirty);
    }

    public void setPopupPosition(@Nullable PopupPosition position) {
        if (!Objects.equals(position, getPopupPosition())) {
            getState(true).popupPosition = position;
            if (position != null) {
                setPopupPosition(-1, -1);
            }
        }
    }

    @Nullable
    public PopupPosition getPopupPosition() {
        return getState(false).popupPosition;
    }

    public void setPopupPosition(int top, int left) {
        setPopupPositionTop(top);
        setPopupPositionLeft(left);
    }

    public void setPopupPositionTop(int top) {
        if (!Objects.equals(top, getPopupPositionTop())) {
            getState(true).popupPositionTop = top;
            if (top != -1) {
                setPopupPosition(null);
            }
        }
    }

    public int getPopupPositionTop() {
        return getState(false).popupPositionTop;
    }

    public void setPopupPositionLeft(int left) {
        if (!Objects.equals(left, getPopupPositionLeft())) {
            getState(true).popupPositionLeft = left;
            if (left != -1) {
                setPopupPosition(null);
            }
        }
    }

    public int getPopupPositionLeft() {
        return getState(false).popupPositionLeft;
    }
}
