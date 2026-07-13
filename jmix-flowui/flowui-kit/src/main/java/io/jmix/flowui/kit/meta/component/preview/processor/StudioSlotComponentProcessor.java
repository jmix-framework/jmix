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

package io.jmix.flowui.kit.meta.component.preview.processor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasSuffix;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewSlotProcessor;

/**
 * Studio preview processor for slot-hinted attachment points that a parent+child type pair alone
 * cannot disambiguate: {@link HasPrefix}/{@link HasSuffix} single-component slots and
 * {@link AppLayout} navbar/drawer/content slots.
 */
public class StudioSlotComponentProcessor implements StudioPreviewSlotProcessor {

    static final String PREFIX = "prefix";
    static final String SUFFIX = "suffix";
    static final String NAVBAR = "navbar";
    static final String DRAWER = "drawer";
    static final String CONTENT = "content";

    @Override
    public boolean addToSlot(Component parent, Component child, int index, String slotHint) {
        if (parent instanceof HasPrefix hasPrefix && PREFIX.equals(slotHint)) {
            hasPrefix.setPrefixComponent(child);
            return true;
        }
        if (parent instanceof HasSuffix hasSuffix && SUFFIX.equals(slotHint)) {
            hasSuffix.setSuffixComponent(child);
            return true;
        }
        if (parent instanceof AppLayout appLayout) {
            if (NAVBAR.equals(slotHint)) {
                appLayout.addToNavbar(child);
                return true;
            }
            if (DRAWER.equals(slotHint)) {
                appLayout.addToDrawer(child);
                return true;
            }
            if (CONTENT.equals(slotHint)) {
                appLayout.setContent(child);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeFromSlot(Component parent, Component child, String slotHint) {
        if (parent instanceof HasPrefix hasPrefix && PREFIX.equals(slotHint)) {
            hasPrefix.setPrefixComponent(null);
            return true;
        }
        if (parent instanceof HasSuffix hasSuffix && SUFFIX.equals(slotHint)) {
            hasSuffix.setSuffixComponent(null);
            return true;
        }
        // AppLayout#remove(Component...) is slot-agnostic in the real Vaadin API (unlike the add side,
        // which needs addToNavbar/addToDrawer/setContent), so any slotHint is accepted here.
        if (parent instanceof AppLayout appLayout) {
            appLayout.remove(child);
            return true;
        }
        return false;
    }
}
