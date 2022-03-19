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

package com.haulmont.cuba.web.gui.components.mainwindow;

import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.component.impl.AppWorkAreaImpl;

import java.util.function.Consumer;

@Deprecated
public class WebAppWorkArea extends AppWorkAreaImpl implements AppWorkArea {

    @Override
    public void removeStateChangeListener(Consumer<StateChangeEvent> listener) {
        unsubscribe(StateChangeEvent.class, listener);
    }

    @Override
    protected VBoxLayout createInitialLayout() {
        UiComponents uiComponents = applicationContext.getBean(UiComponents.class);
        return uiComponents.create(VBoxLayout.NAME);
    }
}
