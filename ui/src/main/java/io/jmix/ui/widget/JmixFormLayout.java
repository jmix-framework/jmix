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

import com.vaadin.ui.GridLayout;
import io.jmix.ui.widget.client.fieldgrouplayout.CaptionAlignment;
import io.jmix.ui.widget.client.fieldgrouplayout.JmixFieldGroupLayoutState;

import java.util.HashMap;
import java.util.Map;

public class JmixFormLayout extends GridLayout {

    protected static final String INLINE_CAPTION_STYLENAME = "inline-caption";

    protected Map<Integer, Integer> columnFieldCaptionWidth = null;
    protected Map<Integer, CaptionAlignment> columnCaptionAlignments = null;

    public JmixFormLayout() {
        setHideEmptyRowsAndColumns(true);
        setSpacing(true);
        updateStyleName();
    }

    @Override
    protected JmixFieldGroupLayoutState getState() {
        return (JmixFieldGroupLayoutState) super.getState();
    }

    @Override
    protected JmixFieldGroupLayoutState getState(boolean markAsDirty) {
        return (JmixFieldGroupLayoutState) super.getState(markAsDirty);
    }

    public int getFixedCaptionWidth() {
        return getState(false).fieldCaptionWidth;
    }

    public void setFixedCaptionWidth(int fixedCaptionWidth) {
        if (getState(false).fieldCaptionWidth != fixedCaptionWidth) {
            getState().fieldCaptionWidth = fixedCaptionWidth;
        }
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        if (initial) {
            if (columnFieldCaptionWidth != null) {
                int[] newColumnFieldCaptionWidth = new int[getColumns()];
                for (Map.Entry<Integer, Integer> entry : columnFieldCaptionWidth.entrySet()) {
                    int index = entry.getKey();
                    int width = entry.getValue();

                    if (index >= 0 && index < getColumns() && width > 0) {
                        newColumnFieldCaptionWidth[index] = width;
                    }
                }
                getState().columnFieldCaptionWidth = newColumnFieldCaptionWidth;
            }

            if (columnCaptionAlignments != null) {
                CaptionAlignment[] newColumnCaptionAlignments = new CaptionAlignment[getColumns()];
                for (Map.Entry<Integer, CaptionAlignment> entry : columnCaptionAlignments.entrySet()) {
                    int index = entry.getKey();
                    CaptionAlignment alignment = entry.getValue();

                    if (index >= 0
                            && index <= getColumns()
                            && alignment != null) {
                        newColumnCaptionAlignments[index] = alignment;
                    }
                }
                getState().columnsCaptionAlignments = newColumnCaptionAlignments;
            }
        }
    }

    public int getFieldCaptionWidth(int column) {
        if (columnFieldCaptionWidth == null) {
            return -1;
        }

        Integer value = columnFieldCaptionWidth.get(column);
        return value != null ? value : -1;
    }

    public void setFieldCaptionWidth(int column, int width) {
        if (columnFieldCaptionWidth == null) {
            columnFieldCaptionWidth = new HashMap<>();
        }
        columnFieldCaptionWidth.put(column, width);
    }

    public boolean isUseInlineCaption() {
        return getState(false).useInlineCaption;
    }

    public void setUseInlineCaption(boolean useInlineCaption) {
        if (getState(false).useInlineCaption != useInlineCaption) {
            getState().useInlineCaption = useInlineCaption;

            updateStyleName();
        }
    }

    protected void updateStyleName() {
        if (getState(false).useInlineCaption) {
            addStyleName(INLINE_CAPTION_STYLENAME);
        } else {
            removeStyleName(INLINE_CAPTION_STYLENAME);
        }
    }

    public CaptionAlignment getColumnCaptionAlignment() {
        return getState(false).columnsCaptionAlignment;
    }

    public void setColumnCaptionAlignment(CaptionAlignment alignment) {
        if (getState(false).columnsCaptionAlignment != alignment) {
            getState().columnsCaptionAlignment = alignment;
        }
    }

    public CaptionAlignment getColumnCaptionAlignment(int column) {
        if (columnCaptionAlignments == null) {
            return getColumnCaptionAlignment();
        }

        CaptionAlignment alignment = columnCaptionAlignments.get(column);
        return alignment != null ? alignment : getColumnCaptionAlignment();
    }

    public void setColumnCaptionAlignment(int column, CaptionAlignment alignment) {
        if (columnCaptionAlignments == null) {
            columnCaptionAlignments = new HashMap<>();
        }

        columnCaptionAlignments.put(column, alignment);
    }
}
