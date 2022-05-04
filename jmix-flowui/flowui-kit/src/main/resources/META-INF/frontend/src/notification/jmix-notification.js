import { Notification } from "@vaadin/notification";
import { processTemplates } from '@vaadin/component-base/src/templates.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

// todo rp, support themes instead of hardcode Lumo usage

registerStyles(
    'vaadin-notification-card',
    css`
    :host([theme~='warning'][class~='jmix-notification-card']) [part='overlay'] {
      background-color: var(--jmix-lumo-warning-background-color);
      color: var(--jmix-lumo-warning-color);
      box-shadow: var(--lumo-box-shadow-l);
    }
    :host([theme~='warning'][class~='jmix-notification-card']) {
      --_lumo-button-background-color: var(--lumo-shade-20pct);
      --_lumo-button-color: var(--jmix-lumo-warning-color);
      --_lumo-button-primary-background-color: var(--jmix-lumo-warning-color);
      --_lumo-button-primary-color: var(--jmix-lumo-warning-primary-text-color);
    }
    :host([theme~='contrast'][class~='jmix-notification-card']) [part='overlay'] {
      display: flex;
      justify-content: center;
      width: 100%;
    }
  `,
    { moduleId: 'jmix-lumo-notification-card' }
);

class JmixNotification extends Notification {

    static get is() {
        return 'jmix-notification';
    }

    ready() {
        super.ready();

        this._card.classList.add("jmix-notification-card")

        processTemplates(this);
    }
}

customElements.define(JmixNotification.is, JmixNotification);