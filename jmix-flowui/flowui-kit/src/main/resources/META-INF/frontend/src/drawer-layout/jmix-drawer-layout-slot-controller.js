import { SlotController } from '@vaadin/component-base/src/slot-controller.js';

export class JmixDrawerLayoutSlotController extends SlotController {

    constructor(host, slotName) {
        super(host, slotName, 'div', { multiple: true });

        this.actualNodes = [];
    }

    initCustomNode(node) {
        super.initCustomNode(node);

        if (!this.containsActualNode(node)) {
            this.actualNodes.push(node);
        }

        if (this.host.drawerOpened) {
          this.host._moveDrawerChildren();
        }
    }

    teardownNode(node) {
        super.teardownNode(node);

        if (!this.suspendRemovingNodes) {
            if (this.containsActualNode(node)) {
                this.actualNodes.splice(this.actualNodes.indexOf(node), 1);
            }

            if (node.parentElement) {
                node.parentElement.removeChild(node);
            }
        }
    }

    attachDefaultNode(node) {
        const defaultNode = super.attachDefaultNode(node);
        defaultNode.classList.add('jmix-drawer-layout-no-content');
        return defaultNode;
    }

    getActualNodes() {
      return this.actualNodes;
    }

    containsActualNode(node) {
      return this.actualNodes.indexOf(node) !== -1;
    }

    removeActualNode(node) {
        this.actualNodes.splice(this.actualNodes.indexOf(node), 1);
    }

    suspendRemovingActualNodes() {
      this.suspendRemovingNodes = true;
    }

    resumeRemovingActualNodes() {
      this.suspendRemovingNodes = false;
    }
}
