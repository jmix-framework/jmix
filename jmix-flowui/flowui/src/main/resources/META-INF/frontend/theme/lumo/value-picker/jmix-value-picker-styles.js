import { inputFieldShared } from '@vaadin/vaadin-lumo-styles/mixins/input-field-shared.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const valuePicker = css`
  [part="action-part"] ::slotted(*) {
    gap: var(--lumo-space-xs);
  }
  
  :host([has-actions]) [part="input-field"] {
    padding-right: var(--lumo-space-xs);
  }
`;

registerStyles('jmix-value-picker', [inputFieldShared, valuePicker], {
  moduleId: 'lumo-value-picker-styles'
});
