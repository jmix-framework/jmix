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

import {FocusMixin} from '@vaadin/a11y-base/src/focus-mixin.js';

export const ItemMixin = (superclass) =>
    class GridItemMixin extends FocusMixin(superclass) {

        static get properties() {
            return {
                /** @private */
                _value: String
            };
        }

        get value() {
            return this._value !== undefined ? this._value : this.textContent.trim();
        }

        set value(value) {
            this._value = value;
        }

        ready() {
            super.ready();

            const attrValue = this.getAttribute('value');
            if (attrValue !== null) {
                this.value = attrValue;
            }
        }

        focus() {
            super.focus();
            this._setFocused(true);
        }
    };
