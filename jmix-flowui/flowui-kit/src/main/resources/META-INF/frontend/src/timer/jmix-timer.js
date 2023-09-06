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

import { html, PolymerElement } from '@polymer/polymer';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';

export class JmixTimer extends ElementMixin(PolymerElement) {

    static get is() {
        return 'jmix-timer';
    }

    static get template() {
        return html``;
    }

    static get properties() {
        return {
            repeating: {
                type: Boolean,
                value: false,
            },
            delay: {
                type: Number,
                value: 0
            }
        };
    }

    start() {
        if (!this.running) {
            this.runTimer();
            this.running = true;
        }
    }

    runTimer() {
        this.intervalId = setTimeout(this.onTimerTick.bind(this), this.delay);
    }

    onTimerTick() {
        this.dispatchEvent(new CustomEvent('jmix-timer-tick'))
        if (this.repeating) {
            this.runTimer();
        } else {
            this.intervalId = null;
            this.running = false;
        }
    }

    stop() {
        if (this.running) {
            clearInterval(this.intervalId);
            this.intervalId = null;
            this.running = false;
            this.dispatchEvent(new CustomEvent('jmix-timer-stop'))
        }
    }

    disconnectedCallback() {
        this.stop();
    }
}

window.customElements.define(JmixTimer.is, JmixTimer);