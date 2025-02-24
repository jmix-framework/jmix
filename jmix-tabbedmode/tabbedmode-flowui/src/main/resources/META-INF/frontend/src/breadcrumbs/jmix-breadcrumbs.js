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

class JmixBreadcrumbs extends ElementMixin(PolylitMixin(LitElement)) {

    static get is() {
        return 'jmix-breadcrumbs';
    }

    static get styles() {
        return css`
            :host {
                display: flex;
                flex-wrap: wrap;
                gap: var(--lumo-space-s);
                box-sizing: border-box;
            }
            
            :host([hidden]) {
                display: none !important;
            }
        `
    }

    render() {
        return html`
            <slot></slot>
        `
    }
}

defineCustomElement(JmixBreadcrumbs);

export {JmixBreadcrumbs};