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

import { html, LitElement } from 'lit';
import { ifDefined } from 'lit/directives/if-defined.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { PolylitMixin } from '@vaadin/component-base/src/polylit-mixin.js';
import { LumoInjectionMixin } from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';
import { MediaQueryController } from '@vaadin/component-base/src/media-query-controller.js';
import { JmixSidePanelLayoutSlotController } from './jmix-side-panel-layout-slot-controller.js';

import { JmixSidePanelLayoutMixin } from './jmix-side-panel-layout-mixin.js';
import { sidePanelLayoutStyles } from './styles/jmix-side-panel-layout-base-styles.js';
import './jmix-side-panel-layout-dialog.js';

class JmixSidePanelLayout extends JmixSidePanelLayoutMixin(ElementMixin(ThemableMixin(PolylitMixin(LumoInjectionMixin(LitElement))))) {

    static get is() {
      return 'jmix-side-panel-layout';
    }

    static get styles() {
        return [sidePanelLayoutStyles];
    }

    render() {
        return html`
            <div id="layout" part="layout">
                <div id="content" part="content">
                    <slot name="contentSlot"></slot>
                </div>
                <div id="modalityCurtain" part="modalityCurtain" ?hidden="${this._modalityCurtainHidden}"></div>
                <div id="sidePanel" part="sidePanel">
                    <div id="sidePanelContent" part="sidePanelContent">
                         <slot name="sidePanelContentSlot"></slot>
                    </div>
                </div>
            </div>

            <jmix-side-panel-layout-dialog
                    id="dialog"
                    .opened="${this._computeDialogOpened(this.sidePanelOpened, this._displayAsOverlay)}"
                    .fullscreen="${this._displayAsOverlay}"
                    aria-label="${ifDefined(this.overlayAriaLabel)}"
                    theme="${ifDefined(this._theme)}"
                    @close="${this._closeSidePanel}"
                  ></jmix-side-panel-layout-dialog>
        `;
    }

    firstUpdated() {
        super.firstUpdated();

        this.addController(
            new MediaQueryController(this._displayAsOverlayMediaQuery, (matches) => {
                this._displayAsOverlay = this.displayAsOverlayOnSmallDevices ? matches : false;
                this.toggleAttribute('overlay', this._displayAsOverlay);
            }),
        );

        this._contentController = new JmixSidePanelLayoutSlotController(this, 'sidePanelContentSlot');
        this.addController(this._contentController);

        this._attachSidePanelSizeObserver();
    }

    _attachSidePanelSizeObserver() {
        const observer = new ResizeObserver(entries => { this._updateContentSize(); });
        observer.observe(this.$.sidePanel);
    }
}

defineCustomElement(JmixSidePanelLayout);

export { JmixSidePanelLayout };