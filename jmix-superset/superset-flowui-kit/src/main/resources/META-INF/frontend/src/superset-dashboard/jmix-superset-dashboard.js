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
            accessToken : {
                type: String,
                value: ''
            },
            guestToken: {
                type: String,
                value: ''
            },
            embeddedId: {
                type: String,
                value: ''
            },
            supersetDomain: {
                type: String,
                value: ''
            },
            titleVisibility: {
                type: Boolean,
                value: false
            },
            tabVisibility: {
                type: Boolean,
                value: false
            },
            chartControlsVisibility: {
                type: Boolean,
                value: false
            },
            filtersVisibility: {
                type: Boolean,
                value: false
            },
            filtersExpanded: {
                type: Boolean,
                value: true
            }
        }
    }

    ready() {
        super.ready();

        this.updateDashboard();
    }

    updateDashboard() {
        // todo do not load without token?
        if (!this.guestToken) {
            return;
        }

        const embed = async () => {
            await embedDashboard({
                id: this.embeddedId, // given by the Superset embedding UI
                supersetDomain: this.supersetDomain,
                // @ts-ignore
                mountPoint: this.$.dashboard, // html element in which iframe render
                fetchGuestToken: () => this.guestToken,
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
        embed();
    }

    // todo method works, delete hardcoded body
    getToken = async () => {
        const response = await fetch(this.supersetDomain + '/api/v1/security/guest_token/', {
            method: 'post',
            headers: {
                'Accept': '*/*',
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + this.accessToken
            },
            body: JSON.stringify({
                "resources": [
                    {
                        "id": "5defeb1a-dbe9-4999-987d-e88e9a1da89a",
                        "type": "dashboard"
                    }
                ],
                "rls": [],
                "user": {
                    "username": "admin admin"
                }
            }),
            credentials: "include",
        });
        const responseJson = await response.json();
        return responseJson.token;
    }

    static get observers() {
        return [
            '_onGuestTokenPropertyChanged(guestToken)',
        ]
    }

    _onGuestTokenPropertyChanged(guestToken) {
        this.updateDashboard();
    }
}

customElements.define(JmixSupersetDashboard.is, JmixSupersetDashboard);