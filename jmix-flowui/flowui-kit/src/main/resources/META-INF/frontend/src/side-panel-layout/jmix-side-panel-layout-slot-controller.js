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

import { SlotController } from '@vaadin/component-base/src/slot-controller.js';

export class JmixDrawerLayoutSlotController extends SlotController {

    constructor(host, slotName) {
        super(host, slotName, 'div', { multiple: true });

        // Contains nodes that belong to a slot. These nodes can be detached from the
        // slot and moved to the overlay when fullscreen is enabled and device with
        // small screen is detected.
        this.actualNodes = [];
    }

    initCustomNode(node) {
        super.initCustomNode(node);

        if (!this.containsActualNode(node)) {
            this.actualNodes.push(node);
        }

        if (this.host.drawerOpened) {
          this.host._moveDrawerChildren();
        }
    }

    teardownNode(node) {
        super.teardownNode(node);

        if (!this.suspendRemovingNodes) {
            if (this.containsActualNode(node)) {
                this.actualNodes.splice(this.actualNodes.indexOf(node), 1);
            }

            if (node.parentElement) {
                node.parentElement.removeChild(node);
            }
        }
    }

    getActualNodes() {
      return this.actualNodes;
    }

    containsActualNode(node) {
      return this.actualNodes.indexOf(node) !== -1;
    }

    removeActualNode(node) {
        this.actualNodes.splice(this.actualNodes.indexOf(node), 1);
    }

    suspendRemovingActualNodes() {
      this.suspendRemovingNodes = true;
    }

    resumeRemovingActualNodes() {
      this.suspendRemovingNodes = false;
    }
}
