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

package io.jmix.ui.component;

import io.jmix.ui.component.autocomplete.AutoCompleteSupport;
import io.jmix.ui.component.autocomplete.Suggester;
import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

/**
 * Text area component with source code highlighting support.
 */
@StudioComponent(
        caption = "SourceCodeEditor",
        category = "Components",
        xmlElement = "sourceCodeEditor",
        icon = "io/jmix/ui/icon/component/sourceCodeEditor.svg",
        canvasBehaviour = CanvasBehaviour.SOURCE_CODE_EDITOR,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/source-code-editor.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, options = "string"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF),
                @StudioProperty(name = "width", type = PropertyType.SIZE, defaultValue = "300px"),
                @StudioProperty(name = "height", type = PropertyType.SIZE, defaultValue = "200px")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface SourceCodeEditor extends Field<String>, Component.Focusable {
    String NAME = "sourceCodeEditor";

    enum Mode implements HighlightMode {
        Java("java"),
        HTML("html"),
        XML("xml"),
        Groovy("groovy"),
        SQL("sql"),
        JavaScript("javascript"),
        Properties("properties"),
        CSS("css"),
        SCSS("scss"),
        Text("text");

        protected String id;

        Mode(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Mode parse(String name) {
            if (StringUtils.isEmpty(name)) {
                return Text;
            }

            for (Mode mode : values()) {
                if (StringUtils.equalsIgnoreCase(name, mode.name())) {
                    return mode;
                }
            }

            return Text;
        }
    }

    HighlightMode getMode();

    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "Text",
            options = {"Java", "HTML", "CSS", "SCSS", "XML", "Groovy", "SQL", "JavaScript", "Properties", "Text"})
    void setMode(HighlightMode mode);

    @Nullable
    Suggester getSuggester();

    void setSuggester(@Nullable Suggester suggester);

    AutoCompleteSupport getAutoCompleteSupport();

    @StudioProperty(defaultValue = "true")
    void setShowGutter(boolean showGutter);

    boolean isShowGutter();

    @StudioProperty(name = "printMargin", defaultValue = "true")
    void setShowPrintMargin(boolean showPrintMargin);

    boolean isShowPrintMargin();

    /**
     * Set print margin position in symbols
     *
     * @param printMarginColumn print margin position in symbols
     */
    @StudioProperty(defaultValue = "80")
    void setPrintMarginColumn(int printMarginColumn);

    /**
     * @return print margin position in symbols
     */
    int getPrintMarginColumn();

    @StudioProperty(defaultValue = "true")
    void setHighlightActiveLine(boolean highlightActiveLine);

    boolean isHighlightActiveLine();

    /**
     * Enables Tab key handling as tab symbol.
     * If handleTabKey is false then Tab/Shift-Tab key press will change focus to next/previous field.
     */
    @StudioProperty(defaultValue = "true")
    void setHandleTabKey(boolean handleTabKey);

    /**
     * @return if Tab key handling is enabled
     */
    boolean isHandleTabKey();

    @Nullable
    @Override
    String getValue();

    /**
     * Returns a string representation of the value.
     */
    String getRawValue();

    /**
     * Reset the stack of undo/redo redo operations.
     */
    void resetEditHistory();

    /**
     * @return true if SourceCodeEditor suggests options after typing a dot character
     */
    boolean isSuggestOnDot();

    /**
     * Sets whether SourceCodeEditor should suggest options after typing a dot character. Default value is true.
     *
     * @param suggest suggest option
     */
    @StudioProperty(name = "suggestOnDot", defaultValue = "true")
    void setSuggestOnDot(boolean suggest);
}
