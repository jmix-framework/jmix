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
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {DirMixin} from '@vaadin/component-base/src/dir-mixin.js';
import {LumoInjectionMixin} from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import {gridItemStyles} from './styles/jmix-grid-layout-item-base-styles.js';
import {ItemMixin} from './jmix-grid-layout-item-mixin.js';

/**
 * JmixGridLayoutItem class is a web component that represents a {@link JmixGridLayout} item.
 * It is used as a container for content, where the content is placed inside the slot element.
 */
export class JmixGridLayoutItem extends ItemMixin(ThemableMixin(DirMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    render() {
        return html`
            <div part="content">
                <slot></slot>
            </div>
        `;
    }

    static get is() {
        return 'jmix-grid-layout-item';
    }

    static get styles() {
        return [gridItemStyles];
    }
}

defineCustomElement(JmixGridLayoutItem);
