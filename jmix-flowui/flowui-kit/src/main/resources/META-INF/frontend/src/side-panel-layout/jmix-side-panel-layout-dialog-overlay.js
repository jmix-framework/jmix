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

import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DirMixin } from '@vaadin/component-base/src/dir-mixin.js';
import { OverlayMixin } from '@vaadin/overlay/src/vaadin-overlay-mixin.js';
import { DialogOverlayMixin } from '@vaadin/dialog/src/vaadin-dialog-overlay-mixin.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

import { dialogOverlay, resizableOverlay } from '@vaadin/dialog/src/vaadin-dialog-styles.js';
import { overlayStyles } from '@vaadin/overlay/src/vaadin-overlay-styles.js';
import { drawerLayoutDialogOverlayStyles } from './jmix-side-panel-layout-dialog-overlay-styles.js';

registerStyles('jmix-drawer-layout-dialog-overlay',
    [overlayStyles, dialogOverlay, resizableOverlay, drawerLayoutDialogOverlayStyles],
    { moduleId: 'jmix-drawer-layout-dialog-overlay-styles', },
);

class JmixDrawerLayoutDialogOverlay extends OverlayMixin(DirMixin(ThemableMixin(PolymerElement))) {

    static get is() {
      return 'jmix-drawer-layout-dialog-overlay';
    }

    static get template() {
        return html`
            <div part="backdrop" id="backdrop" hidden$="[[!withBackdrop]]"></div>
            <div part="overlay" id="overlay" tabindex="0">
                <section id="resizerContainer" class="resizer-container">
                    <div part="content" id="content">
                        <slot name="drawerContentSlot"></slot>
                    </div>
                </section>
            </div>
        `;
    }

    ready() {
        super.ready();
    }
}

defineCustomElement(JmixDrawerLayoutDialogOverlay);

export { JmixDrawerLayoutDialogOverlay };