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

package io.jmix.ui.widget.client.tabsheet;

import io.jmix.ui.widget.client.addon.dragdroplayouts.ui.tabsheet.DDTabSheetState;
import com.vaadin.shared.annotations.NoLayout;

public class JmixMainTabSheetState extends DDTabSheetState {

    private static final long serialVersionUID = 4132538424243246049L;

    @NoLayout
    public boolean hasActionsHandlers = false;

    /**
     * Important! If Tab in TabSheet contains components that provide drag and drop features, users may face UI freezes
     * in case of dragging elements to TabSheet or Tab layout.
     */
    @NoLayout
    public boolean ddHtmlEnable = false;
}