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

/**
 * A block is an object that allows the end user to reuse components. By default, added blocks are available
 * in the blocks section for adding to the message template using the drag-n-drop action.
 */
public class GrapesJsBlock implements Serializable {

    protected final String id;

    protected String label;

    protected String category;

    protected String content;

    protected String attributes;

    public GrapesJsBlock(String id) {
        this.id = id;
    }

    /**
     * Creates {@link Builder} that can be used to create blocks via the Fluent API.
     *
     * @param id unique ID of the block
     * @return {@link Builder} for subsequent use via the Fluent API
     */
    public static Builder create(String id) {
        return new Builder(id);
    }

    /**
     * @return unique ID of the block
     */
    public String getId() {
        return id;
    }

    /**
     * @return label of the block
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label of the block. The label is used to name a block in the blocks section.
     *
     * @param label label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return category of the block in the blocks section
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category to which the block will be located in the blocks section.
     *
     * @param category category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return HTML content of the block
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the HTML content for the block. The content will be rendered in the {@link JmixGrapesJs} template
     * when you drag-n-drop a block into the template.
     *
     * @param content HTML content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return additional attributes of the block
     */
    public String getAttributes() {
        return attributes;
    }

    /**
     * Sets additional attributes of the block as a JSON string. Additional attributes can be used to
     * set additional parameters.
     * <br/>
     * For example, you can set text that will be displayed as a tooltip when hovering over a block:
     * <pre>{@code
     * grapesJsBlock.setAttributes("""
     *                     {
     *                         "title": "Insert another one"
     *                     }
     *         """);
     * }</pre>
     *
     * @param attributes JSON string attributes to set
     */
    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    /**
     * Builder for {@link GrapesJsBlock} that can be used to create blocks via the Fluent API.
     */
    public static class Builder {

        protected final GrapesJsBlock block;

        public Builder(String id) {
            this.block = new GrapesJsBlock(id);
        }

        /**
         * @return unique id of the block
         */
        public String getId() {
            return block.getId();
        }

        /**
         * @return label of the block
         */
        public String getLabel() {
            return block.getLabel();
        }

        /**
         * Sets the label of the block. The label is used to name a block in the blocks section.
         *
         * @param label label to set
         * @return this
         */
        public Builder withLabel(String label) {
            block.setLabel(label);
            return this;
        }

        /**
         * @return category of the block in the blocks section
         */
        public String getCategory() {
            return block.getCategory();
        }

        /**
         * Sets the category to which the block will be located in the blocks section.
         *
         * @param category category to set
         * @return this
         */
        public Builder withCategory(String category) {
            block.setCategory(category);
            return this;
        }

        /**
         * @return HTML content of the block
         */
        public String getContent() {
            return block.getContent();
        }

        /**
         * Sets the HTML content for the block. The content will be rendered in the {@link JmixGrapesJs} template
         * when you drag-n-drop a block into the template.
         *
         * @param content HTML content to set
         * @return this
         */
        public Builder withContent(String content) {
            block.setContent(content);
            return this;
        }

        /**
         * @return additional attributes of the block
         */
        public String getAttributes() {
            return block.getAttributes();
        }

        /**
         * Sets additional attributes of the block as a JSON string. Additional attributes can be used to
         * set additional parameters.
         * <br/>
         * For example, you can set text that will be displayed as a tooltip when hovering over a block:
         * <pre>{@code
         * grapesJsBlock.setAttributes("""
         *                     {
         *                         "title": "Insert another one"
         *                     }
         *         """);
         * }</pre>
         *
         * @param attributes JSON string attributes to set
         * @return this
         */
        public Builder withAttributes(String attributes) {
            block.setAttributes(attributes);
            return this;
        }

        /**
         * @return an instance of the {@link GrapesJsBlock} that was created using the Fluent API
         */
        public GrapesJsBlock build() {
            return block;
        }
    }
}
