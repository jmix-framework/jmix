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

import {css} from 'lit';

export const stepperStyles = css`
    :host {
        display: flex;
        flex-direction: column;
        box-sizing: border-box;
        min-width: 0;
        min-height: 0;
    }

    :host([hidden]) {
        display: none !important;
    }

    :host([orientation='vertical']) {
        flex-direction: row;
    }

    [part='header'] {
        display: flex;
        flex: none;
        align-items: flex-start;
        min-width: 0;
        overflow: hidden;
    }

    :host([orientation='vertical']) [part='header'] {
        flex-direction: column;
        align-items: stretch;
        overflow: visible;
    }

    [part='content'] {
        display: flex;
        flex-direction: column;
        flex: 1 1 auto;
        min-width: 0;
        min-height: 0;
        overflow: auto;
        box-sizing: border-box;
        padding: var(--jmix-stepper-content-padding, var(--vaadin-padding-m, 1rem));
    }

    :host([theme~='no-padding']) [part='content'] {
        padding: 0;
    }
`;
