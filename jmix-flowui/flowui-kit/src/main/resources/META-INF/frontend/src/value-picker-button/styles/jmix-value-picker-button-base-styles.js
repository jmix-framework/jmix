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

import '@vaadin/component-base/src/styles/style-props.js';
import {css} from 'lit';

export const jmixValuePickerButtonStyles = css`
    :host {
        display: inline-block;
        position: relative;
        outline: none;
        white-space: nowrap;
        -webkit-user-select: none;
        -moz-user-select: none;
        user-select: none;
    }

    :host([hidden]) {
        display: none !important;
    }

    /* Aligns the button with form fields when placed on the same line.
    Note, to make it work, the form fields should have the same "::before" pseudo-element. */

    .value-picker-button-container::before {
        content: '\\2003';
        display: inline-block;
        width: 0;
    }

    .value-picker-button-container {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        width: 100%;
        height: 100%;
        min-height: inherit;
        text-shadow: inherit;
        background: transparent;
        padding: 0;
        border: none;
        box-shadow: none;
    }

    [part='icon'] {
        flex: none;
    }
`;