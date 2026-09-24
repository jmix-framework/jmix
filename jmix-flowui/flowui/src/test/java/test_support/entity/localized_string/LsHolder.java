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
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.List;
import java.util.UUID;

/**
 * Holds a reference to an entity with a localized string, but declares no localized property of its own.
 */
@JmixEntity(name = "test_LsHolder")
public class LsHolder {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    private LsItem item;

    private List<LsItem> items;

    private LsNumbered numbered;

    private LsLink link;

    private LsNode node;

    private String code;

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

    public List<LsItem> getItems() {
        return items;
    }

    public void setItems(List<LsItem> items) {
        this.items = items;
    }

    public LsNumbered getNumbered() {
        return numbered;
    }

    public void setNumbered(LsNumbered numbered) {
        this.numbered = numbered;
    }

    public LsLink getLink() {
        return link;
    }

    public void setLink(LsLink link) {
        this.link = link;
    }

    public LsNode getNode() {
        return node;
    }

    public void setNode(LsNode node) {
        this.node = node;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
