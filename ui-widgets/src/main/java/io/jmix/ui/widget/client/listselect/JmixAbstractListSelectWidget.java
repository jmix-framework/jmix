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

package io.jmix.ui.widget.client.listselect;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.OptionElement;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.ui.VListSelect;

import java.util.function.Consumer;

public abstract class JmixAbstractListSelectWidget extends VListSelect {

    protected Consumer<Integer> doubleClickListener;

    public JmixAbstractListSelectWidget() {
        select.addDoubleClickHandler(event -> {
            if (!isEnabled() || isReadOnly()) {
                return;
            }

            Element element = WidgetUtil.getElementUnderMouse(event.getNativeEvent());

            if (OptionElement.is(element)) {
                doubleClickListener.accept(((OptionElement) element).getIndex());
            }
        });
    }

    public Consumer<Integer> getDoubleClickListener() {
        return doubleClickListener;
    }

    public void setDoubleClickListener(Consumer<Integer> doubleClickListener) {
        this.doubleClickListener = doubleClickListener;
    }

    @Override
    protected void selectionEvent(Object source) {
        if (!isEnabled() || isReadOnly()) {
            return;
        }

        super.selectionEvent(source);
    }
}
