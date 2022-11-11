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

import { css, registerStyles } from "@vaadin/vaadin-themable-mixin";

const pickerButton = css`
  :host {
    padding: 8px;
    min-width: 20px;
    box-sizing: border-box;
    display: inline-flex;
    align-items: baseline;
    justify-content: center;
    border-radius: 4px;
    color: var(--material-primary-text-color);
    font-family: var(--material-font-family);
    text-transform: uppercase;
    font-size: var(--material-button-font-size);
    line-height: 20px;
    font-weight: 500;
    letter-spacing: 0.05em;
    white-space: nowrap;
    overflow: hidden;
    transition: box-shadow 0.2s;
    -webkit-tap-highlight-color: transparent;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  :host::before,
  :host::after {
    content: '';
    pointer-events: none;
    position: absolute;
    border-radius: inherit;
    opacity: 0;
    background-color: currentColor;
  }

  :host::before {
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    transition: opacity 0.5s;
  }

  :host::after {
    border-radius: 50%;
    width: 320px;
    height: 320px;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    transition: all 0.9s;
  }

  [part='label'] ::slotted(*) {
    vertical-align: middle;
  }

  :host(:hover)::before,
  :host([focus-ring])::before {
    opacity: 0.08;
    transition-duration: 0.2s;
  }

  :host([active])::before {
    opacity: 0.16;
    transition: opacity 0.4s;
  }

  :host([active])::after {
    transform: translate(-50%, -50%) scale(0.0000001); /* animation works weirdly with scale(0) */
    opacity: 0.1;
    transition: 0s;
  }

  :host(:hover:not([active]))::after {
    transform: translate(-50%, -50%) scale(1);
    opacity: 0;
  }

  :host([disabled]) {
    pointer-events: none;
    color: var(--material-disabled-text-color);
  }

  /* Contained and outline variants */
  :host([theme~='contained']),
  :host([theme~='outlined']) {
    padding: 8px 16px;
  }

  :host([theme~='outlined']) {
    box-shadow: inset 0 0 0 1px var(--_material-button-outline-color, rgba(0, 0, 0, 0.2));
  }

  :host([theme~='contained']:not([disabled])) {
    background-color: var(--material-primary-color);
    color: var(--material-primary-contrast-color);
    box-shadow: var(--material-shadow-elevation-2dp);
  }

  :host([theme~='contained'][disabled]) {
    background-color: var(--material-secondary-background-color);
  }

  :host([theme~='contained']:hover) {
    box-shadow: var(--material-shadow-elevation-4dp);
  }

  :host([theme~='contained'][active]) {
    box-shadow: var(--material-shadow-elevation-8dp);
  }

  /* Icon alignment */

  [part] ::slotted(vaadin-icon),
  [part] ::slotted(iron-icon) {
    display: block;
    width: 18px;
    height: 18px;
  }
`;

registerStyles('jmix-value-picker-button', pickerButton, {
  moduleId: 'material-value-picker-button-styles'
});