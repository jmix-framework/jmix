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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.EntityLinkField;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.impl.EntityLinkFieldImpl;

@Deprecated
public class WebEntityLinkField<V> extends EntityLinkFieldImpl<V> implements EntityLinkField<V> {

    protected OpenType screenOpenType = OpenType.THIS_TAB;

    @Override
    public OpenType getScreenOpenType() {
        return screenOpenType;
    }

    @Override
    public void setScreenOpenType(OpenType screenOpenType) {
        Preconditions.checkNotNullArgument(screenOpenType);

        this.screenOpenType = screenOpenType;
        this.screenOpenMode = screenOpenType.getOpenMode();
    }
}
