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

// @ts-ignore
if (import.meta.hot) {
  // @ts-ignore
  const hot = import.meta.hot;

  const isLiveReloadDisabled = () => {
    // Checks if live reload is disabled in the debug window
    return sessionStorage.getItem('vaadin.live-reload.active') === 'false';
  };

  const preventViteReload = (payload: any) => {
    // Changing the path prevents Vite from reloading
    payload.path = '/_fake/path.html';
  };

  let pendingNavigationTo: string | undefined = undefined;

  window.addEventListener('vaadin-router-go', (routerEvent: any) => {
    pendingNavigationTo = routerEvent.detail.pathname + routerEvent.detail.search;
  });
  hot.on('vite:beforeFullReload', (payload: any) => {
    if (isLiveReloadDisabled()) {
      preventViteReload(payload);
    }
    if (pendingNavigationTo) {
      // Force reload with the new URL
      location.href = pendingNavigationTo;
      preventViteReload(payload);
    }
  });
}
