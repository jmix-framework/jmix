/*
 * Copyright 2019 Haulmont.
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

import io.jmix.ui.component.BrowserFrame;
import io.jmix.ui.widget.JmixBrowserFrame;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class BrowserFrameImpl extends AbstractResourceView<JmixBrowserFrame> implements BrowserFrame {

    public BrowserFrameImpl() {
        component = createComponent();
    }

    protected JmixBrowserFrame createComponent() {
        return new JmixBrowserFrame();
    }

    @Nullable
    public String getSandbox() {
        return component.getSandbox();
    }

    @Override
    public void setSandbox(@Nullable String value) {
        component.setSandbox(value);
    }

    @Override
    public void setSandbox(Sandbox sandbox) {
        component.setSandbox(sandbox.getValue());
    }

    @Override
    public void setSandbox(@Nullable EnumSet<Sandbox> sandboxSet) {
        if (sandboxSet != null) {
            component.setSandbox(sandboxSet.stream()
                    .map(Sandbox::getValue)
                    .collect(Collectors.joining(" ")));
        } else {
            component.setSandbox(null);
        }
    }

    @Override
    public void setSrcdoc(@Nullable String value) {
        component.setSrcdoc(value);
    }

    @Nullable
    @Override
    public String getSrcdoc() {
        return component.getSrcdoc();
    }

    @Override
    public void setAllow(@Nullable String value) {
        component.setAllow(value);
    }

    @Override
    public void setAllow(Allow allow) {
        component.setAllow(allow.getValue());
    }

    @Override
    public void setAllow(@Nullable EnumSet<Allow> allowSet) {
        if (allowSet != null) {
            component.setAllow(allowSet.stream()
                    .map(Allow::getValue)
                    .collect(Collectors.joining(" ")));
        } else {
            component.setAllow(null);
        }
    }

    @Nullable
    @Override
    public String getAllow() {
        return component.getAllow();
    }

    @Nullable
    @Override
    public String getReferrerPolicy() {
        return component.getReferrerPolicy();
    }

    @Override
    public void setReferrerPolicy(@Nullable String value) {
        component.setReferrerPolicy(value);
    }

    @Override
    public void setReferrerPolicy(ReferrerPolicy referrerPolicy) {
        component.setReferrerPolicy(referrerPolicy.getValue());
    }
}
