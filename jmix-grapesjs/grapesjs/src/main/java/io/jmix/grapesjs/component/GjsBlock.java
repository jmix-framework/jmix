/*
 * Copyright 2020 Haulmont.
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

package io.jmix.grapesjs.component;

import java.io.Serializable;

/**
 * GrapesJS block
 */
public class GjsBlock implements Serializable, Cloneable {

    /**
     * Block id
     */
    private String name;

    /**
     * Name of the block
     */
    private String label;

    /**
     * Group the block inside a catgegory.
     */
    private String category;

    /**
     * HTML content
     */
    private String content;

    /**
     * Block attributes (optional, default {})
     */
    private String attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    @Override
    public GjsBlock clone() {
        try {
            GjsBlock clone = (GjsBlock) super.clone();
            clone.setName(this.name);
            clone.setAttributes(this.attributes);
            clone.setCategory(this.category);
            clone.setLabel(this.label);
            clone.setContent(this.content);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone not supported", e);
        }
    }


}
