import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';


const entityComboBox = css`
  [part="action-part"] ::slotted(*) {
    gap: var(--lumo-space-xs);
    margin-left: var(--lumo-space-xs);
  }
  
  :host([has-actions]) [part="input-field"] {
    padding-right: var(--lumo-space-xs);
  }
`;

registerStyles('jmix-combo-box-picker', entityComboBox,{
    moduleId: 'lumo-combo-box-picker-styles'
});