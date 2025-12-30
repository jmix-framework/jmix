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

import { dialogOverlay } from '@vaadin/dialog/theme/lumo/vaadin-dialog-styles.js';
import { overlay } from '@vaadin/vaadin-lumo-styles/mixins/overlay.js';
import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const drawerLayoutStyles = css`

    [part='drawer'] {
        background-color: var(--lumo-base-color);
        box-shadow: var(--lumo-box-shadow-m);
    }

    :host([drawer-opened][modal][theme~='dimmed-curtain']) [part='modalityCurtain'] {
        background-color: var(--lumo-shade-20pct);
    }

    :host(:not([drawer-opened])[modal][theme~='dimmed-curtain']) [part='modalityCurtain']:not([hidden]) {
        background-color: var(--lumo-shade-20pct);
    }

    :host([drawer-opened][drawer-placement='inline-start']) [part='drawer'] {
        border-inline-end: 1px solid var(--lumo-contrast-10pct);
    }

    :host([drawer-opened][drawer-placement='inline-end']) [part='drawer'] {
        border-inline-start: 1px solid var(--lumo-contrast-10pct);
    }

    :host([drawer-opened][drawer-placement='']) [part='drawer'],
    :host([drawer-opened][drawer-placement='right']) [part='drawer'] {
        border-left: 1px solid var(--lumo-contrast-10pct);
    }

    :host([drawer-opened][drawer-placement='left']) [part='drawer'] {
        border-right: 1px solid var(--lumo-contrast-10pct);
    }

    :host([drawer-opened][drawer-placement='top']) [part='drawer'] {
        border-block-end: 1px solid var(--lumo-contrast-10pct);
    }

    :host([drawer-opened][drawer-placement='bottom']) [part='drawer'] {
        border-block-start: 1px solid var(--lumo-contrast-10pct);
    }
`;

registerStyles('jmix-drawer-layout-dialog-overlay', [overlay, dialogOverlay],
    { moduleId: 'lumo-jmix-drawer-layout-dialog-overlay' },
);

registerStyles('jmix-drawer-layout', [drawerLayoutStyles],
    { moduleId: 'lumo-jmix-drawer-layout'})