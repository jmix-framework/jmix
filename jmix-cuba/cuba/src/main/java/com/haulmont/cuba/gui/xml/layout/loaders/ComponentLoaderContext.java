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

package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.data.DsContext;
import io.jmix.ui.screen.ScreenOptions;

public class ComponentLoaderContext extends io.jmix.ui.xml.layout.loader.ComponentLoaderContext {

    protected DsContext dsContext;

    public ComponentLoaderContext(ScreenOptions options) {
        super(options);
    }

    public DsContext getDsContext() {
        return dsContext;
    }

    public void setDsContext(DsContext dsContext) {
        this.dsContext = dsContext;
    }
}
