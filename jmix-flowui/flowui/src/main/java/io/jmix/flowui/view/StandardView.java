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

package io.jmix.flowui.view;

import com.vaadin.flow.component.applayout.AppLayout;
import io.jmix.flowui.component.layout.ViewLayout;

/**
 * Base class of regular views opened either inside {@link AppLayout} or in a {@link DialogWindow}.
 */
public class StandardView extends View<ViewLayout> {

    @Override
    protected ViewLayout initContent() {
        ViewLayout content = super.initContent();
        content.setSizeFull();

        return content;
    }
}
