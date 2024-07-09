/**
 * @license
 * Copyright (c) 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */
import '@vaadin/tabsheet/src/vaadin-tabsheet-scroller.js';
import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { ControllerMixin } from '@vaadin/component-base/src/controller-mixin.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { TabSheetMixin } from '@vaadin/tabsheet/src/vaadin-tabsheet-mixin.js';

/**
 * `<jmix-tabsheet>` is a Web Component for organizing and grouping content
 * into scrollable panels. The panels can be switched between by using tabs.
 *
 * ```
 *  <jmix-tabsheet>
 *    <div slot="prefix">Prefix</div>
 *    <div slot="suffix">Suffix</div>
 *
 *    <vaadin-tabs slot="tabs">
 *      <vaadin-tab id="tab-1">Tab 1</vaadin-tab>
 *      <vaadin-tab id="tab-2">Tab 2</vaadin-tab>
 *      <vaadin-tab id="tab-3">Tab 3</vaadin-tab>
 *    </vaadin-tabs>
 *
 *    <div tab="tab-1">Panel 1</div>
 *    <div tab="tab-2">Panel 2</div>
 *    <div tab="tab-3">Panel 3</div>
 *  </jmix-tabsheet>
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
 * See [Styling Components](hhttps://vaadin.com/docs/latest/components/ds-resources/customization/styling-components) documentation.
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
// CAUTION: copied from @vaadin/tabsheet [last update Vaadin 24.4.4]
class JmixTabSheet extends TabSheetMixin(ThemableMixin(ElementMixin(ControllerMixin(PolymerElement)))) {
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
        return 'jmix-tabsheet';
    }
}

defineCustomElement(JmixTabSheet);

export {JmixTabSheet};
