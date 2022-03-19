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

import io.jmix.ui.widget.client.resizabletextarea.JmixResizableTextAreaWrapperServerRpc;
import io.jmix.ui.widget.client.resizabletextarea.JmixResizableTextAreaWrapperState;
import io.jmix.ui.widget.client.resizabletextarea.ResizeDirection;
import com.vaadin.server.AbstractErrorMessage;
import com.vaadin.server.CompositeErrorMessage;
import com.vaadin.server.ErrorMessage;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class JmixResizableTextAreaWrapper extends CustomField {

    protected ResizeListener resizeListener = null;

    public interface ResizeListener {
        void onResize(String oldWidth, String oldHeight, String width, String height);
    }

    @SuppressWarnings("UnusedAssignment")
    protected JmixTextArea textArea = null;

    public JmixResizableTextAreaWrapper(JmixTextArea txtArea) {
        this.textArea = txtArea;

        setWidthUndefined();
        setFocusDelegate(textArea);

        JmixResizableTextAreaWrapperServerRpc rpc = new JmixResizableTextAreaWrapperServerRpc() {
            String oldWidth;
            String oldHeight;

            @Override
            public void sizeChanged(String width, String height) {
                if (StringUtils.isEmpty(oldWidth)) {
                    oldWidth = (int) getWidth() + getWidthUnits().getSymbol();
                }
                if (StringUtils.isEmpty(oldHeight)) {
                    oldHeight = ((int) getHeight()) + getHeightUnits().getSymbol();
                }

                setWidth(width);
                setHeight(height);

                String prevWidth = oldWidth;
                String prevHeight = oldHeight;

                oldWidth = width;
                oldHeight = height;

                if (resizeListener != null) {
                    resizeListener.onResize(prevWidth, prevHeight, width, height);
                }
            }

            @Override
            public void textChanged(String text) {
                if (!textArea.isReadOnly()) {
                    textArea.setValue(text);
                }
            }
        };
        registerRpc(rpc);
    }

    @Override
    public ErrorMessage getErrorMessage() {
        ErrorMessage superError = super.getErrorMessage();
        if (!textArea.isReadOnly() && isRequiredIndicatorVisible() && textArea.isEmpty()) {
            ErrorMessage error = AbstractErrorMessage.getErrorMessageForException(
                    new com.vaadin.v7.data.Validator.EmptyValueException(getRequiredError()));
            if (error != null) {
                return new CompositeErrorMessage(superError, error);
            }
        }

        return superError;
    }

    @Override
    protected Component initContent() {
        return textArea;
    }

    public boolean isEditable() {
        return !super.isReadOnly();
    }

    public void setEditable(boolean editable) {
        super.setReadOnly(!editable);
        textArea.setReadOnly(!editable);
    }

    @Override
    protected void doSetValue(Object value) {
        // do nothing
    }

    @Nullable
    @Override
    public Object getValue() {
        return null;
    }

    @Override
    protected JmixResizableTextAreaWrapperState getState() {
        return (JmixResizableTextAreaWrapperState) super.getState();
    }

    @Override
    protected JmixResizableTextAreaWrapperState getState(boolean markAsDirty) {
        return (JmixResizableTextAreaWrapperState) super.getState(markAsDirty);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        super.setRequiredIndicatorVisible(visible);
        textArea.setRequiredIndicatorVisible(visible);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);

        if (textArea != null) {
            if (width < 0) {
                textArea.setWidthUndefined();
            } else {
                textArea.setWidth("100%");
            }
        }
    }

    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);

        if (textArea != null) {
            if (height < 0) {
                textArea.setHeightUndefined();
            } else {
                textArea.setHeight("100%");
            }
        }
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (getState(false).resizableDirection.equals(ResizeDirection.BOTH)
                && isPercentageSize()) {
            LoggerFactory.getLogger(JmixResizableTextAreaWrapper.class).warn(
                    "TextArea with percentage size can not be resizable");
            getState().resizableDirection = ResizeDirection.NONE;
        } else if (getState(false).resizableDirection.equals(ResizeDirection.VERTICAL)
                && Unit.PERCENTAGE.equals(getHeightUnits())) {
            LoggerFactory.getLogger(JmixResizableTextAreaWrapper.class).warn(
                    "TextArea height with percentage size can not be resizable to vertical direction");
            getState().resizableDirection = ResizeDirection.NONE;
        } else if (getState(false).resizableDirection.equals(ResizeDirection.HORIZONTAL)
                && (Unit.PERCENTAGE.equals(getWidthUnits()))) {
            LoggerFactory.getLogger(JmixResizableTextAreaWrapper.class).warn(
                    "TextArea width with percentage size can not be resizable to horizontal direction");
            getState().resizableDirection = ResizeDirection.NONE;
        }
    }

    protected boolean isPercentageSize() {
        return Unit.PERCENTAGE.equals(getHeightUnits()) || Unit.PERCENTAGE.equals(getWidthUnits());
    }

    public ResizeListener getResizeListener() {
        return resizeListener;
    }

    public void setResizeListener(ResizeListener resizeListener) {
        this.resizeListener = resizeListener;
    }

    public void setResizableDirection(ResizeDirection direction) {
        getState().resizableDirection = direction;
    }

    public ResizeDirection getResizableDirection() {
        return getState(false).resizableDirection;
    }
}