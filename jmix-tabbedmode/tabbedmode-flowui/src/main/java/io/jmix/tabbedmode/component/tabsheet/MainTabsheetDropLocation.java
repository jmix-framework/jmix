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

import java.util.Objects;

public enum MainTabsheetDropLocation {

    LEFT("left"),

    RIGHT("right"),

    EMPTY("empty");

    private final String clientName;

    MainTabsheetDropLocation(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Gets name that is used in the client side representation of the
     * component.
     *
     * @return the name used in the client side representation of the component.
     */
    public String getClientName() {
        return clientName;
    }

    @Nullable
    public static MainTabsheetDropLocation fromClientName(String clientName) {
        for (MainTabsheetDropLocation location : MainTabsheetDropLocation.values()) {
            if (location.getClientName().equals(clientName)) {
                return location;
            }
        }

        return null;
    }
}
