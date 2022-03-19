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

import com.vaadin.v7.ui.OptionGroup;
import io.jmix.ui.widget.client.optiongroup.JmixOptionGroupState;
import io.jmix.ui.widget.client.optiongroup.OptionGroupOrientation;
import io.jmix.ui.widget.compatibility.JmixValueChangeEvent;

import java.util.function.Function;

public class JmixOptionGroup extends OptionGroup {

    protected Function<Object, String> itemCaptionGenerator;

    protected boolean omitValueChange = false;

    public JmixOptionGroup() {
        setValidationVisible(false);
        setShowBufferedSourceException(false);

        this.resetValueToNullOnContainerChange = false;
    }

    public void setValueToComponent(Object value) {
        omitValueChange = true;

        setValue(value, false, true);

        omitValueChange = false;

        fireValueChangeEvent(false);
    }

    @Override
    public String getItemCaption(Object itemId) {
        if (itemCaptionGenerator != null) {
            return itemCaptionGenerator.apply(itemId);
        }
        return super.getItemCaption(itemId);
    }

    @Override
    protected JmixOptionGroupState getState() {
        return (JmixOptionGroupState) super.getState();
    }

    @Override
    protected JmixOptionGroupState getState(boolean markAsDirty) {
        return (JmixOptionGroupState) super.getState(markAsDirty);
    }

    public OptionGroupOrientation getOrientation() {
        return getState(false).orientation;
    }

    public void setOrientation(OptionGroupOrientation orientation) {
        if (orientation != getOrientation()) {
            getState().orientation = orientation;
        }
    }

    public Function<Object, String> getItemCaptionGenerator() {
        return itemCaptionGenerator;
    }

    public void setItemCaptionGenerator(Function<Object, String> itemCaptionGenerator) {
        this.itemCaptionGenerator = itemCaptionGenerator;
    }

    @Override
    protected void fireValueChange(boolean repaintIsNotNeeded) {
        fireValueChangeEvent(true);
        if (!repaintIsNotNeeded) {
            markAsDirty();
        }
    }

    protected void fireValueChangeEvent(boolean userOriginated) {
        if (!omitValueChange) {
            fireEvent(new JmixValueChangeEvent(this, userOriginated));
        }
    }
}
