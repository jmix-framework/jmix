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

import { dialogOverlay } from '@vaadin/dialog/theme/lumo/vaadin-dialog-styles.js';
import { overlay } from '@vaadin/vaadin-lumo-styles/mixins/overlay.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const sidePanelLayoutStyles = css`

    [part='sidePanel'] {
        background-color: var(--lumo-base-color);
        box-shadow: var(--lumo-box-shadow-m);
    }

    :host([side-panel-opened][modal]) [part='modalityCurtain'] {
        background-color: var(--lumo-shade-20pct);
    }

    :host(:not([side-panel-opened])[modal]) [part='modalityCurtain']:not([hidden]) {
        background-color: var(--lumo-shade-20pct);
    }

    :host([side-panel-opened][side-panel-position='inline-start']) [part='sidePanel'] {
        border-inline-end: 1px solid var(--lumo-contrast-10pct);
    }

    :host([side-panel-opened][side-panel-position='inline-end']) [part='sidePanel'] {
        border-inline-start: 1px solid var(--lumo-contrast-10pct);
    }

    :host([side-panel-opened][side-panel-position='']) [part='sidePanel'],
    :host([side-panel-opened][side-panel-position='right']) [part='sidePanel'] {
        border-left: 1px solid var(--lumo-contrast-10pct);
    }

    :host([side-panel-opened][side-panel-position='left']) [part='sidePanel'] {
        border-right: 1px solid var(--lumo-contrast-10pct);
    }

    :host([side-panel-opened][side-panel-position='top']) [part='sidePanel'] {
        border-block-end: 1px solid var(--lumo-contrast-10pct);
    }

    :host([side-panel-opened][side-panel-position='bottom']) [part='sidePanel'] {
        border-block-start: 1px solid var(--lumo-contrast-10pct);
    }
`;

registerStyles('jmix-side-panel-layout-dialog-overlay', [overlay, dialogOverlay],
    { moduleId: 'lumo-jmix-side-panel-layout-dialog-overlay' },
);

registerStyles('jmix-side-panel-layout', [sidePanelLayoutStyles],
    { moduleId: 'lumo-jmix-side-panel-layout'})