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
import { buttonStyles } from '@vaadin/button/src/styles/vaadin-button-base-styles.js';

/* Copied from packages/app-layout/src/styles/vaadin-drawer-toggle-base-styles.js */
export const jmixSidePanelLayoutCloser = css`

    [part='icon'] {
      background: currentColor;
      display: block;
      height: var(--vaadin-icon-size, 1lh);
      mask: var(--_vaadin-icon-cross) 50% / var(--vaadin-icon-visual-size, 100%) no-repeat;
      width: var(--vaadin-icon-size, 1lh);
    }

    [hidden] {
      display: none !important;
    }

    @media (forced-colors: active) {

        [part='icon'] {
          background: CanvasText;
        }
    }
`;

export const jmixSidePanelLayoutCloserStyles = [buttonStyles, jmixSidePanelLayoutCloser];