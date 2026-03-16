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

import {html, LitElement} from 'lit';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {gridLayoutStyles} from './styles/jmix-grid-layout-base-styles.js';

/**
 * `JmixGridLayout` is a layout web component that arranges its child elements in a responsive grid.
 *
 * It provides properties to set the minimum width of the columns and the grid gap between elements.
 *
 * Slots:
 * - Default slot for adding child elements, typically {@link JmixGridLayoutItem} elements.
 *
 * Custom CSS properties:
 * - `--_grid-layout-column-min-width`: Custom property to control the column minimum width.
 * - `--_grid-layout-grid-gap`: Custom property to control the spacing between items.
 *
 * Properties:
 * - `disabled` (Boolean): Determines whether the layout is disabled.
 * - `columnMinWidth` (String): Specifies the minimum width of the columns. Defaults to `'19rem'`.
 * - `gridGap` (String): Specifies the gap between grid items. Defaults to `'var(--lumo-space-s)'`.
 *
 */
export class JmixGridLayout extends ElementMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement)))) {

    render() {
        return html`
            <div part="items">
                <slot></slot>
            </div>
        `;
    }

    static get is() {
        return 'jmix-grid-layout';
    }

    static get styles() {
        return [gridLayoutStyles];
    }

    static get properties() {
        return {
            disabled: {
                type: Boolean,
                value: false,
                reflectToAttribute: true
            },
            columnMinWidth: {
                type: String,
                value: '19rem',
                observer: '_columnMinWidthChanged'
            },
            gridGap: {
                type: String,
                value: '0.5rem',
                observer: '_gridGapChanged'
            }
        };
    }

    ready() {
        super.ready();

        setTimeout(this._checkImport.bind(this), 2000);
    }

    /** @private */
    _checkImport() {
        const item = this.querySelector('jmix-grid-layout-item');
        if (item && !(item instanceof PolymerElement)) {
            console.warn(`Make sure you have imported the jmix-grid-layout-item element.`);
        }
    }

    /**
     * @param value new value
     * @private
     */
    _columnMinWidthChanged(value) {
        this.style.setProperty('--_grid-layout-column-min-width', value);
    }

    /**
     * @param value new value
     * @private
     */
    _gridGapChanged(value) {
        this.style.setProperty('--_grid-layout-grid-gap', value);
    }
}

defineCustomElement(JmixGridLayout);
