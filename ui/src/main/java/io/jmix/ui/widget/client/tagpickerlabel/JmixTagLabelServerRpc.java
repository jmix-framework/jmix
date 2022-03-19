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

package io.jmix.ui.widget.client.tagpickerlabel;

import com.vaadin.shared.communication.ServerRpc;

/**
 * Server RPC for tag label component.
 */
public interface JmixTagLabelServerRpc extends ServerRpc {

    /**
     * User clicks on close element.
     */
    void removeItem();

    /**
     * User clicks on tag label.
     */
    void itemClick();
}
