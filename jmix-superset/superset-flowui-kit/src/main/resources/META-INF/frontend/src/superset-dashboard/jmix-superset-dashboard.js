import {html, PolymerElement} from '@polymer/polymer/polymer-element';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin';
import {embedDashboard} from "@superset-ui/embedded-sdk";
import {getGuestTokenRefreshTiming} from "@superset-ui/embedded-sdk/lib/guestTokenRefresh";

class JmixSupersetDashboard extends ThemableMixin(ElementMixin(PolymerElement)) {

    static get template() {
        return html`
            <style>
                #dashboard {
                    width: 100%;
                    height: 100%;
                    background-color: #f7f7f7;
                }

                #dashboard iframe {
                    border: none;
                    width: 100%;
                    height: 100%;
                }

                #stub-image-container {
                    align-items: center;
                    display: flex;
                    justify-content: center;
                    height: 100%;
                }

                #stub-image-container img {
                    width: 50px;
                }
            </style>
            <div id="dashboard">
                <div id="stub-image-container">
                    <img src="superset-dashboard/icons/superset.png"/>
                </div>
            </div>
        `;
    }

    static get is() {
        return 'jmix-superset-dashboard';
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
        img.src = 'superset-dashboard/icons/superset.png';

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