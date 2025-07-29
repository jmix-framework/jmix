/*
 * Copyright 2025 Haulmont.
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

package io.jmix.vaadincommercialcomponents.component.loader;

import com.vaadin.flow.component.board.Row;
import io.jmix.flowui.xml.layout.loader.container.AbstractContainerLoader;

public class RowLoader extends AbstractContainerLoader<Row> {

    @Override
    protected Row createComponent() {
        return factory.create(Row.class) ;
    }

    @Override
    public void initComponent() {
        super.initComponent();
        createSubComponents(resultComponent, element);
    }

    @Override
    public void loadComponent() {
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);

        loadSubComponents();
    }
}
