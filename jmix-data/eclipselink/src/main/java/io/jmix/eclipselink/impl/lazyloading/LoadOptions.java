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

package io.jmix.eclipselink.impl.lazyloading;

import io.jmix.core.constraint.AccessConstraint;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class LoadOptions implements Serializable {
    private static final long serialVersionUID = 7963025798626360359L;

    protected Map<String, Serializable> hints;
    protected transient List<AccessConstraint<?>> accessConstraints;

    public static LoadOptions with() {
        return new LoadOptions();
    }

    public static LoadOptions with(LoadOptions srcOptions) {
        return with()
                .setHints(srcOptions.getHints())
                .setAccessConstraints(srcOptions.getAccessConstraints());
    }

    private LoadOptions() {
    }

    public Map<String, Serializable> getHints() {
        return hints == null ? Collections.emptyMap() : hints;
    }

    public LoadOptions setHints(Map<String, Serializable> hints) {
        this.hints = hints;
        return this;
    }

    public List<AccessConstraint<?>> getAccessConstraints() {
        return accessConstraints == null ? Collections.emptyList() : accessConstraints;
    }

    public LoadOptions setAccessConstraints(List<AccessConstraint<?>> accessConstraints) {
        this.accessConstraints = accessConstraints;
        return this;
    }
}
