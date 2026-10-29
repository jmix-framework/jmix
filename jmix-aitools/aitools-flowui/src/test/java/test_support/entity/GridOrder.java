/*
 * Copyright 2026 Haulmont.
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

package test_support.entity;

import io.jmix.aitools.ExcludeFromAi;
import io.jmix.core.annotation.Secret;
import io.jmix.core.metamodel.annotation.JmixEntity;

/**
 * DTO fixture of a grid row entity: plain attributes, {@code @ExcludeFromAi} and {@code @Secret} attributes, a
 * reference to a regular entity and a reference to an excluded one.
 */
@JmixEntity(name = "aitlsui_GridOrder")
public class GridOrder {

    private String number;

    @ExcludeFromAi
    private String internalNote;

    @Secret
    private String apiKey;

    private GridCustomer customer;

    private GridHiddenEntity hidden;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getInternalNote() {
        return internalNote;
    }

    public void setInternalNote(String internalNote) {
        this.internalNote = internalNote;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public GridCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(GridCustomer customer) {
        this.customer = customer;
    }

    public GridHiddenEntity getHidden() {
        return hidden;
    }

    public void setHidden(GridHiddenEntity hidden) {
        this.hidden = hidden;
    }
}
