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

import com.google.common.base.Strings;
import com.vaadin.data.provider.DataGenerator;
import com.vaadin.ui.TwinColSelect;
import elemental.json.JsonObject;
import io.jmix.ui.widget.client.twincolselect.JmixTwinColSelectState;

import javax.annotation.Nullable;
import java.util.function.Function;

@SuppressWarnings("serial")
public class JmixTwinColSelect<V> extends TwinColSelect<V> {

    protected Function<? super V, String> optionStyleProvider;

    public JmixTwinColSelect() {
        addDataGenerator(createDataGenerator());
    }

    public void setOptionStyleProvider(@Nullable Function<? super V, String> optionStyleProvider) {
        this.optionStyleProvider = optionStyleProvider;
        refreshDataItems();
    }

    protected DataGenerator<V> createDataGenerator() {
        return new TwinColumnDataGenerator();
    }

    public boolean isAddAllBtnEnabled() {
        return getState(false).addAllBtnEnabled;
    }

    public void setAddAllBtnEnabled(boolean addAllBtnEnabled) {
        if (isAddAllBtnEnabled() != addAllBtnEnabled) {
            getState(true).addAllBtnEnabled = addAllBtnEnabled;
        }
    }

    public boolean isReorderable() {
        return getState(false).reorderable;
    }

    public void setReorderable(boolean reorderable) {
        if (isReorderable() != reorderable) {
            getState(true).reorderable = reorderable;
        }
    }

    @Override
    protected JmixTwinColSelectState getState() {
        return (JmixTwinColSelectState) super.getState();
    }

    @Override
    protected JmixTwinColSelectState getState(boolean markAsDirty) {
        return (JmixTwinColSelectState) super.getState(markAsDirty);
    }

    protected void refreshDataItems() {
        getDataProvider().refreshAll();
    }

    protected class TwinColumnDataGenerator implements DataGenerator<V> {

        @Override
        public void generateData(V item, JsonObject jsonObject) {
            if (optionStyleProvider != null) {
                String style = optionStyleProvider.apply(item);
                if (!Strings.isNullOrEmpty(style)) {
                    jsonObject.put("style", style);
                }
            }
        }
    }

    public interface JmixOptionStyleProvider<V> {

        String getStyleName(V item, boolean selected);
    }
}