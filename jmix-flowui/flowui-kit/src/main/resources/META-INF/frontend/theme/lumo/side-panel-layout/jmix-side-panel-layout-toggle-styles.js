/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import '@vaadin/vaadin-lumo-styles/color.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/sizing.js';
import '@vaadin/vaadin-lumo-styles/font-icons.js';
import { button } from '@vaadin/button/theme/lumo/vaadin-button-styles.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

/*
 * CAUTION! Copied from packages/app-layout/theme/lumo/vaadin-drawer-toggle-styles.js
 */
const sidePanelDrawerLayoutToggleStyles = css`
    :host {
        width: var(--lumo-size-l);
        height: var(--lumo-size-l);
        min-width: auto;
        margin: 0 var(--lumo-space-s);
        padding: 0;
        background: transparent;
    }

    [part='icon'],
    [part='icon']::after,
    [part='icon']::before {
        position: inherit;
        height: auto;
        width: auto;
        background: transparent;
        top: auto;
    }

    [part='icon']::before {
        font-family: lumo-icons;
        font-size: var(--lumo-icon-size-m);
        content: var(--lumo-icons-menu);
    }

    :host([slot~='navbar']) {
        color: var(--lumo-secondary-text-color);
    }
`;

// TODO: pinyazhin, rename to "lumo-drawer.." after updating Vaadin to 25
registerStyles('jmix-side-panel-layout-toggle', [button, sidePanelDrawerLayoutToggleStyles], { moduleId: 'jmix-lumo-side-panel-layout-toggle' });
