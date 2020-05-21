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

package io.jmix.ui.widget.client.verticalactionslayout;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;
import io.jmix.ui.widget.client.orderedactionslayout.JmixOrderedActionsLayoutWidget;
import com.vaadin.client.ComputedStyle;

public class JmixVerticalActionsLayoutWidget extends JmixOrderedActionsLayoutWidget {

    public static final String CLASSNAME = "v-verticallayout";

    public JmixVerticalActionsLayoutWidget(){
        super(CLASSNAME, true);
    }

    @Override
    protected int computeWidgetHeight(Widget w) {
        Element el = w.getElement();

        int computedHeight = (int) new ComputedStyle(el).getHeight();
        int measuredHeight = getLayoutManager().getOuterHeight(el);

        return measuredHeight >= 0 ? measuredHeight : computedHeight;
    }
}
