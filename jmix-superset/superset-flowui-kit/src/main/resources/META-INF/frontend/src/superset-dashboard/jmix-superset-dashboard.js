import {html, PolymerElement} from '@polymer/polymer/polymer-element';
import {ElementMixin} from '@vaadin/component-base/src/element-mixin';
import {ThemableMixin} from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin';
import {embedDashboard} from "@superset-ui/embedded-sdk";

class JmixSupersetDashboard extends ThemableMixin(ElementMixin(PolymerElement)) {

    static get template() {
        return html`
            <style>
                #dashboard {
                    width: 100%;
                    height: 100%;
                }

                #dashboard iframe {
                    width: 100%;
                    height: 100%;

                    border: none;
                }
            </style>

            <div id="dashboard"/>
        `;
    }

    static get is() {
        return 'jmix-superset-dashboard';
    }

    static get properties() {
        return {
            guestToken: {
                type: String,
                value: '',
            },
            embeddedId: {
                type: String,
                value: '',
            },
            supersetDomain: {
                type: String,
                value: '',
            },
            titleVisibility: {
                type: Boolean,
                value: false,
            },
            tabVisibility: {
                type: Boolean,
                value: false,
            },
            chartControlsVisibility: {
                type: Boolean,
                value: false,
            },
            filtersVisibility: {
                type: Boolean,
                value: false,
            },
            filtersExpanded: {
                type: Boolean,
                value: true,
            },
            /**
             * @protected
             */
            _userInfo: {
                type: Object,
                value: {
                    username: "undefined",
                }
            },
            /**
             * @protected
             */
            _dataConstraints: {
                type: Object,
                value: [],
            },
            /**
             * @protected
             */
            _accessToken: {
                type: String,
                value: '',
            },
            /**
             * @protected
             */
            _supersetDomain: {
                type: String,
                value: '',
            }
        }
    }

    updateDashboard() {
        const embedDashboardInternal = async () => {
            await embedDashboard({
                id: this.embeddedId, // given by the Superset embedding UI
                supersetDomain: this.getDomain(),
                // @ts-ignore
                mountPoint: this.$.dashboard, // html element in which iframe render
                fetchGuestToken: () => this.getGuestToken(),
                dashboardUiConfig: {
                    hideTitle: !this.titleVisibility,
                    hideChartControls: !this.chartControlsVisibility,
                    hideTab: !this.tabVisibility,
                    filters: {
                        visible: this.filtersVisibility,
                        expanded: this.filtersExpanded
                    }
                },
            })
        };
        embedDashboardInternal();
    }

    getGuestToken = async () => {
        // Use custom guest token if is set
        if (this.guestToken) {
            return this.guestToken;
        }

        const response = await fetch(this.getDomain() + '/api/v1/security/guest_token/', {
            method: 'post',
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + this._accessToken
            },
            body: JSON.stringify({
                "resources": [
                    {
                        "id": this.embeddedId,
                        "type": "dashboard"
                    }
                ],
                "rls": this._dataConstraints,
                "user": this._userInfo,
            }),
            credentials: "include",
        });
        const responseJson = await response.json();
        return responseJson.token;
    }

    getDomain() {
        return this.supersetDomain ? this.supersetDomain : this._supersetDomain;
    }

    _isReadyToEmbed() {

    }
}

customElements.define(JmixSupersetDashboard.is, JmixSupersetDashboard);