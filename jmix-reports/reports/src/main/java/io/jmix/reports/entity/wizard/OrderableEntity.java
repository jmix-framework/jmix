/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.entity.wizard;

/**
 * Interface to be implemented by some entities that order display can be changed in UI.
 * That order might to be set by com.haulmont.reports.gui.components.actions.OrderableItemMoveAction
 */
public interface OrderableEntity {
    Long getOrderNum();

    void setOrderNum(Long orderNum);
}