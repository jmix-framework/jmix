import {html, PolymerElement} from '@polymer/polymer/polymer-element';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin';
import {embedDashboard} from "@superset-ui/embedded-sdk";
import {getGuestTokenRefreshTiming} from "@superset-ui/embedded-sdk/lib/guestTokenRefresh";

const GUEST_TOKEN_REFRESH_BUFFER = 5000;

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
                    width: 100%;
                    height: 100%;

                    border: none;
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
            /**
             * @protected
             */
            _url: {
                type: String,
                value: '',
            },
            url: {
                type: String,
                value: '',
                observer: '_onPropertyChanged'
            },
            embeddedId: {
                type: String,
                value: '',
                observer: '_onPropertyChanged'
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
            guestToken: {
                type: String,
                value: '',
                observer: '_onGuestTokenChanged'
            },
            /**
             * @protected
             */
            _guestToken: {
                type: String,
                value: '',
                observer: '_onInternalGuestTokenChanged',
            },
        }
    }

    updateDashboard() {
        if (!this._isReadyToEmbed()) {
            this.$.dashboard.replaceChildren() // removes all children
            this.$.dashboard.appendChild(this._createStubImageContainer());
            return;
        }
        const embedDashboardInternal = async () => {
            await embedDashboard({
                id: this.embeddedId, // the embedded ID specified in component
                supersetDomain: this.getBaseUrl(),
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
            this.dashboardInitialized = true;
        };
        embedDashboardInternal();
    }

    getGuestToken = async () => {
        if (this.guestToken) {
            return this.guestToken;
        }

        if (this._guestToken) {
            return this._guestToken;
        }
    }

    getBaseUrl() {
        return this.url ? this.url : this._url;
    }

    _isReadyToEmbed() {
        return (this.guestToken || this._guestToken)
            && this.embeddedId
            && (this.getBaseUrl() && this.getBaseUrl().length > 0);
    }

    _onPropertyChanged() {
        if (this.dashboardInitialized) {
            // If property changed after dashboard embedding, update it
            this.updateDashboard();
        }
    }

    _onGuestTokenChanged(token) {
        if (!token) {
            return;
        }

        this._stopGuestTokenRefreshTimer(this._guestTokenTimerId);

        if (!this.dashboardInitialized) {
            this.updateDashboard();
        }
    }

    _onInternalGuestTokenChanged(token) {
        if (!token) {
            return;
        }

        this._guestTokenTimerId = this._startGuestTokenRefreshTimer(token);

        if (!this.dashboardInitialized) {
            this.updateDashboard();
        }
    }

    _stopGuestTokenRefreshTimer(timerId) {
        clearTimeout(timerId);
    }

    _startGuestTokenRefreshTimer(_guestToken) {
        let supersetTiming = getGuestTokenRefreshTiming(_guestToken);
        return setTimeout(() => this.$server.refreshGuestToken(),
            supersetTiming - GUEST_TOKEN_REFRESH_BUFFER);
    }

    _createStubImageContainer() {
        const img = document.createElement('img');
        img.src = 'superset-dashboard/icons/superset.png';

        const container = document.createElement('div');
        container.id = 'stub-image-container'
        container.appendChild(img);

        return container;
    }
}

customElements.define(JmixSupersetDashboard.is, JmixSupersetDashboard);