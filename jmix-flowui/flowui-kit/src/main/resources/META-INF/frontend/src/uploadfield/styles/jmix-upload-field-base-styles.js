/*
 * Copyright 2022 Haulmont.
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

import '@vaadin/component-base/src/styles/style-props.js';
import {field} from '@vaadin/field-base/src/styles/field-base-styles.js';
import {addGlobalStyles} from '@vaadin/component-base/src/styles/add-global-styles.js';
import {css} from 'lit';

const jmixUploadField = css`
    
    :host::before {
        display: inline-flex;
    }

    /*
     * Use "auto" width instead of default 12em, because upload field
     * with visible file name is not fit in.
     */
    [class$='container'] {
        width: var(--jmix-upload-field-default-width, var(--vaadin-field-default-width, auto));
    }

    [part='input-field'] {
        --vaadin-input-field-hover-highlight-opacity: 0;
        --vaadin-field-default-width: auto;
    }
    
    :host(:hover:not([readonly]):not([focused])) [part='input-field']::after {
        opacity: 0;
    }

    ::slotted(:not([slot$='fix'])) {
        padding: 0;
        -webkit-mask-image: 0;
        mask-image: 0;
    }

    ::slotted([slot='input']) {
        align-items: center;
        display: flex;
        gap: var(--vaadin-gap-s);
    }
`;

addGlobalStyles(
    'jmix-upload-field',
    css`
        jmix-upload-field[theme~="no-file-name"] .jmix-upload-button {
            flex-grow: 1;
        }

        .jmix-upload-field-file-name {
            cursor: pointer;
            margin: 0;
        }

        .jmix-upload-field-file-name:hover {
            text-decoration: underline;
        }

        .jmix-upload-field-file-name.empty {
            color: var(--vaadin-text-color);
            cursor: default;
        }

        .jmix-upload-field-clear {
            color: var(--vaadin-input-field-button-text-color, var(--vaadin-text-color-secondary));
            cursor: var(--vaadin-clickable-cursor);
            touch-action: manipulation;
            -webkit-tap-highlight-color: transparent;
            -webkit-user-select: none;
            user-select: none;
            /* Ensure minimum click target (WCAG) */
            padding: max(0px, (24px - 1lh) / 2);

            background: transparent;
            border: none;

            border-radius: var(--vaadin-button-border-radius, var(--vaadin-radius-m));
        }

        /* Icon */

        .jmix-upload-field-clear:before {
            background: currentColor;
            content: '';
            display: block;
            height: var(--vaadin-icon-size, 1lh);
            width: var(--vaadin-icon-size, 1lh);
            mask-size: var(--vaadin-icon-visual-size, 100%);
            mask-position: 50%;
            mask-repeat: no-repeat;
            mask-image: var(--_vaadin-icon-cross);
        }

        jmix-upload-field[readonly] .jmix-upload-field-clear {
            display: none;
        }

        jmix-upload-field[disabled] .jmix-upload-field-clear {
            color: var(--vaadin-text-color-disabled);
            cursor: var(--vaadin-disabled-cursor);
        }

        .jmix-upload-field-clear:focus-visible {
            outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
            outline-offset: 1px;
        }

        @media (forced-colors: active) {
            .jmix-upload-field-clear {
                background: var(--vaadin-background-container);
                border: 1px solid var(--vaadin-border-color-secondary);
            }
        }

        .jmix-upload-dialog-content {
            display: flex;
            flex-direction: column;
            min-width: 20em;
        }

        .jmix-upload-dialog-cancel-button {
            align-self: end;
        }
    `,
);

export const jmixUploadFieldStyles = [field, jmixUploadField];