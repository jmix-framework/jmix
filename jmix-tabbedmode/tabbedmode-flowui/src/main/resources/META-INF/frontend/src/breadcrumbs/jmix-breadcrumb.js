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

import {css, html, LitElement} from 'lit';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {ActiveMixin} from '@vaadin/a11y-base/src/active-mixin.js';
import {FocusMixin} from '@vaadin/a11y-base/src/focus-mixin.js';
import {TabindexMixin} from '@vaadin/a11y-base/src/tabindex-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {TextController} from './text-controller.js';

class JmixBreadcrumb extends ActiveMixin(TabindexMixin(FocusMixin(ThemableMixin(ElementMixin(PolylitMixin(LitElement)))))) {
    static get is() {
        return 'jmix-breadcrumb';
    }

    static get styles() {
        return css`
            :host {
                display: inline-block;
                box-sizing: border-box;
                outline: none;
                white-space: nowrap;
                user-select: none;

                font-family: var(--lumo-font-family);
                border-radius: var(--lumo-border-radius-m);

                --_focus-ring-color: var(--vaadin-focus-ring-color, var(--lumo-primary-color-50pct));
                --_focus-ring-width: var(--vaadin-focus-ring-width, 2px);
            }

            :host([hidden]) {
                display: none !important;
            }

            :host(:last-of-type) {
                pointer-events: none;
            }

            [part='text'] {
                display: inline-flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;
                text-align: center;
                width: 100%;
                height: 100%;
                min-height: inherit;
                text-shadow: inherit;

                gap: var(--lumo-space-s);
            }

            [part='text'] ::slotted([slot='text']) {
                color: var(--lumo-primary-text-color);
                font-weight: 500;
                border-radius: var(--lumo-border-radius-m);
                cursor: var(--lumo-clickable-cursor);
            }

            :host(:last-of-type) [part='text'] ::slotted([slot='text']) {
                color: inherit;
                font-weight: inherit;
            }

            [part='separator']::after {
                content: '>';
                speak: none;
            }

            :host(:last-of-type) [part='separator'] {
                display: none;
            }

            /* Keyboard focus */

            :host([focus-ring]) [part='text'] ::slotted([slot='text']) {
                box-shadow: 0 0 0 var(--_focus-ring-width) var(--_focus-ring-color);
            }

            /* Disabled state */

            :host([disabled]) {
                pointer-events: none;
                color: var(--lumo-disabled-text-color);
            }

            :host([disabled]) [part='text'] ::slotted([slot='text']) {
                color: var(--lumo-disabled-text-color);
            }

            /* RTL specific styles */


            /*  */

            @media (forced-colors: active) {
                :host {
                    outline: 1px solid;
                    outline-offset: -1px;
                }

                :host([focused]) {
                    outline-width: 2px;
                }

                :host([disabled]) {
                    outline-color: GrayText;
                }
            }
        `
    }

    render() {
        return html`
            <div part="text">
                <slot name="text"></slot>
                <span part="separator"></span>
            </div>
        `
    }

    static get properties() {
        return {
            text: {
                type: String,
                value: '',
                observer: '_textChanged',
            },

            /**
             * Indicates whether the element can be focused and where it participates in sequential keyboard navigation.
             *
             * @override
             * @protected
             */
            tabindex: {
                type: Number,
                value: 0,
                reflectToAttribute: true,
            },
        }
    }

    constructor() {
        super();

        this._textController = new TextController(this);
    }

    /** @protected */
    ready() {
        super.ready();

        this.addController(this._textController);
    }

    /**
     * @param {string} text
     * @protected
     */
    _textChanged(text) {
        this._textController.setText(text);
    }

    /**
     * Overrides the default element `click` method in order to prevent
     * firing the `click` event when the element is the last step.
     * @protected
     * @override
     */
    click() {
        if (this.nextElementSibling) {
            super.click();
        }
    }

    /**
     * Since this component doesn't have a native <button> inside, in order to be fully
     * accessible from the keyboard, it should manually fire the `click` event once an
     * activation key is pressed, as it follows from the WAI-ARIA specifications:
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

        if (event.altKey || event.shiftKey || event.ctrlKey || event.metaKey) {
            return;
        }

        if (this._activeKeys.includes(event.key)) {
            event.preventDefault();

            // `DisabledMixin` overrides the standard `click()` method
            // so that it doesn't fire the `click` event when the element is disabled.
            this.click();
        }
    }
}

defineCustomElement(JmixBreadcrumb);

export {JmixBreadcrumb};