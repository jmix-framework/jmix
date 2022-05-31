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

package io.jmix.flowui.sys;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.ExtendedClientDetails;
import com.vaadin.flow.component.page.Page.ExtendedClientDetailsReceiver;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("flowUI_ExtendedClientDetailsProvider")
public class ExtendedClientDetailsProvider {

    @Nullable
    public ExtendedClientDetails getExtendedClientDetails() {
        return getExtendedClientDetails(UI.getCurrent());
    }

    @Nullable
    public ExtendedClientDetails getExtendedClientDetails(UI ui) {
        return ui.getInternals().getExtendedClientDetails();
    }

    public void retrieveExtendedClientDetails(ExtendedClientDetailsReceiver receiver) {
        retrieveExtendedClientDetails(UI.getCurrent(), receiver);
    }

    public void retrieveExtendedClientDetails(UI ui, ExtendedClientDetailsReceiver receiver) {
        ui.getPage().retrieveExtendedClientDetails(receiver);
    }
}
