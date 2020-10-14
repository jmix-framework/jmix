/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.impl;

import com.vaadin.ui.CssLayout;
import io.jmix.ui.component.mainwindow.Drawer;
import org.springframework.beans.factory.InitializingBean;

public class DrawerImpl extends CssLayoutImpl implements Drawer, InitializingBean {

    protected static final String DRAWER_STYLENAME = "jmix-drawer";
    protected static final String COLLAPSED_SYLENAME = "collapsed";
    protected static final String EXPAND_ON_HOVER_SYLENAME = "expand-on-hover";

    protected boolean collapsed = false;
    protected boolean expandOnHover = false;

    public DrawerImpl() {
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(CssLayout component) {
        component.setPrimaryStyleName(DRAWER_STYLENAME);
    }

    @Override
    public void collapse() {
        component.addStyleName(COLLAPSED_SYLENAME);
        this.collapsed = true;
    }

    @Override
    public void expand() {
        component.removeStyleName(COLLAPSED_SYLENAME);
        this.collapsed = false;
    }

    @Override
    public boolean isExpandOnHover() {
        return expandOnHover;
    }

    @Override
    public void setExpandOnHover(boolean expandOnHover) {
        this.expandOnHover = expandOnHover;
        if (expandOnHover) {
            component.addStyleName(EXPAND_ON_HOVER_SYLENAME);
        } else {
            component.removeStyleName(EXPAND_ON_HOVER_SYLENAME);
        }
    }

    @Override
    public void toggle() {
        if (this.collapsed) {
            expand();
        } else {
            collapse();
        }
    }

    @Override
    public boolean isCollapsed() {
        return this.collapsed;
    }
}
