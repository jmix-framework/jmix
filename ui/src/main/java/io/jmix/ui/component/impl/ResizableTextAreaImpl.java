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

import io.jmix.core.common.event.Subscription;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.component.ResizableTextArea;
import io.jmix.ui.widget.JmixResizableTextAreaWrapper;
import io.jmix.ui.widget.JmixTextArea;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Component;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ResizableTextAreaImpl<V> extends AbstractTextArea<JmixTextArea, V>
        implements ResizableTextArea<V>, InitializingBean {

    protected JmixResizableTextAreaWrapper wrapper;

    public ResizableTextAreaImpl() {
        component = createComponent();
        attachValueChangeListener(component);

        wrapper = new JmixResizableTextAreaWrapper(component);
        wrapper.setResizeListener(this::onResize);
    }

    @Override
    protected void componentValueChanged(String prevComponentValue, String newComponentValue, boolean isUserOriginated) {
        wrapper.markAsDirty();

        super.componentValueChanged(prevComponentValue, newComponentValue, isUserOriginated);
    }

    protected JmixTextArea createComponent() {
        return new JmixTextArea() {
            @Override
            public void setComponentError(ErrorMessage componentError) {
                if (componentError instanceof UserError) {
                    super.setComponentError(componentError);
                } else {
                    wrapper.setComponentError(componentError);
                }
            }
        };
    }

    @Override
    public void afterPropertiesSet() {
        initComponent(component);
    }

    protected void initComponent(JmixTextArea component) {
        component.setValueChangeMode(ValueChangeMode.BLUR);
    }

    @Override
    public Component getComposition() {
        return wrapper;
    }

    @Nullable
    @Override
    public String getCaption() {
        return wrapper.getCaption();
    }

    @Override
    public void setCaption(@Nullable String caption) {
        wrapper.setCaption(caption);
    }

    @Override
    public boolean isCaptionAsHtml() {
        return wrapper.isCaptionAsHtml();
    }

    @Override
    public void setCaptionAsHtml(boolean captionAsHtml) {
        wrapper.setCaptionAsHtml(captionAsHtml);
    }

    @Nullable
    @Override
    public String getDescription() {
        return wrapper.getDescription();
    }

    @Override
    public void setDescription(@Nullable String description) {
        wrapper.setDescription(description);
    }

    @Override
    public boolean isRequired() {
        return wrapper.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequired(boolean required) {
        wrapper.setRequiredIndicatorVisible(required);
    }

    @Override
    protected void setEditableToComponent(boolean editable) {
        wrapper.setEditable(editable);
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return wrapper.getRequiredError();
    }

    @Override
    public void setRequiredMessage(@Nullable String msg) {
        wrapper.setRequiredError(msg);
    }

    @Override
    public CaseConversion getCaseConversion() {
        return CaseConversion.valueOf(component.getCaseConversion().name());
    }

    @Override
    public void setCaseConversion(CaseConversion caseConversion) {
        io.jmix.ui.widget.CaseConversion widgetCaseConversion =
                io.jmix.ui.widget.CaseConversion.valueOf(caseConversion.name());
        component.setCaseConversion(widgetCaseConversion);
    }

    @Override
    public ResizeDirection getResizableDirection() {
        return WrapperUtils.toResizeDirection(wrapper.getResizableDirection());
    }

    @Override
    public Subscription addResizeListener(Consumer<ResizeEvent> listener) {
        return getEventHub().subscribe(ResizeEvent.class, listener);
    }

    @Override
    public void setResizableDirection(ResizeDirection direction) {
        Preconditions.checkNotNullArgument(direction);
        wrapper.setResizableDirection(WrapperUtils.toVaadinResizeDirection(direction));
    }

    protected void onResize(String oldWidth, String oldHeight, String width, String height) {
        ResizeEvent e = new ResizeEvent(this, oldWidth, width, oldHeight, height);
        publish(ResizeEvent.class, e);
    }
}
