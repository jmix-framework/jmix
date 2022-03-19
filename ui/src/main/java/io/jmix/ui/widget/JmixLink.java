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

package io.jmix.ui.widget;

import io.jmix.ui.widget.client.link.JmixLinkState;
import com.vaadin.ui.Link;


public class JmixLink extends Link {

    public void setRel(String rel) {
        getState(false).rel = rel;
    }

    public String getRel() {
        return getState(false).rel;
    }

    @Override
    protected JmixLinkState getState(boolean markAsDirty) {
        return (JmixLinkState) super.getState(markAsDirty);
    }

    @Override
    protected JmixLinkState getState() {
        return (JmixLinkState) super.getState();
    }
}