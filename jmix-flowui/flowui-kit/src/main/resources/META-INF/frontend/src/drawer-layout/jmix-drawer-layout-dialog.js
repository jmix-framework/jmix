import { DialogBaseMixin } from '@vaadin/dialog/src/vaadin-dialog-base-mixin.js';
import { OverlayClassMixin } from '@vaadin/component-base/src/overlay-class-mixin.js';
import { ThemePropertyMixin } from '@vaadin/vaadin-themable-mixin/vaadin-theme-property-mixin.js';
import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';

import './jmix-drawer-layout-dialog-overlay.js';

class JmixDrawerLayoutDialog extends DialogBaseMixin(OverlayClassMixin(ThemePropertyMixin(PolymerElement))) {
    static get is() {
      return 'jmix-drawer-layout-dialog';
    }

    static get properties() {
      return {
        ariaLabel: {
          type: String,
        },

        fullscreen: {
          type: Boolean,
        },
      };
    }

    static get template() {
      return html`
        <style>
          :host {
            display: none;
          }
        </style>

        <jmix-drawer-layout-dialog-overlay
          id="overlay"
          opened="[[opened]]"
          aria-label$="[[ariaLabel]]"
          on-opened-changed="_onOverlayOpened"
          on-mousedown="_bringOverlayToFront"
          on-touchstart="_bringOverlayToFront"
          theme$="[[_theme]]"
          modeless="[[modeless]]"
          with-backdrop="[[!modeless]]"
          resizable$="[[resizable]]"
          fullscreen$="[[fullscreen]]"
          role="dialog"
          focus-trap
        ></jmix-drawer-layout-dialog-overlay>
      `;
    }
}

defineCustomElement(JmixDrawerLayoutDialog);