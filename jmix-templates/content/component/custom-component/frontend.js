import {css, html, LitElement} from 'lit';
import {DisabledMixin} from '@vaadin/a11y-base/src/disabled-mixin.js';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';
import {PolylitMixin} from '@vaadin/component-base/src/polylit-mixin.js';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

export class ${componentClassName} extends DisabledMixin(ThemableMixin(PolylitMixin(LitElement))) {

    static get is() {
        return '${tag}';
    }

    static get styles() {
        return css`
            :host {
                display: inline-block;
            }

            :host([hidden]) {
                display: none !important;
            }

            :host([disabled]) {
                pointer-events: none;
            }
        `;
    }

    static get properties() {
        return {
            // TODO declare properties synced with the server component, e.g.
            // value: {type: String, reflectToAttribute: true, notify: true}
        };
    }

    render() {
        return html`
            <!-- TODO define the component's markup -->
            <div></div>
        `;
    }

    ready() {
        super.ready();
        // TODO initialize the component
    }
}

defineCustomElement(${componentClassName});
