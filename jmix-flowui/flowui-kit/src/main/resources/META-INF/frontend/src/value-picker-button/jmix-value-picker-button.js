/*
 * Copyright 2022 Haulmont.
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

import {html, PolymerElement} from "@polymer/polymer";
import {TabindexMixin} from "@vaadin/component-base/src/tabindex-mixin";
import {FocusMixin} from "@vaadin/component-base/src/focus-mixin";
import {ElementMixin} from "@vaadin/component-base/src/element-mixin";
import {ThemableMixin} from "@vaadin/vaadin-themable-mixin";
import {ActiveMixin} from "@vaadin/component-base/src/active-mixin";
import { registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles('jmix-value-picker-button', [],{
    moduleId: 'jmix-value-picker-button-styles'
});

export class JmixValuePickerButton extends ActiveMixin(TabindexMixin(FocusMixin(ElementMixin(ThemableMixin(PolymerElement))))) {

    static get is() {
        return 'jmix-value-picker-button';
    }

    static get template() {
        return html`
            <style>
                :host {
                    display: inline-block;
                    position: relative;
                    outline: none;
                    white-space: nowrap;
                    -webkit-user-select: none;
                    -moz-user-select: none;
                    user-select: none;
                }

                :host([hidden]) {
                    display: none !important;
                }
                
                /* Aligns the button with form fields when placed on the same line.
                Note, to make it work, the form fields should have the same "::before" pseudo-element. */
                .value-picker-button-container::before {
                    content: '\\2003';
                    display: inline-block;
                    width: 0;
                }
                
                .value-picker-button-container {
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    text-align: center;
                    width: 100%;
                    height: 100%;
                    min-height: inherit;
                    text-shadow: inherit;
                    background: transparent;
                    padding: 0;
                    border: none;
                    box-shadow: none;
                }

                [part='icon'] {
                    flex: none;
                }
            </style>

            <div class="value-picker-button-container">
                <span part="icon">
                    <slot name="icon"></slot>
                </span>
            </div>
        `;
    }

    static get properties() {
        return {
            /**
             * Indicates whether the element can be focused and where it participates in sequential keyboard navigation.
             *
             * @override
             * @protected
             */
            tabindex: {
                value: 0
            }
        };
    }

    /** @protected */
    ready() {
        super.ready();

        // By default, if the user hasn't provided a custom role,
        // the role attribute is set to "button".
        if (!this.hasAttribute('role')) {
            this.setAttribute('role', 'button');
        }
    }

    /**
     * By default, `Space` is the only possible activation key for a focusable HTML element.
     * Nonetheless, the button is an exception as it can be also activated by pressing `Enter`.
     * See the "Keyboard Support" section in https://www.w3.org/TR/wai-aria-practices/examples/button/button.html.
     *
     * @protected
     * @override
     */
    get _activeKeys() {
        return ['Enter', ' '];
    }

    /**
     * Since the button component is designed on the base of the `[role=button]` attribute,
     * and doesn't have a native <button> inside, in order to be fully accessible from the keyboard,
     * it should manually fire the `click` event once an activation key is pressed,
     * as it follows from the WAI-ARIA specifications:
     * https://www.w3.org/TR/wai-aria-practices-1.1/#button
     *
     * According to the UI Events specifications,
     * the `click` event should be fired exactly on `keydown`:
     * https://www.w3.org/TR/uievents/#event-type-keydown
     *
     * @param {KeyboardEvent} event
     * @protected
     * @override
     */
    _onKeyDown(event) {
        super._onKeyDown(event);

        if (this._activeKeys.includes(event.key)) {
            event.preventDefault();

            // `DisabledMixin` overrides the standard `click()` method
            // so that it doesn't fire the `click` event when the element is disabled.
            this.click();
        }
    }
}

customElements.define(JmixValuePickerButton.is, JmixValuePickerButton);