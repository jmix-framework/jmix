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
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { ControllerMixin } from '@vaadin/component-base/src/controller-mixin.js';
import { MediaQueryController } from '@vaadin/component-base/src/media-query-controller.js';
import { JmixDrawerLayoutSlotController } from './jmix-side-panel-layout-slot-controller.js';

import { JmixDrawerLayoutMixin } from './jmix-side-panel-layout-mixin.js';
import { drawerLayoutStyles } from './jmix-side-panel-layout-styles.js';
import './jmix-side-panel-layout-dialog.js';

registerStyles('jmix-drawer-layout', drawerLayoutStyles, { moduleId: 'jmix-drawer-layout-styles' });

class JmixDrawerLayout extends JmixDrawerLayoutMixin(ElementMixin(ControllerMixin(ThemableMixin(PolymerElement)))) {

    static get template() {
        return html`
            <div id="layout" part="layout">
                <div id="content" part="content">
                    <slot name="contentSlot"></slot>
                </div>
                <div id="modalityCurtain" part="modalityCurtain" hidden$="[[_modalityCurtainHidden]]"></div>
                <div id="drawer" part="drawer" hidden$="[[!drawerOpened]]">
                    <div id="drawerContent" part="drawerContent">
                         <slot name="drawerContentSlot"></slot>
                    </div>
                </div>
            </div>

            <jmix-drawer-layout-dialog
                    id="dialog"
                    opened="[[_computeDialogOpened(drawerOpened, _displayAsOverlay)]]"
                    fullscreen="[[_displayAsOverlay]]"
                    aria-label="[[overlayAriaLabel]]"
                    theme$="[[_theme]]"
                  ></jmix-drawer-layout-dialog>
        `;
    }

    static get is() {
      return 'jmix-drawer-layout';
    }

    ready() {
        super.ready();

        this.addController(
            new MediaQueryController(this._displayAsOverlayMediaQuery, (matches) => {
                this._displayAsOverlay = this.displayAsOverlayOnSmallDevices ? matches : false;
                this.toggleAttribute('overlay', this._displayAsOverlay);
            }),
        );

        this._contentController = new JmixDrawerLayoutSlotController(this, 'drawerContentSlot');
        this.addController(this._contentController);

        this._attachDrawerSizeObserver();
    }

    _attachDrawerSizeObserver() {
        const observer = new ResizeObserver(entries => { this._updateContentAnimation(); });
        observer.observe(this.$.drawer);
    }
}

defineCustomElement(JmixDrawerLayout);

export { JmixDrawerLayout };