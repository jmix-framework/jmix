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

package com.haulmont.cuba.gui.screen.compatibility;

import com.haulmont.cuba.gui.components.ExpandingLayout;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.HasWindowManager;
import com.haulmont.cuba.gui.data.DsContext;
import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.ActionsHolder;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.FrameContext;
import io.jmix.ui.component.HasMargin;
import io.jmix.ui.component.HasSpacing;
import io.jmix.ui.component.OrderedContainer;
import io.jmix.ui.component.Validatable;
import io.jmix.ui.component.ValidationException;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.List;

public interface LegacyFrame extends
        Component.HasXmlDescriptor,
        Component.HasCaption,
        Component.HasIcon,
        Component.Wrapper,
        Component.BelongToFrame,
        Frame.Wrapper,
        ActionsHolder,
        HasSpacing,
        HasMargin,
        OrderedContainer,
        ExpandingLayout,
        HasWindowManager {

    @Nullable
    <T> T getCompanion();

    FrameContext getContext();

    String getMessagesPack();
    void setMessagesPack(String name);

    boolean validateAll();

    boolean validate(List<Validatable> fields);

    void validate() throws ValidationException;

    boolean isValid();

    @Override
    default Element getXmlDescriptor() {
        return ((Component.HasXmlDescriptor) getWrappedFrame()).getXmlDescriptor();
    }

    @Override
    default void setXmlDescriptor(Element element) {
        ((Component.HasXmlDescriptor) getWrappedFrame()).setXmlDescriptor(element);
    }

    /**
     * @return {@link DsContext} of the current frame or window
     */
    DsContext getDsContext();

    /** INTERNAL. Don't call from application code. */
    @Internal
    void setDsContext(DsContext dsContext);
}
