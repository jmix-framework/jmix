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

package test_support.entity.lookup_field;

import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.entity.annotation.LookupField;
import io.jmix.core.entity.annotation.LookupItemsQuery;
import io.jmix.core.entity.annotation.LookupType;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.UUID;

/**
 * A non-persistent entity with no assigned data store (the noop store), used to verify that a
 * DROPDOWN degrades to a view lookup regardless of {@code itemsQuery} configuration.
 */
@LookupField(type = LookupType.DROPDOWN, itemsQuery = @LookupItemsQuery(byInstanceName = true))
@JmixEntity(name = "test_LfNote")
public class LfNote {

    @JmixId
    private UUID id;

    @InstanceName
    private String text;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
