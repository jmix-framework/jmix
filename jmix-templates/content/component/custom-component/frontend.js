import {html, LitElement} from 'lit';
import {defineCustomElement} from '@vaadin/component-base/src/define.js';

export class ${componentClassName} extends LitElement {

    static get is() {
        return '${tag}';
    }

    static get template() {
        return html`
            <!-- TODO define the component's markup -->
            <div></div>
        `;
    }

    static get properties() {
        return {
            // TODO declare properties synced with the server component, e.g.
            // value: {type: String, reflectToAttribute: true, notify: true}
        };
    }

    constructor() {
        super();
        // TODO initialize the component
    }
}

defineCustomElement(${componentClassName});
