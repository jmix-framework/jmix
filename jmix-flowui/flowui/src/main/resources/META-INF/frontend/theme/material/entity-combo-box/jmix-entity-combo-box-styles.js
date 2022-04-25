import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const entityComboBox = css`
  [part="action-part"] ::slotted(*) {
    gap: 0.25rem;
    margin-left: 0.25rem;
  }
`;

registerStyles('jmix-entity-combo-box', entityComboBox, {
    moduleId: 'material-entity-combo-box-styles'
});