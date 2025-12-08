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

import {css} from 'lit';

export const gridLayoutStyles = css`
    :host {
        display: flex;
        box-sizing: border-box;
    }

    :host([hidden]) {
        display: none !important;
    }

    [part='items'] {
        display: grid;
        grid-gap: var(--_grid-layout-grid-gap, var(--lumo-space-s));
        grid-template-columns: repeat(auto-fit, minmax(var(--_grid-layout-column-min-width, 19rem), 1fr));

        height: 100%;
        width: 100%;
        -webkit-overflow-scrolling: touch;
    }
`;
