/*
 * Copyright 2023 Haulmont.
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
import { css } from 'lit';
import { contentStyles } from "./jmix-code-editor-content-styles";

export const statesStyles = css`

    :host {
        width: 50em;
        height: 20em;
    }

    .jmix-code-editor-container {
        /*
         * Sized by the host through flex, not by a percentage: a percentage does not resolve
         * against an 'auto' height, and setting 'height' at all would suppress the cross-axis
         * stretch that gives the container its height when the host is sized by 'min-height'.
         */
        flex: 1;
        min-height: 0;
    }

    [part='input-field'] {
        height: auto;
        flex: 1;
        min-height: 0;
        box-sizing: border-box;
        display: flex;
        border: var(--vaadin-input-field-border-width, 1px) solid var(--vaadin-input-field-border-color, var(--vaadin-border-color));
        border-radius: var(--vaadin-radius-m);
    }

    /*
     * Ace is rendered into a nested element rather than into the 'input-field' part. The part is a
     * public styling hook, so an application theme addressing '::part(input-field)' from the
     * document wins over the styles an Ace theme injects into the shadow root, and would repaint
     * the editor with the colors of the field.
     */
    .jmix-code-editor-canvas,
    /*
     * Ace adds '.ace_editor' to the canvas on initialization; the doubled class outweighs the
     * percentage sizing of the vendored Ace stylesheet regardless of the order of the rules.
     */
    .jmix-code-editor-canvas.ace_editor {
        /* The canvas is sized by the field: stretched vertically, filling it horizontally. */
        flex: 1;
        width: auto;
        height: auto;
        min-width: 0;
        border-radius: inherit;
        overflow: hidden;
    }

    /*
     * The canvas covers the field with the background of the Ace theme, including the background a
     * theme paints for the disabled state, so the state is conveyed by dimming the canvas instead.
     */
    :host([disabled]) .jmix-code-editor-canvas {
        opacity: 0.5;
    }

    [part='input-field']:focus-within {
        outline: var(--vaadin-focus-ring-width) solid var(--vaadin-focus-ring-color);
        outline-offset: calc(var(--vaadin-input-field-border-width, 1px) * -1);
    }

    :host([readonly]) [part='input-field']:focus-within {
        outline-style: dashed;
        --vaadin-input-field-border-color: transparent;
    }

    :host([invalid]) {
        --vaadin-input-field-border-color: var(--vaadin-input-field-error-color, var(--vaadin-text-color));
    }

    [part='input-field'] {
        transition: background-color 0.1s;
    }

    :host(:not([readonly])) [part='input-field']::after {
        display: none;
    }

    :host([readonly]) [part='input-field'] {
        border-style: dashed;
    }

    :host([readonly]) [part='input-field']::after {
        border: none;
    }
`;

export const jmixCodeEditorStyles = [contentStyles, statesStyles];