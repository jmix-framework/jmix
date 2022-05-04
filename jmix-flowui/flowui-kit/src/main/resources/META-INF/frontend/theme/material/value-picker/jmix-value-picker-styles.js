import { inputFieldShared } from '@vaadin/vaadin-material-styles/mixins/input-field-shared.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const valuePicker = css`
  [part="action-part"] ::slotted(*) {
    gap: 0.25rem;
  }
`;

registerStyles('jmix-value-picker', [inputFieldShared, valuePicker], {
  moduleId: 'material-value-picker-styles'
});
