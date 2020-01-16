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

package io.jmix.ui.components.presentations.actions;

import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.entity.Presentation;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.ComponentsHelper;
import io.jmix.ui.components.Table;

public class SaveAsPresentationAction extends AbstractEditPresentationAction {

    protected Metadata metadata = AppBeans.get(Metadata.NAME);

    public SaveAsPresentationAction(Table table) {
        super(table, "PresentationsPopup.saveAs");
    }

    @Override
    public void actionPerform(Component component) {
        tableImpl.hidePresentationsPopup();

        Presentation presentation = metadata.create(Presentation.class);
        presentation.setComponentId(ComponentsHelper.getComponentPath(table));

        openEditor(presentation);
    }
}
