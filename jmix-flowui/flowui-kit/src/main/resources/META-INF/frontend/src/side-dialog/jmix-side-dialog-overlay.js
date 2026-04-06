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

import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { DialogOverlay } from '@vaadin/dialog/src/vaadin-dialog-overlay.js';

import { jmixSideDialogOverlayStyles } from './styles/jmix-side-dialog-overlay-base-styles.js';

class JmixSideDialogOverlay extends DialogOverlay {

    static get styles() {
      return [...super.styles, jmixSideDialogOverlayStyles];
    }

    static get is() {
      return 'jmix-side-dialog-overlay';
    }
}

defineCustomElement(JmixSideDialogOverlay);