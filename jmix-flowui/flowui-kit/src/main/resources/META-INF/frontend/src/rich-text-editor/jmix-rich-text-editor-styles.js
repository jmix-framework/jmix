/*
 * Copyright 2024 Haulmont.
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

import { css } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

export const jmixRichTextEditorStyles = css`
    :host {
        display: flex;
        box-sizing: content-box;

        min-height: calc(var(--lumo-size-m) * 8);
        --_focus-ring-color: var(--vaadin-focus-ring-color, var(--lumo-primary-color-50pct));
        --_focus-ring-width: var(--vaadin-focus-ring-width, 2px);

        font-family: var(--lumo-font-family);
        font-size: var(--lumo-font-size-m);
        line-height: var(--lumo-line-height-m);
        -webkit-text-size-adjust: 100%;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        
        padding: var(--lumo-space-xs) 0;
    }

    :host([hidden]) {
        display: none !important;
    }

    :host([disabled]) {
        pointer-events: none;
        opacity: 0.5;
        -webkit-user-select: none;
        -moz-user-select: none;
        user-select: none;
    }

    :host(:not([has-label])) [part='label'] {
        display: none;
    }

    :host([disabled]) [part='label'] {
        color: var(--lumo-disabled-text-color);
        -webkit-text-fill-color: var(--lumo-disabled-text-color);
    }

    .jmix-rich-text-editor-wrapper {
        display: flex;
        flex-direction: column;
        min-height: 100%;
        max-height: 100%;
        flex: auto;
    }

    [part="editor"] {
        display: flex;
        flex-direction: column;
        flex: auto;
        height: 100%;
        overflow-y: auto;
    }

    :host(:not([theme~='no-border'])) [part="editor"] {
        border: 1px solid var(--lumo-contrast-20pct);
        border-radius: var(--lumo-border-radius-m);
    }
    
    /**
     * Label styles
     * 
     * Can be replaced with packages/vaadin-lumo-styles/mixins/required-field.js import
     * after other required-field functionality is added 
     */

    [part='label'] {
        align-self: flex-start;
        color: var(--vaadin-input-field-label-color, var(--lumo-secondary-text-color));
        font-weight: var(--vaadin-input-field-label-font-weight, 500);
        font-size: var(--vaadin-input-field-label-font-size, var(--lumo-font-size-s));
        margin-left: calc(var(--lumo-border-radius-m) / 4);
        transition: color 0.2s;
        line-height: 1;
        padding-right: 1em;
        padding-bottom: 0.5em;
        /* As a workaround for diacritics being cut off, add a top padding and a
        negative margin to compensate */
        padding-top: 0.25em;
        margin-top: -0.25em;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        position: relative;
        max-width: 100%;
        box-sizing: border-box;
    }

    :host([focused]:not([readonly])) [part='label'] {
        color: var(--vaadin-input-field-focused-label-color, var(--lumo-primary-text-color));
    }

    :host(:hover:not([readonly]):not([focused])) [part='label'] {
        color: var(--vaadin-input-field-hovered-label-color, var(--lumo-body-text-color));
    }

    /* Touch device adjustment */
    @media (pointer: coarse) {
        :host(:hover:not([readonly]):not([focused])) [part='label'] {
            color: var(--vaadin-input-field-label-color, var(--lumo-secondary-text-color));
        }
    }

    :host([has-label]) {
        padding-top: var(--lumo-space-m);
    }

    /* RTL specific styles */

    :host([dir='rtl']) [part='label'] {
        margin-left: 0;
        margin-right: calc(var(--lumo-border-radius-m) / 4);
    }

    :host([dir='rtl']) [part='label'] {
        padding-left: 1em;
        padding-right: 0;
    }
`;