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

import '@vaadin/tabsheet/src/vaadin-tabsheet-scroller.js';
import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {ControllerMixin} from '@vaadin/component-base/src/controller-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {TabSheetMixin} from '@vaadin/tabsheet/src/vaadin-tabsheet-mixin.js';
import {DragAndDropMixin} from "./jmix-main-tabs-drag-and-drop-mixin.js";

/**
 * `<jmix-main-tabsheet>` is a Web Component for organizing and grouping content
 * into scrollable panels. The panels can be switched between by using tabs.
 *
 * ```
 *  <jmix-main-tabsheet>
 *    <div slot="prefix">Prefix</div>
 *    <div slot="suffix">Suffix</div>
 *
 *    <vaadin-tabs slot="tabs">
 *      <jmix-view-tab id="tab-1">View 1</jmix-view-tab>
 *      <jmix-view-tab id="tab-2">View 2</jmix-view-tab>
 *      <jmix-view-tab id="tab-3">View 3</jmix-view-tab>
 *    </vaadin-tabs>
 *
 *    <div tab="tab-1">Panel 1</div>
 *    <div tab="tab-2">Panel 2</div>
 *    <div tab="tab-3">Panel 3</div>
 *  </jmix-main-tabsheet>
 * ```
 *
 * ### Styling
 *
 * The following shadow DOM parts are exposed for styling:
 *
 * Part name | Description
 * --------- | ---------------
 * `tabs-container`    | The container for the slotted prefix, tabs and suffix
 * `content`    | The container for the slotted panels
 *
 * The following state attributes are available for styling:
 *
 * Attribute         | Description
 * ------------------|-------------
 * `loading` | Set when a tab without associated content is selected
 * `overflow`   | Set to `top`, `bottom`, `start`, `end`, all of them, or none.
 *
 * See [Styling Components](https://vaadin.com/docs/latest/styling/styling-components) documentation.
 *
 * @fires {CustomEvent} items-changed - Fired when the `items` property changes.
 * @fires {CustomEvent} selected-changed - Fired when the `selected` property changes.
 *
 * @customElement
 * @extends HTMLElement
 * @mixes TabSheetMixin
 * @mixes ElementMixin
 * @mixes ThemableMixin
 * @mixes ControllerMixin
 */
// CAUTION: copied from @vaadin/tabsheet [last update Vaadin 24.6.3]
class JmixMainTabSheet extends DragAndDropMixin(TabSheetMixin(ThemableMixin(ElementMixin(ControllerMixin(PolymerElement))))) {
    static get template() {
        return html`
            <style>
                :host([hidden]) {
                    display: none !important;
                }

                :host {
                    display: flex;
                    flex-direction: column;
                }

                [part='tabs-container'] {
                    position: relative;
                    display: flex;
                    align-items: center;
                }

                ::slotted([slot='tabs']) {
                    flex: 1;
                    align-self: stretch;
                    min-width: 8em;
                }

                [part='content'] {
                    position: relative;
                    flex: 1;
                    box-sizing: border-box;
                }
            </style>

            <div part="tabs-container">
                <slot name="prefix"></slot>
                <slot name="tabs"></slot>
                <slot name="suffix"></slot>
            </div>

            <vaadin-tabsheet-scroller part="content">
                <div part="loader"></div>
                <slot id="panel-slot"></slot>
            </vaadin-tabsheet-scroller>
        `;
    }

    static get is() {
        return 'jmix-main-tabsheet';
    }
}

defineCustomElement(JmixMainTabSheet);

export {JmixMainTabSheet};