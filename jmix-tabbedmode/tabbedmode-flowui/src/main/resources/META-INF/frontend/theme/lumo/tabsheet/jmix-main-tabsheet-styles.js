/*
 * Copyright 2025 Haulmont.
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

// CAUTION: copied from @vaadin/tabsheet/theme/lumo/vaadin-tabsheet-styles.js [last update Vaadin 24.6.3]
import '@vaadin/vaadin-lumo-styles/color.js';
import '@vaadin/vaadin-lumo-styles/spacing.js';
import '@vaadin/vaadin-lumo-styles/typography.js';
import {loader} from '@vaadin/vaadin-lumo-styles/mixins/loader.js';
import {css, registerStyles} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const jmixMainTabsheetStyles = css`
    :host {
        font-size: var(--lumo-font-size-m);
        line-height: var(--lumo-line-height-m);
        font-family: var(--lumo-font-family);

        box-sizing: border-box;
    }

    :host([theme~='bordered']) {
        border: 1px solid var(--lumo-contrast-20pct);
        border-radius: var(--lumo-border-radius-l);
    }

    [part='tabs-container'] {
        box-shadow: inset 0 -1px 0 0 var(--lumo-contrast-10pct);
        padding: var(--lumo-space-xs) var(--lumo-space-s);
        gap: var(--lumo-space-s);
    }

    ::slotted([slot='tabs']) {
        box-shadow: initial;
        margin: calc(var(--lumo-space-xs) * -1) calc(var(--lumo-space-s) * -1);
    }

    [part='content'] {
        padding: 0;
        border-bottom-left-radius: inherit;
        border-bottom-right-radius: inherit;
    }

    :host([loading]) [part='content'] {
        display: flex;
        align-items: center;
        justify-content: center;
    }

    /* Jmix specifics:
    - padding: 0 by default
    - no need in 'no-padding' theme variant
    */
`;

registerStyles('jmix-main-tabsheet', [jmixMainTabsheetStyles, loader], {moduleId: 'lumo-main-tabsheet-styles'});