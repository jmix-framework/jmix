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
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';


/*
Vaadin behaviour:
If a page is refreshed or duplicated, chain of component connectCallback-disconnectCallback-connectCallback is invoked.

So, to prevent faulty stop events during page refresh or duplication,
"jmix-timer-stop event" is not fired on disconnect but only when stop is triggered by user
*/
export class JmixTimer extends ElementMixin(PolymerElement) {

    static get is() {
        return 'jmix-timer';
    }

    static get template() {
        return html`
            <style>
                :host {
                    display: none;
                }
            </style>
        `;
    }

    static get properties() {
        return {
            repeating: {
                type: Boolean,
                value: false
            },
            delay: {
                type: Number,
                value: 0
            },
            autostart: {
                type: Boolean,
                value: false
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
        this.stopInternal(true);
    }

    stopInternal(userOriginated) {
        if (this.running) {
            clearInterval(this.intervalId);
            this.intervalId = null;
            this.running = false;
            if (userOriginated) {
                this.dispatchEvent(new CustomEvent('jmix-timer-stop'))
            }
        }
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        //stop without firing stop event
        this.stopInternal(false);
    }

    connectedCallback() {
        super.connectedCallback();
        if (this.autostart) {
            this.start();
        }
    }
}

defineCustomElement(JmixTimer);