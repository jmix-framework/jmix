/*
 * Copyright 2026 Haulmont.
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

import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { buttonStyles } from '@vaadin/button/src/vaadin-button-core-styles.js';
import { ButtonMixin } from '@vaadin/button/src/vaadin-button-mixin.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DirMixin } from '@vaadin/component-base/src/dir-mixin.js';
import { isEmptyTextNode } from '@vaadin/component-base/src/dom-utils.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { jmixSidePanelCloserLayout } from './jmix-side-panel-layout-closer-styles.js';

registerStyles('jmix-side-panel-layout-closer', [buttonStyles, jmixSidePanelCloserLayout], { moduleId: 'jmix-side-panel-layout-closer-styles' });

class JmixSidePanelLayoutCloser extends ButtonMixin(DirMixin(ThemableMixin(PolymerElement))) {

    static get is() {
        return 'jmix-side-panel-layout-closer';
    }

    static get template() {
        return html`
            <slot id="slot">
                <div part="icon"></div>
            </slot>
        `;
    }

    static get properties() {
       return {
           ariaLabel: {
               type: String,
               reflectToAttribute: true,
           },
       };
    }
}

defineCustomElement(JmixSidePanelLayoutCloser);

export { JmixSidePanelLayoutCloser };
