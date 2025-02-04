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

import {SlotChildObserveController} from '@vaadin/component-base/src/slot-child-observe-controller.js';

export class TextController extends SlotChildObserveController {

    constructor(host) {
        // Do not provide tag name, as we create text element lazily.
        super(host, 'text', 'span');
    }


    /**
     * Set text based on corresponding host property.
     *
     * @param {string} text
     */
    setText(text) {
        this.text = text;

        // Restore the default label, if needed.
        const textNode = this.getSlotChild();
        if (!textNode) {
            this.restoreDefaultNode();
        }

        // When default text is used, update it.
        if (this.node === this.defaultNode) {
            this.updateDefaultNode(this.node);
        }
    }

    /**
     * Override method inherited from `SlotChildObserveController`
     * to restore and observe the default text element.
     *
     * @protected
     * @override
     */
    restoreDefaultNode() {
        const {text} = this;

        // Restore the default text.
        if (text && text.trim() !== '') {
            const textNode = this.attachDefaultNode();

            // Observe the default label.
            this.observeNode(textNode);
        }
    }

    /**
     * Override method inherited from `SlotChildObserveController`
     * to update the default label element text content.
     *
     * @param {Node | undefined} node
     * @protected
     * @override
     */
    updateDefaultNode(node) {
        const {text} = this;
        const hasText = Boolean(text && text.trim() !== '');

        if (node) {
            node.textContent = hasText ? text : '';
            node.hidden = !hasText;
        }

        // Notify the host after update.
        super.updateDefaultNode(node);
    }

    /**
     * Override to observe the newly added custom node.
     *
     * @param {Node} node
     * @protected
     * @override
     */
    initCustomNode(node) {
        // Notify the host about adding a custom node.
        super.initCustomNode(node);

        this.observeNode(node);
    }
}