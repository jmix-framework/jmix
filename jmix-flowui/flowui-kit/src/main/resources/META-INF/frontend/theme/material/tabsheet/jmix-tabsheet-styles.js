// CAUTION: copied from @vaadin/tabsheet/theme/material/vaadin-tabsheet-style.js [since Vaadin 23.3.0]
import '@vaadin/vaadin-material-styles/color.js';
import '@vaadin/vaadin-material-styles/typography.js';
import { loader } from '@vaadin/vaadin-material-styles/mixins/loader.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const tabsheet = css`
  :host {
    font-family: var(--material-font-family);
  }

  :host([theme~='bordered']) {
    border-radius: 4px;
    border: 1px solid var(--material-divider-color);
  }

  [part='tabs-container'] {
    box-shadow: inset 0 -1px 0 0 var(--material-divider-color);
    gap: 8px;
    padding: 4px 8px;
  }

  ::slotted([slot='tabs']) {
    margin: -4px -8px;
  }

  [part='content'] {
    padding: 24px;
    border-bottom-left-radius: inherit;
    border-bottom-right-radius: inherit;
  }

  :host([loading]) [part='content'] {
    overflow: visible;
  }

  [part='loader'] {
    position: absolute;
    z-index: 1;
    top: 0;
    left: 0;
    right: 0;
    transform: translate(0, -100%);
  }
`;

registerStyles('jmix-tabsheet', [tabsheet, loader], {
    moduleId: 'material-tabsheet-styles'
});
