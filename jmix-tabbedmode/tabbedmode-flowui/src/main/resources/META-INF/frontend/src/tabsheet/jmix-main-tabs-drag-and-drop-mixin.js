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
const DropLocation = {
    LEFT: 'left',
    RIGHT: 'right',
    EMPTY: 'empty',
};

export const DragAndDropMixin = (superClass) =>
    class DragAndDropMixin extends superClass {
        static get properties() {
            return {
                /**
                 * Marks the tabs' tabs to be available for dragging.
                 * @attr {boolean} tabs-draggable
                 */
                tabsDraggable: {
                    type: Boolean,
                    sync: true,
                },
            };
        }

        static get observers() {
            return ['__tabsDraggableChanged(tabsDraggable)'];
        }

        /** @protected */
        ready() {
            super.ready();

            this.__tabs.addEventListener('dragover', this._onDragOver.bind(this));
            this.__tabs.addEventListener('dragleave', this._onDragLeave.bind(this));
            this.__tabs.addEventListener('drop', this._onDrop.bind(this));
        }

        /** @private */
        _onDragOver(e) {
            if (!this.tabsDraggable) {
                return;
            }

            this._dropLocation = DropLocation.EMPTY;
            this._dragOverTab = undefined;

            let tab = e.composedPath()
                .find((node) => node.localName === 'jmix-view-tab');
            if (tab) {
                // The dragover occurred on a tab, determine the drop location from coordinates
                const tabRect = tab.getBoundingClientRect();
                const dropLeft = e.clientX - tabRect.left < tabRect.right - e.clientX;
                this._dropLocation = dropLeft ? DropLocation.LEFT : DropLocation.RIGHT;
            }

            e.stopPropagation();
            e.preventDefault();

            if (this._dropLocation === DropLocation.EMPTY) {
                this.__tabs.toggleAttribute('dragover', true);
            } else if (tab) {
                this._dragOverTab = tab.id;
                if (tab.getAttribute('dragover') !== this._dropLocation) {
                    this.updateState(tab, 'dragover', this._dropLocation);
                }
            } else {
                this._clearDragStyles();
            }
        }

        /** @private */
        _onDragLeave(e) {
            if (!this.tabsDraggable) {
                return;
            }

            e.stopPropagation();
            this._clearDragStyles();
        }

        /** @private */
        _onDrop(e) {
            if (!this.tabsDraggable) {
                return;
            }

            e.stopPropagation();
            e.preventDefault();

            this._clearDragStyles();

            const event = new CustomEvent('main-tabsheet-drop', {
                bubbles: e.bubbles,
                cancelable: e.cancelable,
                detail: {
                    dropTargetTab: this._dragOverTab,
                    dropLocation: this._dropLocation
                },
            });
            event.originalEvent = e;

            this.dispatchEvent(event);
        }

        _clearDragStyles() {
            this.__tabs.removeAttribute('dragover');
            this.iterateChildren(this.__tabs, (tab) => {
                this.updateState(tab, 'dragover', null);
            });
        }

        /**
         * @param {!HTMLElement} element
         * @param {string} attribute
         * @param {boolean | string | null | undefined} value
         */
        updateState(element, attribute, value) {
            switch (typeof value) {
                case 'boolean':
                    element.toggleAttribute(attribute, value);
                    break;
                case 'string':
                    element.setAttribute(attribute, value);
                    break;
                default:
                    // Value set to null / undefined
                    element.removeAttribute(attribute);
                    break;
            }
        }

        /**
         * @param {HTMLElement} container the DOM element with children
         * @param {Function} callback function to call on each child
         */
        iterateChildren(container, callback) {
            [...container.children].forEach(callback);
        }

        /** @private */
        __tabsDraggableChanged() {
            if (!this.tabsDraggable) {
                this._clearDragStyles();
            }
        }
    };