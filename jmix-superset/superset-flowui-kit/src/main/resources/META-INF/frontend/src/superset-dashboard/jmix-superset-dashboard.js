/*
 * Copyright 2024 Haulmont.
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

import { html, LitElement } from 'lit';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';

import { ElementMixin } from '@vaadin/component-base/src/element-mixin';
import { ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin';
import { PolylitMixin } from '@vaadin/component-base/src/polylit-mixin.js';
import { LumoInjectionMixin } from '@vaadin/vaadin-themable-mixin/lumo-injection-mixin.js';

import { embedDashboard } from "@superset-ui/embedded-sdk";
import { getGuestTokenRefreshTiming } from "@superset-ui/embedded-sdk/lib/guestTokenRefresh";

import { jmixSupersetDashboardStyles } from './styles/jmix-superset-dashboard-base-styles.js';

class JmixSupersetDashboard extends ThemableMixin(ElementMixin(PolylitMixin(LumoInjectionMixin(LitElement)))) {

    static get is() {
        return 'jmix-superset-dashboard';
    }

    static get styles() {
        return jmixSupersetDashboardStyles;
    }

    render() {
        return html`
            <div id="dashboard">
                <div id="stub-image-container">
                    <img src="${this._stubImageUrl}"/>
                </div>
            </div>
        `;
    }

    static get properties() {
        return {
            url: {
                type: String,
            },
            titleVisible: {
                type: Boolean,
                value: false,
                observer: '_onPropertyChanged'
            },
            chartControlsVisible: {
                type: Boolean,
                value: false,
                observer: '_onPropertyChanged'
            },
            filtersExpanded: {
                type: Boolean,
                value: false,
                observer: '_onPropertyChanged'
            },
            embeddedId: {
                type: String,
                observer: '_onEmbeddedIdChanged'
            },
            guestToken: {
                type: String,
                observer: '_onGuestTokenChanged'
            },
            _stubImageUrl: {
                type: String,
                value: '',
            },
        }
    }

    _embedDashboard() {
        if (!this._isReadyToEmbed()) {
            this._replaceDashboardByStub();
            return;
        }
        const embedDashboardAsync = async () => {
            await embedDashboard({
                id: this.embeddedId, // the embedded ID specified in component
                supersetDomain: this.url,
                mountPoint: this.$.dashboard, // html element in which iframe render
                fetchGuestToken: () => this.getGuestToken(),
                dashboardUiConfig: {
                    hideTitle: !this.titleVisible,
                    hideChartControls: !this.chartControlsVisible,
                    filters: {
                        expanded: this.filtersExpanded
                    }
                },
            })
            this.isDashboardEmbedded = true;
        };
        embedDashboardAsync();
    }

    getGuestToken = async () => {
        return this.guestToken;
    }

    _isReadyToEmbed() {
        return this.guestToken && this.embeddedId && this.url;
    }

    _onPropertyChanged() {
        if (this.isDashboardEmbedded) {
            // If property changed after dashboard embedding, update dashboard
            this._embedDashboard();
        }
    }

    _onGuestTokenChanged(token) {
        this._stopGuestTokenRefreshTimer(this._guestTokenTimerId);

        if (!token) {
            return;
        }

        this._guestTokenTimerId = this._startGuestTokenRefreshTimer(token);

        if (!this.isDashboardEmbedded || this.embaddedIdForceChange) {
            this.embaddedIdForceChange = false;
            this._embedDashboard();
        }
    }

    _onEmbeddedIdChanged(embeddedId) {
        if (!embeddedId) {
            this._replaceDashboardByStub();
            this._stopGuestTokenRefreshTimer(this._guestTokenTimerId);
        } else {
            this.embaddedIdForceChange = true;
            this._callFetchGuestToken();
        }
    }

    _stopGuestTokenRefreshTimer(timerId) {
        clearTimeout(timerId);
    }

    _startGuestTokenRefreshTimer(_guestToken) {
        let supersetTiming = getGuestTokenRefreshTiming(_guestToken);
        return setTimeout(() => this._callFetchGuestToken(), supersetTiming);
    }

    _createStubImageContainer() {
        const img = document.createElement('img');
        img.src = this._stubImageUrl;

        const container = document.createElement('div');
        container.id = 'stub-image-container'
        container.appendChild(img);

        return container;
    }

    _replaceDashboardByStub() {
        this.$.dashboard.replaceChildren() // removes all children
        this.$.dashboard.appendChild(this._createStubImageContainer());
        this.isDashboardEmbedded = false;
    }

    _callFetchGuestToken() {
        this.$server.fetchGuestToken()
    }
}

customElements.define(JmixSupersetDashboard.is, JmixSupersetDashboard);