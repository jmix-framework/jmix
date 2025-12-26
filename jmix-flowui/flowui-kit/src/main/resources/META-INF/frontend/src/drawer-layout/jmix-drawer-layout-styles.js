import { css } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

// TODO: pinyazhin move lumo to theme
export const drawerLayoutStyles = css`
  :host {
    display: block;
    box-sizing: border-box;

    --jmix-drawer-layout-drawer-height: 12em;
    --jmix-drawer-layout-drawer-width: 20em;

    --jmix-drawer-layout-transition: 200ms;

//    --jmix-drawer-layout-drawer-padding: 0.5em;
    --jmix-drawer-layout-drawer-header-padding: var(--jmix-drawer-layout-drawer-padding, var(--lumo-space-m));

    --jmix-drawer-layout-drawer-content-padding: var(--jmix-drawer-layout-drawer-padding, var(--lumo-space-m));
    --jmix-drawer-layout-drawer-content-gap: var(--lumo-space-m);

    --jmix-drawer-layout-drawer-footer-padding: var(--jmix-drawer-layout-drawer-padding, var(--lumo-space-m));
    --jmix-drawer-layout-drawer-footer-gap: var(--lumo-space-m);
  }

    :host,
    [part='content'] {
      display: flex;
      flex-direction: column;
    }

    [part='content'] {
      flex: 1 1 100%;
    }

    [part='contentScroller'],
    [part='drawerScroller'] {
      display: flex;
      flex-direction: column;
      overflow: auto;
      flex: auto;
    }

    [part='drawerScroller'] {
        height: 100%;
        width: 100%
    }

    [part='contentScroller']  {
        height: 100%;
        box-sizing: border-box;
        transition:
            max-height var(--jmix-drawer-layout-transition),
            max-width var(--jmix-drawer-layout-transition);
    }

    [part='layout'] {
        display: flex;
        height: 100%;
        position: relative;
        overflow: hidden;
    }

    [part='modalityCurtain'] {
        position: absolute;
        height: 100%;
        width: 100%;
    }

    [part='modalityCurtain'][hidden] {
        display: none;
    }

    [part='drawerContent'] {
        box-sizing: border-box;
        height: 100%;
        display: flex;
        gap: var(--jmix-drawer-layout-drawer-content-gap);
        flex-direction: column;
        align-items: flex-start;
        width: 100%;
    }

    :host([theme~='drawerContentPadding']) [part='drawerContent'] {
        padding: var(--jmix-drawer-layout-drawer-content-padding);
    }

  :host([drawer-opened][modal]:not([theme~='blurred-curtain'])) {
      [part='modalityCurtain'] {
          background-color: var(--lumo-shade-20pct);
          animation: var(--jmix-drawer-layout-transition) jmix-modality-curtain-dimmed-enter both;
          will-change: opacity;
      }
  }

  :host(:not([drawer-opened]):not([theme~='blurred-curtain'])[modal]) {
      [part='modalityCurtain']:not([hidden]) {
          background-color: var(--lumo-shade-20pct);
          animation: var(--jmix-drawer-layout-transition) jmix-modality-curtain-dimmed-out both;
          will-change: opacity;
      }
  }

    :host([drawer-opened][modal][theme~='blurred-curtain']) {
        [part='modalityCurtain'] {
            animation: var(--jmix-drawer-layout-transition) jmix-modality-curtain-blur-enter both;
            will-change: backdrop-filter;
        }
    }

    :host(:not([drawer-opened])[modal][theme~='blurred-curtain']) {
        [part='modalityCurtain']:not([hidden]) {
            animation: var(--jmix-drawer-layout-transition) jmix-modality-curtain-blur-out both;
            will-change: backdrop-filter;
        }
    }

  [part='drawer'] {
//    z-index: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    width: 100%;
    outline: none;

    position: absolute;

    transition:
          transform var(--jmix-drawer-layout-transition),
          visibility var(--jmix-drawer-layout-transition);

    background-color: var(--lumo-base-color);

    box-shadow: var(--lumo-box-shadow-m);
    box-sizing: border-box;
  }

  [part='drawer'][hidden] {
    visibility: hidden;
  }

  :host([drawer-opened][fullscreen]) [part='drawer'] {
      visibility: hidden;
  }

  /*
   * Drawer positioning
   */

  :host([drawer-placement='left']) [part='drawer'] {
    transform: translateX(-100%);
    /*
     * Hardcode value because drawerPlacement is a concrete position managed by component and independent from
     * block flow direction.
     */
    left: 0;
    width: var(--jmix-drawer-layout-drawer-width);
  }

  :host([drawer-placement='']) [part='drawer'],
  :host([drawer-placement='right']) [part='drawer'] {
      transform: translateX(100%);
      /*
       * Hardcode value because drawerPlacement is a concrete position managed by component and independent from
       * block flow direction.
       */
      right: 0;
      width: var(--jmix-drawer-layout-drawer-width);
  }

  :host([drawer-placement='top']) [part='drawer'] {
      transform: translateY(-100%);
      /*
       * Hardcode value because drawerPlacement is a concrete position managed by component and independent from
       * block flow direction.
       */
      top: 0;
      height: var(--jmix-drawer-layout-drawer-height);
  }

  :host([drawer-placement='bottom']) [part='drawer'] {
      transform: translateY(100%);
      /*
       * Hardcode value because drawerPlacement is a concrete position managed by component and independent from
       * block flow direction.
       */
      bottom: 0;
      height: var(--jmix-drawer-layout-drawer-height);
  }

    :host([drawer-placement='inline-start']) [part='drawer'] {
        transform: translateX(-100%);
        inset-inline-start: 0;
        width: var(--jmix-drawer-layout-drawer-width);
    }

    :host([drawer-placement='inline-start'][dir='rtl']) [part='drawer'] {
        transform: translateX(100%);
    }

    :host([drawer-placement='inline-end']) [part='drawer'] {
        transform: translateX(100%);
        inset-inline-end: 0;
        width: var(--jmix-drawer-layout-drawer-width);
    }

    :host([drawer-placement='inline-end'][dir='rtl']) [part='drawer'] {
        transform: translateX(-100%);
    }

  :host([drawer-opened]) [part='drawer'] {
      visibility: visible;
      touch-action: manipulation; //  TODO: pinyazhin what it is?
  }

  /*
   * Animation for RIGHT, LEFT drawer
   */


  :host([drawer-opened][drawer-placement='left']) {
      [part='drawer'] {
          border-right: 1px solid var(--lumo-contrast-10pct);
          transform: translateX(0%);
      }
  }

  :host([drawer-placement='left'][drawer-mode='push']) #contentScroller {
      /*
       * Since we change scroller max-width, the scroller shrinks itself to start.
       * When LEFT mode is used, we need to align scroller to end for smooth animation.
       */
      margin-left: auto;
  }

  :host([drawer-opened][drawer-placement='left'][drawer-mode='push']) #contentScroller {
      max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
  }

  :host([drawer-opened][drawer-placement='inline-start']) [part='drawer'] {
      border-inline-end: 1px solid var(--lumo-contrast-10pct);
      transform: translateX(0%);
  }

  :host([drawer-placement='inline-start'][drawer-mode='push']) #contentScroller {
      margin-inline-start: auto;
  }

  :host([drawer-opened][drawer-placement='inline-start'][drawer-mode='push']) #contentScroller {
      max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
  }

    :host([drawer-opened][drawer-placement='inline-end']) [part='drawer'] {
        border-inline-start: 1px solid var(--lumo-contrast-10pct);
        transform: translateX(0%);
    }

    :host([drawer-placement='inline-end'][drawer-mode='push']) #contentScroller {
        margin-inline-end: auto;
    }

    :host([drawer-opened][drawer-placement='inline-end'][drawer-mode='push']) #contentScroller {
        max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
    }

  :host([drawer-opened][drawer-placement='']),
  :host([drawer-opened][drawer-placement='right']) {
      [part='drawer'] {
          border-left: 1px solid var(--lumo-contrast-10pct);
          transform: translateX(0%);
      }
  }

  :host([drawer-placement='right'][drawer-mode='push']) #contentScroller,
  :host([drawer-placement='left'][drawer-mode='push']) #contentScroller,
  :host([drawer-placement='inline-start'][drawer-mode='push']) #contentScroller,
  :host([drawer-placement='inline-end'][drawer-mode='push']) #contentScroller {
      max-width: 100%;
  }

  :host([drawer-opened][drawer-placement='right'][drawer-mode='push']) #contentScroller {
      max-width: calc(100% - var(--jmix-drawer-layout-drawer-width));
  }


  /*
   * Animation for TOP, BOTTOM drawer
   */

  :host([drawer-placement='top'][drawer-mode='push']) #contentScroller,
  :host([drawer-placement='bottom'][drawer-mode='push']) #contentScroller {
      max-height: 100%;
  }

  :host([drawer-placement='top'][drawer-mode='push']) #contentScroller {
      /*
       * Since we change scroller max-height, the scroller shrinks itself to top.
       * When TOP mode is used, we need to align scroller to bottom for smooth animation.
       */
      align-self: end;
  }

  :host([drawer-opened][drawer-placement='top'][drawer-mode='push']) #contentScroller {
      max-height: calc(100% - var(--jmix-drawer-layout-drawer-height));
  }

  :host([drawer-opened][drawer-placement='bottom'][drawer-mode='push']) #contentScroller {
      max-height: calc(100% - var(--jmix-drawer-layout-drawer-height));
  }

  :host([drawer-opened][drawer-placement='top']) {
      [part='drawer'] {
          border-block-end: 1px solid var(--lumo-contrast-10pct);
          height: var(--jmix-drawer-layout-drawer-height);
          width: 100%;
          transform: translateY(0%);
      }
  }

  :host([drawer-opened][drawer-placement='bottom']) {
      [part='drawer'] {
          border-block-start: 1px solid var(--lumo-contrast-10pct);
          height: var(--jmix-drawer-layout-drawer-height);
          width: 100%;
          transform: translateY(0%);
      }
  }

  @keyframes jmix-modality-curtain-dimmed-enter {
      0% {
        opacity: 0;
      }
  }

  @keyframes jmix-modality-curtain-dimmed-out {
      100% {
        opacity: 0;
      }
  }

  @keyframes jmix-modality-curtain-blur-enter {
        0% {
          backdrop-filter: blur(0);
        }
        100% {
          backdrop-filter: blur(1px);
        }
    }

    @keyframes jmix-modality-curtain-blur-out {
        0% {
          backdrop-filter: blur(1px);
        }
        100% {
          backdrop-filter: blur(0);
        }
    }
`;
