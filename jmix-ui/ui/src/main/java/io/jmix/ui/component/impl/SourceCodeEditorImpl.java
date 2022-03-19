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

import com.google.common.base.Strings;
import com.vaadin.server.ClientConnector;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.AppUI;
import io.jmix.ui.component.HighlightMode;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.autocomplete.AutoCompleteSupport;
import io.jmix.ui.component.autocomplete.Suggester;
import io.jmix.ui.widget.JmixSourceCodeEditor;
import io.jmix.ui.widget.addon.aceeditor.AceEditor;
import io.jmix.ui.widget.addon.aceeditor.AceMode;
import io.jmix.ui.widget.addon.aceeditor.Suggestion;
import io.jmix.ui.widget.addon.aceeditor.SuggestionExtension;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SourceCodeEditorImpl extends AbstractField<JmixSourceCodeEditor, String, String>
        implements SourceCodeEditor {

    protected boolean suggestOnDot = true;

    protected HighlightMode mode = Mode.Text;
    protected Suggester suggester;
    protected SuggestionExtension suggestionExtension;

    protected AutoCompleteSupport autoCompleteSupport;

    public SourceCodeEditorImpl() {
        component = createComponent();
        initComponent(component);

        autoCompleteSupport = createAutoCompleteSupport();

        attachValueChangeListener(component);
    }

    protected JmixSourceCodeEditor createComponent() {
        return new JmixSourceCodeEditor();
    }

    protected AutoCompleteSupport createAutoCompleteSupport() {
        return new AutoCompleteSupport() {
            @Override
            public int getCursorPosition() {
                return component.getCursorPosition();
            }

            @Override
            public Object getValue() {
                return component.getValue();
            }
        };
    }

    protected void initComponent(JmixSourceCodeEditor component) {
        component.setMode(AceMode.text);
        component.addAttachListener(this::handleAttach);
    }

    protected void handleAttach(ClientConnector.AttachEvent attachEvent) {
        AceEditor component = (AceEditor) attachEvent.getSource();
        AppUI appUi = (AppUI) component.getUI();
        if (appUi == null) {
            return;
        }

        String acePath = appUi.getWebJarPath("ace-builds", "ace.js");
        String path = appUi.translateToWebPath(acePath.substring(0, acePath.lastIndexOf("/"))) + "/";

        component.setBasePath(path);
        component.setThemePath(path);
        component.setWorkerPath(path);
        component.setModePath(path);
    }

    @Override
    public HighlightMode getMode() {
        return mode;
    }

    @Override
    public void setMode(HighlightMode mode) {
        Preconditions.checkNotNullArgument(mode, "HighlightMode of SourceCodeEditor cannot be null");

        this.mode = mode;

        AceMode editorMode = AceMode.text;

        for (AceMode aceMode : AceMode.values()) {
            if (aceMode.name().equals(mode.getId())) {
                editorMode = aceMode;
                break;
            }
        }

        component.setMode(editorMode);
    }

    @Override
    public void resetEditHistory() {
        component.resetEditHistory();
    }

    @Nullable
    @Override
    public Suggester getSuggester() {
        return suggester;
    }

    @Override
    public void setSuggester(@Nullable Suggester suggester) {
        this.suggester = suggester;

        if (suggester != null && suggestionExtension == null) {
            suggestionExtension = new SuggestionExtension(new SourceCodeEditorSuggester());
            suggestionExtension.extend(component);
            suggestionExtension.setShowDescriptions(false);
            suggestionExtension.setSuggestOnDot(suggestOnDot);
        }
    }

    @Override
    public AutoCompleteSupport getAutoCompleteSupport() {
        return autoCompleteSupport;
    }

    @Override
    public void setShowGutter(boolean showGutter) {
        component.setShowGutter(showGutter);
    }

    @Override
    public boolean isShowGutter() {
        return component.isShowGutter();
    }

    @Override
    public void setShowPrintMargin(boolean showPrintMargin) {
        component.setShowPrintMargin(showPrintMargin);
    }

    @Override
    public boolean isShowPrintMargin() {
        return component.isShowPrintMargin();
    }

    @Override
    public void setPrintMarginColumn(int printMarginColumn) {
        component.setPrintMarginColumn(printMarginColumn);
    }

    @Override
    public int getPrintMarginColumn() {
        return component.getPrintMarginColumn();
    }

    @Override
    public void setHighlightActiveLine(boolean highlightActiveLine) {
        component.setHighlightActiveLine(highlightActiveLine);
    }

    @Override
    public boolean isHighlightActiveLine() {
        return component.isHighlightActiveLine();
    }

    @Override
    public void setHandleTabKey(boolean handleTabKey) {
        component.setHandleTabKey(handleTabKey);
    }

    @Override
    public boolean isHandleTabKey() {
        return component.isHandleTabKey();
    }

    @Nullable
    @Override
    public String getValue() {
        String value = super.getValue();
        return Strings.emptyToNull(value);
    }

    @Override
    public String getRawValue() {
        return component.getValue();
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }

    @Override
    public boolean isSuggestOnDot() {
        return suggestOnDot;
    }

    @Override
    public void setSuggestOnDot(boolean suggest) {
        this.suggestOnDot = suggest;

        if (suggestionExtension != null) {
            suggestionExtension.setSuggestOnDot(suggestOnDot);
        }
    }

    protected class SourceCodeEditorSuggester implements io.jmix.ui.widget.addon.aceeditor.Suggester {
        @Override
        public List<Suggestion> getSuggestions(String text, int cursor) {
            if (suggester == null) {
                return Collections.emptyList();
            }

            List<io.jmix.ui.component.autocomplete.Suggestion> suggestions =
                    suggester.getSuggestions(getAutoCompleteSupport(), text, cursor);
            List<Suggestion> vSuggestions = new ArrayList<>();
            for (io.jmix.ui.component.autocomplete.Suggestion s : suggestions) {
                vSuggestions.add(new Suggestion(s.getDisplayText(), "", s.getValueText(),
                        s.getStartPosition(), s.getEndPosition()));
            }

            return vSuggestions;
        }

        @Override
        public String applySuggestion(Suggestion suggestion, String text, int cursor) {
            String suggestionText = suggestion.getSuggestionText();

            int startPosition = suggestion.getStartPosition();
            int endPosition = suggestion.getEndPosition();

            int start = startPosition > -1 ? Math.min(startPosition, text.length()) : cursor;
            int end = endPosition > -1 ? Math.min(endPosition, text.length()) : cursor;

            String leftText = StringUtils.substring(text, 0, start);
            String rightText = StringUtils.substring(text, end, text.length());

            return leftText + suggestionText + rightText;
        }
    }
}
