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

class JmixViewContainer extends ElementMixin(PolylitMixin(LitElement)) {

    static get is() {
        return 'jmix-view-container';
    }

    static get styles() {
        return css`
            :host {
                display: flex;
                flex-direction: column;
                box-sizing: border-box;
            }

            [part='breadcrumbs'] {
                flex-shrink: 0;
            }

            [part='breadcrumbs'] ::slotted(*:not([hidded])) {
                padding: var(--lumo-space-m) var(--lumo-space-m) 0;
            }

            [part='content'] {
                flex-grow: 1;
                overflow: auto;
            }
        `
    }

    render() {
        return html`
            <div part="breadcrumbs">
                <slot name="breadcrumbs"></slot>
            </div>
            <div part="content">
                <slot></slot>
            </div>
        `
    }
}

defineCustomElement(JmixViewContainer);

export {JmixViewContainer};