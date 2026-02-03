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
import {css} from 'lit';

export const jmixUploadFieldStyles = css`
    vaadin-input-container {
        background-color: transparent;
        padding: 0;
        cursor: auto;
    }

    vaadin-input-container:after {
        border: 0;
    }
    
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
    }
    
    :host(:hover:not([readonly]):not([focused])) [part='input-field']::after {
        opacity: 0;
    }

    ::slotted(:not([slot$='fix'])) {
        padding: 0;
        --_lumo-text-field-overflow-mask-image: 0;
        -webkit-mask-image: 0;
        mask-image: 0;
    }
`;
