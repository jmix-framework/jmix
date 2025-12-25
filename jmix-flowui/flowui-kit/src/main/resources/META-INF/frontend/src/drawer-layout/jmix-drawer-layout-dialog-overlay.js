import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DirMixin } from '@vaadin/component-base/src/dir-mixin.js';
import { OverlayMixin } from '@vaadin/overlay/src/vaadin-overlay-mixin.js';
import { DialogOverlayMixin } from '@vaadin/dialog/src/vaadin-dialog-overlay-mixin.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

import { dialogOverlay, resizableOverlay } from '@vaadin/dialog/src/vaadin-dialog-styles.js';
import { overlayStyles } from '@vaadin/overlay/src/vaadin-overlay-styles.js';
import { drawerLayoutDialogOverlayStyles } from './jmix-drawer-layout-dialog-overlay-styles.js';

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
                    <header part="header">
                        <slot name="drawerHeaderSlot"></slot>
                    </header>
                    <div part="content" id="content">
                        <slot name="drawerContentSlot"></slot>
                    </div>
                    <footer part="footer">
                        <slot id="drawerFooterSlot" name="drawerFooterSlot"></slot>
                    </footer>
                </section>
            </div>
        `;
    }

    ready() {
        super.ready();

        // Set attributes to display header and footer
        this.setAttribute('has-header', '');
        this.setAttribute('has-footer', '');
    }
}

defineCustomElement(JmixDrawerLayoutDialogOverlay);

export { JmixDrawerLayoutDialogOverlay };