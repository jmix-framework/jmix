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
        height: 100%;
    }

    [part='input-field'] {
        height: auto;
        box-sizing: border-box;
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