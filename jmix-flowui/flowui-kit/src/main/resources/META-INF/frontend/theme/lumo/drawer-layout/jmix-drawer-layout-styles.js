import { dialogOverlay } from '@vaadin/dialog/theme/lumo/vaadin-dialog-styles.js';
import { overlay } from '@vaadin/vaadin-lumo-styles/mixins/overlay.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

registerStyles(
  'jmix-drawer-layout-dialog-overlay',
  [
    overlay,
    dialogOverlay,
  ],
  {
    moduleId: 'lumo-jmix-drawer-layout-dialog-overlay',
  },
);