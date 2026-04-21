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
import com.vaadin.flow.function.SerializableConsumer;
import io.jmix.core.annotation.Internal;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_ExtendedClientDetailsProvider")
public class ExtendedClientDetailsProvider {

    public ExtendedClientDetails getExtendedClientDetails() {
        return getExtendedClientDetails(UI.getCurrent());
    }

    public ExtendedClientDetails getExtendedClientDetails(UI ui) {
        return ui.getPage().getExtendedClientDetails();
    }

    /**
     * Retrieves extended client details for the current {@link UI} and passes
     * the details to the specified receiver.
     *
     * @param receiver the {@link ExtendedClientDetailsReceiver} that will handle
     *                 the received extended client details
     * @deprecated Use {@link #getExtendedClientDetails()} to get the cached details,
     * or {@link ExtendedClientDetails#refresh(SerializableConsumer)} to refresh the cached values.
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public void retrieveExtendedClientDetails(ExtendedClientDetailsReceiver receiver) {
        retrieveExtendedClientDetails(UI.getCurrent(), receiver);
    }

    /**
     * Retrieves extended client details for the specified {@link UI} and passes
     * the details to the specified receiver.
     *
     * @param ui       the {@link UI} for which to retrieve extended client details
     * @param receiver the {@link ExtendedClientDetailsReceiver} that will handle
     *                 the received extended client details
     * @deprecated Use {@link #getExtendedClientDetails()} to get the cached details,
     * or {@link ExtendedClientDetails#refresh(SerializableConsumer)} to refresh the cached values.
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public void retrieveExtendedClientDetails(UI ui, ExtendedClientDetailsReceiver receiver) {
        ui.getPage().retrieveExtendedClientDetails(receiver);
    }
}
