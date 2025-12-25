import { html, PolymerElement } from '@polymer/polymer/polymer-element.js';
import { defineCustomElement } from '@vaadin/component-base/src/define.js';
import { ElementMixin } from '@vaadin/component-base/src/element-mixin.js';
import { registerStyles, ThemableMixin } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';
import { ControllerMixin } from '@vaadin/component-base/src/controller-mixin.js';
import { MediaQueryController } from '@vaadin/component-base/src/media-query-controller.js';
import { JmixDrawerLayoutSlotController } from './jmix-drawer-layout-slot-controller.js';
import './jmix-drawer-layout-dialog.js';

import { drawerLayoutStyles } from './jmix-drawer-layout-styles.js';

registerStyles('jmix-drawer-layout', drawerLayoutStyles, { moduleId: 'jmix-drawer-layout-styles' });

class JmixDrawerLayout extends ElementMixin(ControllerMixin(ThemableMixin(PolymerElement))) {
  static get template() {
    return html`
      <div id="layout" part="layout">
          <div  id="contentScroller" part="contentScroller">
              <div id="content" part="content">
                  <slot name="contentSlot"></slot>
              </div>
          </div>
          <div id="modalityCurtain" part="modalityCurtain" hidden$="[[_modalityCurtainHidden]]"></div>
          <div id="drawer" part="drawer" hidden$="[[!drawerOpened]]">
               <div id="drawerHeader" part="drawerHeader" hidden>
                    <slot id="drawerHeaderSlot" name="drawerHeaderSlot"></slot>
               </div>
               <div id="drawerScroller" part="drawerScroller">
                   <div id="drawerContent" part="drawerContent">
                        <slot name="drawerContentSlot"></slot>
                   </div>
               </div>
               <div id="drawerFooter" part="drawerFooter" hidden>
                    <slot id="drawerFooterSlot" name="drawerFooterSlot"></slot>
               </div>
          </div>
      </div>

      <jmix-drawer-layout-dialog
              id="dialog"
              opened="[[__computeDialogOpened(drawerOpened, _fullscreen)]]"
              fullscreen="[[_fullscreen]]"
              aria-label="[[__dialogAriaLabel]]"
              no-close-on-outside-click="[[__isDirty]]"
              no-close-on-esc="[[__isDirty]]"
              theme$="[[_theme]]"
              on-opened-changed="__onDialogOpened"
            ></jmix-drawer-layout-dialog>
    `;
  }

  static get is() {
    return 'jmix-drawer-layout';
  }

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
          observer: '_drawerPlacementChanged',
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

      this.$.drawerHeaderSlot.addEventListener('slotchange', () => this._onSlotChange(this.$.drawerHeader, this.$.drawerHeaderSlot));
      this.$.drawerFooterSlot.addEventListener('slotchange', () => this._onSlotChange(this.$.drawerFooter, this.$.drawerFooterSlot));

      this.$.dialog.$.overlay.addEventListener('vaadin-overlay-outside-click', (e) => this._closeDrawer());
      this.$.dialog.$.overlay.addEventListener('vaadin-overlay-escape-press', (e) => this._closeDrawer());

      this.addController(
          new MediaQueryController(this._fullscreenMediaQuery, (matches) => {
              this._fullscreen = this.fullscreenOnSmallDevice ? matches : false;
              this.toggleAttribute('fullscreen', this._fullscreen);
          }),
      );

      this._headerController = new JmixDrawerLayoutSlotController(this, 'drawerHeaderSlot');
      this.addController(this._headerController);

      this._contentController = new JmixDrawerLayoutSlotController(this, 'drawerContentSlot');
      this.addController(this._contentController);

      this._footerController = new JmixDrawerLayoutSlotController(this, 'drawerFooterSlot');
      this.addController(this._footerController);

      this._curtainHideTimeout = null;

      // Update the modality curtain, because component can be reattached to UI.
      this._updateModalityCurtainHidden();
  }

  _drawerOpenedChanged(opened, oldOpened) {
      this._updateModalityCurtainHidden();

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

  _drawerPlacementChanged(drawerPlacement) {
      console.log('Drawer placement changed: ' + drawerPlacement);
  }

  _drawerModeChanged(drawerMode) {
      console.log('Drawer mode changed: ' + drawerMode);
  }

  _modalChanged(mask, oldMask) {
      this._updateModalityCurtainHidden();
  }

  _updateModalityCurtainHidden() {
      if (!this.modalityCurtain) {
          return;
      }

      // Use timeouts to correctly display animation when drawer is closed
      // in some cases.
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

  _computeModalityCurtainHidden(drawerOpened, modal) {
      return !drawerOpened || !modal;
  }

  /** @protected */
  _onModalityCurtainClick(e) {
      if (this.closeOnModalityCurtainClick) {
          this.drawerOpened = false;
      }

      this.dispatchEvent(new CustomEvent('jmix-drawer-mask-click', { detail: { originalEvent: e} }));
  }

  /** @private */
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

  /** @private */
  _getStylePropertyValue(property) {
    const customPropertyValue = getComputedStyle(this).getPropertyValue(property);
    return (customPropertyValue || '').trim().toLowerCase();
  }

    /** @private */
    _onSlotChange(slotParent, slot) {
        const hidden = slot.assignedElements().length == 1
                           && slot.assignedElements()[0].classList.contains('jmix-drawer-layout-no-content');
        slotParent.toggleAttribute('hidden', hidden);
    }

    _fullscreenChanged(fullscreen, oldFullscreen) {
        this._moveDrawerChildren();
    }

    /**
      * Closes the drawer.
      *
      * @protected
      */
    _closeDrawer() {
        this.drawerOpened = false;
    }

    /** @private */
    __computeDialogOpened(opened, fullscreen) {
      // Only open dialog when editorPosition is "" or fullscreen is set
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
      if (!this._headerController) {
          return;
      }

      const headers = this._headerController.getActualNodes();
      const contents = this._contentController.getActualNodes();
      const footers = this._footerController.getActualNodes();
      const nodes = [...headers, ...contents, ...footers];

      if (!nodes.every((node) => node instanceof HTMLElement)) {
        return;
      }

      this._headerController.suspendRemovingActualNodes();
      this._contentController.suspendRemovingActualNodes();
      this._footerController.suspendRemovingActualNodes();

      // Move nodes to corresponding slots of the target element.
      [...nodes].forEach((node) => {
        target.appendChild(node);
      });

      // Wait for the nodes to be moved.
      setTimeout(() => {
          this._headerController.resumeRemovingActualNodes();
          this._contentController.resumeRemovingActualNodes();
          this._footerController.resumeRemovingActualNodes();
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
        if (!existingChildren || !this._fullscreen) {
            return;
        }

        const removedElements = [];

        this._headerController.getActualNodes().forEach((element) => {
            if (existingChildren.indexOf(element) === -1) {
                this._headerController.removeActualNode(element);
                removedElements.push(element);
            }
        });
        this._contentController.getActualNodes().forEach((element) => {
            if (existingChildren.indexOf(element) === -1) {
                this._contentController.removeActualNode(element);
                removedElements.push(element);
            }
        });
        this._footerController.getActualNodes().forEach((element) => {
            if (existingChildren.indexOf(element) === -1) {
                this._footerController.removeActualNode(element);
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

defineCustomElement(JmixDrawerLayout);

export { JmixDrawerLayout };