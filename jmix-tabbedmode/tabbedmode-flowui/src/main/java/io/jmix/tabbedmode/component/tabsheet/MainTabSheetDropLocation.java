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

package io.jmix.tabbedmode.component.tabsheet;

import org.springframework.lang.Nullable;

/**
 * Defines drop locations within {@link MainTabSheet}.
 */
public enum MainTabSheetDropLocation {

    /**
     * Drop before the target tab.
     */
    BEFORE("before"),

    /**
     * Drop after the target tab.
     */
    AFTER("after"),

    /**
     * Dropping into an empty space so that a target tab cannot be determined.
     */
    EMPTY("empty");

    private final String clientName;

    MainTabSheetDropLocation(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Gets a name used in the client side representation of the component.
     *
     * @return the name used in the client side representation of the component.
     */
    public String getClientName() {
        return clientName;
    }

    @Nullable
    public static MainTabSheetDropLocation fromClientName(String clientName) {
        for (MainTabSheetDropLocation location : MainTabSheetDropLocation.values()) {
            if (location.getClientName().equals(clientName)) {
                return location;
            }
        }

        return null;
    }
}
