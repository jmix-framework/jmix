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

import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import {ControllerMixin} from '@vaadin/component-base/src/controller-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin.js';
import {TooltipController} from '@vaadin/component-base/src/tooltip-controller.js';
import {registerStyles, ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {TabMixin} from '@vaadin/tabs/src/vaadin-tab-mixin.js';
import {tabStyles} from '@vaadin/tabs/src/vaadin-tab-styles.js';

registerStyles('jmix-view-tab', tabStyles, {moduleId: 'jmix-view-tab-styles'});

/**
 * `<jmix-view-tab>` is a Web Component providing an accessible and customizable tab.
 *
 * ```
 *   <jmix-view-tab>
 *     Tab 1
 *   </jmix-view-tab>
 * ```
 *
 * The following state attributes are available for styling:
 *
 * Attribute  | Description | Part name
 * -----------|-------------|------------
 * `disabled` | Set to a disabled tab | :host
 * `focused` | Set when the element is focused | :host
 * `focus-ring` | Set when the element is keyboard focused | :host
 * `selected` | Set when the tab is selected | :host
 * `active` | Set when mousedown or enter/spacebar pressed | :host
 * `orientation` | Set to `horizontal` or `vertical` depending on the direction of items  | :host
 *
 * See [Styling Components](https://vaadin.com/docs/latest/styling/styling-components) documentation.
 *
 * @customElement
 * @extends HTMLElement
 * @mixes ControllerMixin
 * @mixes ElementMixin
 * @mixes ThemableMixin
 * @mixes TabMixin
 */
// CAUTION: copied from @vaadin/tabs/src/vaadin-tab.js [last update Vaadin 24.6.3]
class JmixViewTab extends ElementMixin(ThemableMixin(TabMixin(ControllerMixin(PolymerElement)))) {
    static get template() {
        return html`
            <slot name="prefix"></slot>
            <slot></slot>
            <slot name="suffix"></slot>
            <slot name="tooltip"></slot>
        `;
    }

    static get is() {
        return 'jmix-view-tab';
    }

    /** @protected */
    ready() {
        super.ready();

        this._tooltipController = new TooltipController(this);
        this.addController(this._tooltipController);
    }
}

defineCustomElement(JmixViewTab);

export {JmixViewTab};