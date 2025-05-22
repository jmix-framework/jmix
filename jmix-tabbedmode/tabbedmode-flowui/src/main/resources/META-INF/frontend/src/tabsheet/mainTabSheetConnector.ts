/*
 * Copyright 2025 Haulmont.
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

// @ts-nocheck
import {JmixViewTab} from './jmix-view-tab.js';

window.Vaadin.Flow.mainTabSheetConnector = {}
window.Vaadin.Flow.mainTabSheetConnector.initLazy = (tabsheet) => {
    // Check whether the connector was already initialized for the tabsheet
    if (tabsheet.$connector) {
        return;
    }

    tabsheet._findEventTab = function (event) {
        const path = event.__composedPath || event.composedPath();
        const tabSheetIndex = path.indexOf(tabsheet);

        for (let i = 0; i < tabSheetIndex; i++) {
            if (path[i] instanceof JmixViewTab) {
                const tab = path[i];
                return {tab};
            }
        }

        return {};
    }

    tabsheet.addEventListener('vaadin-context-menu-before-open', function (e) {
        const {tabId} = e.detail;
        tabsheet.$server.updateContextMenuTargetTab(tabId);
    });

    tabsheet.getContextMenuBeforeOpenDetail = function (event) {
        // For `contextmenu` events, we need to access the source event,
        // when using open on click we just use the click event itself
        const sourceEvent = event.detail.sourceEvent || event;
        // const eventContext = tabsheet.getEventContext(sourceEvent);
        const {tab} = tabsheet._findEventTab(sourceEvent);
        const tabId = tab?.id || '';
        return {tabId};
    };

    tabsheet.preventContextMenu = function (event) {
        const isLeftClick = event.type === 'click';
        const {tab} = tabsheet._findEventTab(event);

        return isLeftClick || !(tab instanceof JmixViewTab);
    };
};