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

package io.jmix.ui.widget.client.verticalmenu;

import com.vaadin.shared.communication.ClientRpc;
import elemental.json.JsonArray;

import javax.annotation.Nullable;
import java.util.Map;

public interface JmixSideMenuClientRpc extends ClientRpc {
    /**
     * Build menu
     *
     * @param menuItems menu items tree
     */
    void buildMenu(JsonArray menuItems);

    /**
     * Select item
     *
     * @param itemId target item id
     */
    void selectItem(@Nullable String itemId);

    /**
     * Update badges of items
     *
     * @param badgeUpdates Map item id -&gt; new badge text
     */
    void updateBadge(Map<String, String> badgeUpdates);
}