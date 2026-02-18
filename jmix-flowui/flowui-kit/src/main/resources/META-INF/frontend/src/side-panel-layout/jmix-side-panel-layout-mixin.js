/*
 * Copyright 2026 Haulmont.
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

export const JmixSidePanelLayoutMixin = (superClass) =>
    class JmixSidePanelLayoutMixin extends superClass {

    static get properties() {
        return {
            sidePanelOpened: {
                type: Boolean,
                value: false,
                reflectToAttribute: true,
                notify: true,
                observer: '_sidePanelOpenedChanged',
                sync: true,
            },
            sidePanelPosition: {
                type: String,
                reflectToAttribute: true,
                value: 'right',
                notify: true,
                observer: '_sidePanelPositionChanged',
                sync: true,
            },
            sidePanelMode: {
                type: String,
                reflectToAttribute: true,
                value: 'overlay',
                notify: true,
                observer: '_sidePanelModeChanged',
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
            closeOnOutsideClick: {
                type: Boolean,
                value: true,
                notify: true,
                sync: true,
            },
            displayAsOverlayOnSmallDevices: {
                type: Boolean,
                value: true,
                notify: true,
                sync: true,
            },
            overlayAriaLabel: {
                type: String,
                notify: true,
                sync: true,
            },
            sidePanelHorizontalSize: {
                type: String,
                notify: true,
                sync: true,
            },
            sidePanelHorizontalMinSize: {
                type: String,
                notify: true,
                sync: true,
            },
            sidePanelHorizontalMaxSize: {
                type: String,
                notify: true,
                sync: true,
            },
            sidePanelVerticalSize: {
                type: String,
                notify: true,
                sync: true,
            },
            sidePanelVerticalMinSize: {
                type: String,
                notify: true,
                sync: true,
            },
            sidePanelVerticalMaxSize: {
                type: String,
                notify: true,
                sync: true,
            },
            _modalityCurtainHidden: {
                type: Boolean,
                value: true,
            },
            _displayAsOverlayMediaQuery: {
                type: String,
                value: '(max-width: 600px), (max-height: 600px)',
            },
            _displayAsOverlay: {
                type: Boolean,
                value: false,
                observer: '_displayAsOverlayChanged',
            },
        };
    }

    static get observers() {
        return [
            '_updateSidePanelHorizontalSizes(sidePanelHorizontalSize, sidePanelHorizontalMinSize, sidePanelHorizontalMaxSize)',
            '_updateSidePanelVerticalSizes(sidePanelVerticalSize, sidePanelVerticalMinSize, sidePanelVerticalMaxSize)',
        ];
    }

    ready() {
        super.ready();

        this.modalityCurtain = this.$.modalityCurtain;
        this.modalityCurtain.addEventListener('click', (e) => this._onModalityCurtainClick(e));

        this.$.dialog.$.overlay.addEventListener('vaadin-overlay-outside-click', (e) => this._closeSidePanel());
        this.$.dialog.$.overlay.addEventListener('vaadin-overlay-escape-press', (e) => this._closeSidePanel());

        this.$.sidePanel.addEventListener('transitionstart', (e) => this._onSidePanelTransitionStart(e));
        this.$.sidePanel.addEventListener('transitionend', (e) => this._onSidePanelTransitionEnd(e));

        // Update the modality curtain, because component can be reattached to UI.
        this._updateModalityCurtainHidden();
    }

    /**
     * Focuses the specified component. If the side panel is animating, the focus will be set after
     * the animation is finished.
     *
     * @param {HTMLElement} focusComponent
     */
    focusComponent(focusComponent) {
        if (!focusComponent || this._contentController.getActualNodes().includes(focusComponent)) {
            return
        }

        if (!this.animating) {
           focusComponent.focus();
        } else {
            const checkInterval = setInterval(() => {
                if (!this.animating && this.sidePanelOpened) {
                    focusComponent.focus();
                    clearInterval(checkInterval);
                }
            }, 20);
            setTimeout(() => clearInterval(checkInterval), this._getSidePanelTransition());
        }
    }

    _onSidePanelTransitionStart(e) {
        if (e.target !== this.$.sidePanel && e.propertyName !== 'transform') {
            return;
        }

        this.animating = true;
    }

    _onSidePanelTransitionEnd(e) {
        if (e.target !== this.$.sidePanel && e.propertyName !== 'transform') {
            return;
        }

        this.animating = false;

        if (this.sidePanelOpened) {
            this.dispatchEvent(new CustomEvent('jmix-side-panel-layout-after-open-event'));
        }
    }

    /**
     * Observer for {@code sidePanelMode} property.
     *
     * @private
     */
    _sidePanelModeChanged(sidePanelMode) {
        this._updateContentSize();
    }

    /**
     * Observer for {@code sidePanelOpened} property.
     *
     * @private
     */
    _sidePanelOpenedChanged(opened, oldOpened) {
        this._updateModalityCurtainHidden();
        this._updateContentSize();

        if (opened) {
            this._moveSidePanelChildren();
        }
    }

    /**
     * Observer for {@code modal} property.
     *
     * @private
     */
    _modalChanged() {
        this._updateModalityCurtainHidden();
    }

    /**
     * Observer for {@code sidePanelPosition} property.
     *
     * @private
     */
    _sidePanelPositionChanged() {
        this._updateSidePanelSizes();
    }

    _updateModalityCurtainHidden() {
        if (!this.modalityCurtain) {
            return;
        }

        if (this._curtainHideTimeout) {
            clearTimeout(this._curtainHideTimeout);
            this._curtainHideTimeout = null;
        }

        const shouldBeHidden = this._computeModalityCurtainHidden(this.sidePanelOpened, this.modal);

        if (shouldBeHidden && !this._modalityCurtainHidden) {
            // The side panel is closing, so hide the curtain after a delay
            this._curtainHideTimeout = setTimeout(() => {
                this._modalityCurtainHidden = true;
                this._curtainHideTimeout = null;
            }, this._getSidePanelTransition());
        } else if (!shouldBeHidden) {
            // Display curtain immediately
            this._modalityCurtainHidden = false;
        }
    }

    _updateSidePanelSizes() {
        this._updateSidePanelHorizontalSizes(this.sidePanelHorizontalSize, this.sidePanelHorizontalMinSize,
                this.sidePanelHorizontalMaxSize);
        this._updateSidePanelVerticalSizes(this.sidePanelVerticalSize, this.sidePanelVerticalMinSize,
                this.sidePanelVerticalMaxSize);
    }

    _updateSidePanelHorizontalSizes(size, minSize, maxSize) {
        if (this.sidePanelPosition !== 'right'
                && this.sidePanelPosition !== 'left'
                && this.sidePanelPosition !== 'inline-start'
                && this.sidePanelPosition !== 'inline-end') {
            this.$.sidePanel.style.removeProperty('width');
            this.$.sidePanel.style.removeProperty('min-width');
            this.$.sidePanel.style.removeProperty('max-width');
            return;
        }

        if (size) {
            this.$.sidePanel.style.setProperty('width', size);
        } else {
            this.$.sidePanel.style.removeProperty('width');
        }

        if (minSize) {
            this.$.sidePanel.style.setProperty('min-width', minSize);
        } else {
            this.$.sidePanel.style.removeProperty('min-width');
        }

        if (maxSize) {
            this.$.sidePanel.style.setProperty('max-width', maxSize);
        } else {
            this.$.sidePanel.style.removeProperty('max-width');
        }
    }

    _updateSidePanelVerticalSizes(size, minSize, maxSize) {
        if (this.sidePanelPosition !== 'top' && this.sidePanelPosition !== 'bottom') {
            this.$.sidePanel.style.removeProperty('height');
            this.$.sidePanel.style.removeProperty('min-height');
            this.$.sidePanel.style.removeProperty('max-height');
            return;
        }

        if (size) {
            this.$.sidePanel.style.setProperty('height', size);
        } else {
            this.$.sidePanel.style.removeProperty('height');
        }

        if (minSize) {
            this.$.sidePanel.style.setProperty('min-height', minSize);
        } else {
            this.$.sidePanel.style.removeProperty('min-height');
        }

        if (maxSize) {
            this.$.sidePanel.style.setProperty('max-height', maxSize);
        } else {
            this.$.sidePanel.style.removeProperty('max-height');
        }
    }

    _updateContentSize() {
        if (this.sidePanelMode == 'overlay') {
            this.$.content.style.maxWidth = '100%';
            this.$.content.style.maxHeight = '100%';
            return;
        }
        if (this.sidePanelPosition === 'left'
                || this.sidePanelPosition === 'right'
                || this.sidePanelPosition === 'inline-start'
                || this.sidePanelPosition === 'inline-end') {
            let realWidth = this.$.sidePanel.getBoundingClientRect().width + 'px';
            if (this.sidePanelOpened) {
                this.$.content.style.setProperty('--_jmix-side-panel-horizontal-size', realWidth);
                this.$.content.style.maxWidth = 'calc(100% - var(--_jmix-side-panel-horizontal-size))';

                // Clear height if sidePanelPosition changed when side panel is opened
                this.$.content.style.maxHeight = '100%';
                this.$.content.style.setProperty('--_jmix-side-panel-vertical-size', '');
            } else {
                this.$.content.style.maxWidth = '100%';
                this.$.content.style.setProperty('--_jmix-side-panel-horizontal-size', '');
            }
        }
        if (this.sidePanelPosition === 'top' || this.sidePanelPosition === 'bottom') {
            let realHeight = this.$.sidePanel.getBoundingClientRect().height + 'px';
            if (this.sidePanelOpened) {
                this.$.content.style.setProperty('--_jmix-side-panel-vertical-size', realHeight);
                this.$.content.style.maxHeight = 'calc(100% - var(--_jmix-side-panel-vertical-size))';

                // Clear width if sidePanelPosition changed when side panel is opened
                this.$.content.style.maxWidth = '100%';
                this.$.content.style.setProperty('--_jmix-side-panel-horizontal-size', '');
            } else {
                this.$.content.style.maxHeight = '100%';
                this.$.content.style.setProperty('--_jmix-side-panel-vertical-size', '');
            }
        }
    }

    /**
     * Returns whether the modality curtain should be hidden.
     *
     * @protected
     */
    _computeModalityCurtainHidden(sidePanelOpened, modal) {
        return !sidePanelOpened || !modal;
    }

    /**
     * Returns the value of a custom CSS property.
     *
     * @protected
     */
    _onModalityCurtainClick(e) {
        this.dispatchEvent(new CustomEvent('jmix-side-panel-layout-modality-curtain-click-event', { detail: { originalEvent: e} }));

        if (this.closeOnOutsideClick) {
            this.sidePanelOpened = false;
        }
    }

    /**
     * Closes the side panel.
     *
     * @protected
     */
    _closeSidePanel() {
        this.sidePanelOpened = false;
    }

    /**
     * Returns the value of a custom CSS property.
     *
     * @private
     */
    _getSidePanelTransition() {
       const transition = this._getStylePropertyValue('--_transition-duration');
       if (transition === 'none') {
           return 0;
       }
       // Transition duration is in ms (e.g.)
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
    _displayAsOverlayChanged(fullscreen, oldFullscreen) {
        this._moveSidePanelChildren();
    }

    /**
     * Returns true if the dialog should be opened.
     *
     * @private
     */
    _computeDialogOpened(opened, displayAsOverlay) {
      return displayAsOverlay ? opened : false;
    }

    /**
     * Moves the side panel children to the dialog or component depending on how the side panel is displayed.
     *
     * @private
     */
    _moveSidePanelChildren() {
      if (this._displayAsOverlay) {
        // Move to dialog
        this._moveSidePanelChildrenTo(this.$.dialog.$.overlay);
      } else {
        // Move to component
        this._moveSidePanelChildrenTo(this);
      }
    }

    /**
     * Moves the side panel children to the target element (dialog or component).
     *
     * @private
     */
    _moveSidePanelChildrenTo(target) {
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
     * @param existingChildren the existing children of the side panel layout
     *
     * @private
     */
    _updateControllers(...existingChildren) {
        if (!existingChildren || !this._displayAsOverlay || !this._contentController) {
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
        if (this.sidePanelOpened && removedElements.length > 0) {
            for (const element of removedElements) {
                this.$.dialog.$.overlay.removeChild(element);
            }
        }
    }
}
