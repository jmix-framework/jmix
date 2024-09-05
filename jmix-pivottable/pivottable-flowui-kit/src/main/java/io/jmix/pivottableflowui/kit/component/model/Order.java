/*
 * Copyright 2024 Haulmont.
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

package io.jmix.pivottableflowui.kit.component.model;

import javax.annotation.Nullable;

/**
 * An enum with the orders in which column and row data is provided to the renderer.
 * <p>
 * Ordering by value orders by column total.
 */
public enum Order implements SerializedEnum {

    KEYS_ASCENDING("key_a_to_z"),
    VALUES_ASCENDING("value_a_to_z"),
    VALUES_DESCENDING("value_z_to_a");

    private final String id;

    Order(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static Order fromId(String id) {
        for (Order order : values()) {
            if (order.getId().equals(id)) {
                return order;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return id;
    }
}
