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
import io.jmix.core.annotation.Internal;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Provides methods to retrieve and manage {@link ExtendedClientDetails} for a specified {@link UI}.
 */
@Internal
@Component("flowui_ExtendedClientDetailsProvider")
public class ExtendedClientDetailsProvider {

    @Nullable
    public ExtendedClientDetails getExtendedClientDetails() {
        return getExtendedClientDetails(UI.getCurrent());
    }

    /**
     * Retrieves the extended client details associated with the specified {@link UI} instance.
     *
     * @param ui the {@link UI} instance from which to retrieve the {@link ExtendedClientDetails}
     * @return the {@link ExtendedClientDetails} associated with the specified {@link UI}
     */
    @Nullable
    public ExtendedClientDetails getExtendedClientDetails(UI ui) {
        return ui.getInternals().getExtendedClientDetails();
    }

    /**
     * Retrieves the extended client details for the current {@link UI} instance and passes them to the specified
     * {@link ExtendedClientDetailsReceiver}.
     *
     * @param receiver the {@link ExtendedClientDetailsReceiver} that handles the received extended client details
     */
    public void retrieveExtendedClientDetails(ExtendedClientDetailsReceiver receiver) {
        retrieveExtendedClientDetails(UI.getCurrent(), receiver);
    }

    /**
     * Retrieves extended client details for a specified {@link UI} instance and passes them to the provided
     * {@link ExtendedClientDetailsReceiver}.
     *
     * @param ui       the {@link UI} instance for which the extended client details are retrieved
     * @param receiver the {@link ExtendedClientDetailsReceiver} that will handle the retrieved
     *                 extended client details
     */
    public void retrieveExtendedClientDetails(UI ui, ExtendedClientDetailsReceiver receiver) {
        ui.getPage().retrieveExtendedClientDetails(receiver);
    }
}
