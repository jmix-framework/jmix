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

package test_support.entity.localized_string;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.UUID;

/**
 * Carries an instance name that is a reference to an item, whose own instance name is a localized string, so that
 * a reference to it is sorted by a localized text one reference deeper.
 */
@JmixEntity(name = "test_LsLink")
public class LsLink {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    @InstanceName
    private LsItem item;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LsItem getItem() {
        return item;
    }

    public void setItem(LsItem item) {
        this.item = item;
    }
}
