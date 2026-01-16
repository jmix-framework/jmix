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

import { css } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

/*
 * CAUTION! Copied from packages/app-layout/src/vaadin-drawer-toggle-styles.js
 */
export const jmixSidePanelCloserLayout = css`
    :host {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        cursor: default;
        position: relative;
        outline: none;
        height: 24px;
        width: 24px;
        padding: 4px;
    }

    [part='icon'],
    [part='icon']::after,
    [part='icon']::before {
       position: absolute;
       top: 8px;
       height: 3px;
       width: 24px;
       background-color: #000;
    }

    [part='icon']::after,
    [part='icon']::before {
       content: '';
    }

    [part='icon']::after {
       top: 6px;
    }

    [part='icon']::before {
       top: 12px;
    }

    @media (forced-colors: active) {

        :host {
            outline: 1px solid;
            outline-offset: -1px;
        }
    }
`;
