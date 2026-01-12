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

export const JmixDrawerLayoutMixin = (superClass) =>
    class JmixDrawerLayoutMixin extends superClass {

    static get properties() {
        return {
            drawerOpened: {
                type: Boolean,
                value: false,
                reflectToAttribute: true,
                notify: true,
                observer: '_drawerOpenedChanged',
                sync: true,
            },
            drawerPlacement: {
                type: String,
                reflectToAttribute: true,
                value: 'right',
                notify: true,
                sync: true,
            },
            drawerMode: {
                type: String,
                reflectToAttribute: true,
                value: 'overlay',
                notify: true,
                observer: '_drawerModeChanged',
                sync: true,
            },
            modal: {
                type: Boolean,
                reflectToAttribute: true,
                value: true,
                notify: true,
                observer: '_modalChanged',
                sync: true,
            },
            closeOnModalityCurtainClick: {
                type: Boolean,
                value: true,
                notify: true,
                sync: true,
            },
            displayOverlayOnSmallScreens: {
                type: Boolean,
                value: true,
                notify: true,
                sync: true,
            },
            _modalityCurtainHidden: {
                type: Boolean,
                value: true,
            },
            fullscreenOnSmallDevice: {
                type: Boolean,
                value: true,
            },
            _fullscreenMediaQuery: {
                type: String,
                value: '(max-width: 600px), (max-height: 600px)', /* TODO: pinyazhin why OR and 600px? */
            },
            _fullscreen: {
                type: Boolean,
                value: false,
                observer: '_fullscreenChanged',
            },
        };
    }

    ready() {
        super.ready();

        this.modalityCurtain = this.$.modalityCurtain;
        this.modalityCurtain.addEventListener('click', (e) => this._onModalityCurtainClick(e));

        this.$.dialog.$.overlay.addEventListener('vaadin-overlay-outside-click', (e) => this._closeDrawer());
        this.$.dialog.$.overlay.addEventListener('vaadin-overlay-escape-press', (e) => this._closeDrawer());

        this._curtainHideTimeout = null;

        // Update the modality curtain, because component can be reattached to UI.
        this._updateModalityCurtainHidden();
    }

      _drawerModeChanged(drawerMode) {
          this._updateContentAnimation();
      }

    _drawerOpenedChanged(opened, oldOpened) {
        this._updateModalityCurtainHidden();
        this._updateContentAnimation();

        if (!opened && oldOpened) {
            this._closeDrawer();
        }

        if (opened) {
            this._moveDrawerChildren();

            // TODO: pinyazhin focus fields or drawer itself?
            // When using bottom aside editor position,
             // auto-focus the editor element on open.
             /*if (this._form.parentElement === this) {
               this.$.editor.setAttribute('tabindex', '0');
               this.$.editor.focus();
             } else {
               this.$.editor.removeAttribute('tabindex');
             }*/
        }
    }

    _modalChanged(mask, oldMask) {
        this._updateModalityCurtainHidden();
    }

    _updateModalityCurtainHidden() {
        if (!this.modalityCurtain) {
            return;
        }

        if (this._curtainHideTimeout) {
            clearTimeout(this._curtainHideTimeout);
            this._curtainHideTimeout = null;
        }

        const shouldBeHidden = this._computeModalityCurtainHidden(this.drawerOpened, this.modal);

        if (shouldBeHidden && !this._modalityCurtainHidden) {
            const transitionDuration = this._getDrawerLayoutTransition();

            // The drawer is closing, so hide the curtain after a delay
            this._curtainHideTimeout = setTimeout(() => {
                this._modalityCurtainHidden = true;
                this._curtainHideTimeout = null;
            }, transitionDuration);
        } else if (!shouldBeHidden) {
            // Display curtain immediately
            this._modalityCurtainHidden = false;
        }
    }

    _updateContentAnimation() {
        if (this.drawerMode == 'overlay') {
            this.$.content.style.maxWidth = '100%';
            this.$.content.style.maxHeight = '100%';
            return;
        }
        if (this.drawerPlacement === 'left'
                || this.drawerPlacement === 'right'
                || this.drawerPlacement === 'inline-start'
                || this.drawerPlacement === 'inline-end') {
            let realWidth = this.$.drawer.getBoundingClientRect().width + 'px';
            if (this.drawerOpened) {
                this.$.content.style.setProperty('--_jmix-drawer-layout-drawer-horizontal-size', realWidth);
                this.$.content.style.maxWidth = 'calc(100% - var(--_jmix-drawer-layout-drawer-horizontal-size))';

                // Clear height if drawerPlacement changed when drawer is opened
                this.$.content.style.maxHeight = '100%';
                this.$.content.style.setProperty('--_jmix-drawer-layout-drawer-vertical-size', '');
            } else {
                this.$.content.style.maxWidth = '100%';
                this.$.content.style.setProperty('--_jmix-drawer-layout-drawer-horizontal-size', '');
            }
        }
        if (this.drawerPlacement === 'top' || this.drawerPlacement === 'bottom') {
            let realHeight = this.$.drawer.getBoundingClientRect().height + 'px';
            if (this.drawerOpened) {
                this.$.content.style.setProperty('--_jmix-drawer-layout-drawer-vertical-size', realHeight);
                this.$.content.style.maxHeight = 'calc(100% - var(--_jmix-drawer-layout-drawer-vertical-size))';

                // Clear width if drawerPlacement changed when drawer is opened
                this.$.content.style.maxWidth = '100%';
                this.$.content.style.setProperty('--_jmix-drawer-layout-drawer-horizontal-size', '');
            } else {
                this.$.content.style.maxHeight = '100%';
                this.$.content.style.setProperty('--_jmix-drawer-layout-drawer-vertical-size', '');
            }
        }
    }

    /**
     * Returns whether the modality curtain should be hidden.
     *
     * @protected
     */
    _computeModalityCurtainHidden(drawerOpened, modal) {
        return !drawerOpened || !modal;
    }

    /**
     * Returns the value of a custom CSS property.
     *
     * @protected
     */
    _onModalityCurtainClick(e) {
        if (this.closeOnModalityCurtainClick) {
            this.drawerOpened = false;
        }

        this.dispatchEvent(new CustomEvent('jmix-drawer-layout-modality-curtain-click', { detail: { originalEvent: e} }));
    }

    /**
     * Closes the drawer.
     *
     * @protected
     */
    _closeDrawer() {
        this.drawerOpened = false;
    }

    /**
     * Returns the value of a custom CSS property.
     *
     * @private
     */
    _getDrawerLayoutTransition() {
       const transition = this._getStylePropertyValue('--jmix-drawer-layout-transition');
       if (transition.length === 0) {
           return 0;
       }
       if (transition.endsWith('ms')) {
           return parseInt(transition.slice(0, transition.length - 'ms'.length), 10);
       }
       return parseInt(transition, 10);
    }

    /**
     * Returns the value of a custom CSS property.
     *
     * @private
     */
    _getStylePropertyValue(property) {
      const customPropertyValue = getComputedStyle(this).getPropertyValue(property);
      return (customPropertyValue || '').trim().toLowerCase();
    }

    /**
     * Observer for fullscreen property.
     *
     * @private
     */
    _fullscreenChanged(fullscreen, oldFullscreen) {
        this._moveDrawerChildren();
    }

    /**
     * Returns true if the dialog should be opened.
     *
     * @private
     */
    _computeDialogOpened(opened, fullscreen) {
      return fullscreen ? opened : false;
    }

    /**
     * Decides where to move the drawer children: dialog or component.
     *
     * @private
     */
    _moveDrawerChildren() {
      if (this._fullscreen) {
        // Move to dialog
        this._moveDrawerChildrenTo(this.$.dialog.$.overlay);
      } else {
        // Move to component
        this._moveDrawerChildrenTo(this);
      }
    }

    /**
     * Moves the drawer children to the target element: dialog or component.
     *
     * @private
     */
    _moveDrawerChildrenTo(target) {
      // If the component is not fully initialized
      if (!this._contentController) {
          return;
      }

      const contents = this._contentController.getActualNodes();
      const nodes = [...contents];

      if (!nodes.every((node) => node instanceof HTMLElement)) {
        return;
      }

      this._contentController.suspendRemovingActualNodes();

      [...nodes].forEach((node) => {
        target.appendChild(node);
      });

      // Wait for the nodes to be moved.
      setTimeout(() => {
          this._contentController.resumeRemovingActualNodes();
      })
    }

    /**
     * Server callable function.
     *
     * Updates the controllers to remove any elements that are no longer in the DOM.
     * @param existingChildren the existing children of the drawer layout
     *
     * @private
     */
    _updateControllers(...existingChildren) {
        if (!existingChildren || !this._fullscreen || !this._contentController) {
            return;
        }

        const removedElements = [];

        this._contentController.getActualNodes().forEach((element) => {
            if (existingChildren.indexOf(element) === -1) {
                this._contentController.removeActualNode(element);
                removedElements.push(element);
            }
        });

        // Update dialog overlay if opened
        if (this.drawerOpened && removedElements.length > 0) {
            for (const element of removedElements) {
                this.$.dialog.$.overlay.removeChild(element);
            }
        }
    }
}
