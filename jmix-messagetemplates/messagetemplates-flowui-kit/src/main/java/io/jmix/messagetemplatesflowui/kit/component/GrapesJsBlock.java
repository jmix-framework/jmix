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

package io.jmix.messagetemplatesflowui.kit.component;

import java.io.Serializable;

public class GrapesJsBlock implements Serializable {

    protected final String name;

    protected String label;

    protected String category;

    protected String content;

    protected String attributes;

    public GrapesJsBlock(String name) {
        this.name = name;
    }

    public static Builder create(String name) {
        return new Builder(name);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public static class Builder {

        protected final GrapesJsBlock block;

        public Builder(String name) {
            this.block = new GrapesJsBlock(name);
        }

        public String getName() {
            return block.getName();
        }

        public String getLabel() {
            return block.getLabel();
        }

        public Builder withLabel(String label) {
            block.setLabel(label);
            return this;
        }

        public String getCategory() {
            return block.getCategory();
        }

        public Builder withCategory(String category) {
            block.setCategory(category);
            return this;
        }

        public String getContent() {
            return block.getContent();
        }

        public Builder withContent(String content) {
            block.setContent(content);
            return this;
        }

        public String getAttributes() {
            return block.getAttributes();
        }

        public Builder withAttributes(String attributes) {
            block.setAttributes(attributes);
            return this;
        }

        public GrapesJsBlock build() {
            return block;
        }
    }
}
