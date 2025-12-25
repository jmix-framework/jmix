import { css } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

export const drawerLayoutDialogOverlayStyles = css`
    :host([fullscreen]) [part='overlay'] {
        height: 100%;
        width: 100%;
        border-radius: 0 !important;
    }

    :host([fullscreen]) [part='header'] ::slotted(:not([disabled])),
    :host([fullscreen]) [part='footer'] ::slotted(:not([disabled])) {
      pointer-events: auto;
    }
`;