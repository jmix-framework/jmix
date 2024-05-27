/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.kit.component.richtexteditor;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.JsonSerializer;
import elemental.json.JsonObject;

import java.io.Serializable;
import java.util.Objects;

@Tag("jmix-rich-text-editor")
@JsModule("./src/rich-text-editor/jmix-rich-text-editor.js")
public class JmixRichTextEditor extends AbstractSinglePropertyField<JmixRichTextEditor, String>
        implements HasSize, HasTheme, HasLabel, HasHelper, HasThemeVariant<RichTextEditorVariant>,
        HasAriaLabel, HasValueChangeMode, KeyNotifier, InputNotifier, CompositionNotifier {

    protected static final String PROPERTY_NAME = "htmlValue";

    protected ValueChangeMode currentMode;

    protected RichTextEditorI18n i18n;

    public JmixRichTextEditor() {
        super(PROPERTY_NAME, "", String.class,
                JmixRichTextEditor::presentationToModel,
                JmixRichTextEditor::modelToPresentation);

        initComponent();
    }

    protected void initComponent() {
        setPresentationValue("");
        setValueChangeMode(ValueChangeMode.ON_CHANGE);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // htmlValue property is not writeable and will not be automatically
        // initialized on the client-side element. Instead, call set
        // presentation value to run the necessary JS for initializing the
        // client-side element
        setPresentationValue(getValue());
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        String presentationValue = modelToPresentation(newPresentationValue);
        getElement().setProperty(PROPERTY_NAME, presentationValue);
        // htmlValue property is not writeable, HTML value needs to be set using
        // method exposed by web component instead
        getElement().callJsFunction("setHtmlValueInternal", presentationValue);
    }

    protected String presentationToModel(String htmlValue) {
        // Sanitize HTML coming from client
        return sanitize(htmlValue);
    }

    protected String modelToPresentation(String htmlValue) {
        // Sanitize HTML sent to client
        return sanitize(htmlValue);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default value is {@link ValueChangeMode#ON_CHANGE}.
     */
    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        setSynchronizedEvent(ValueChangeMode.eventForMode(valueChangeMode, "html-value-changed"));
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the object content that is gotten from this method will
     * not update the lang on the component if not set back using
     * {@link JmixRichTextEditor#setI18n(RichTextEditorI18n)}
     *
     * @return the i18n object. It will be {@code null}, If the i18n properties weren't set.
     */
    public RichTextEditorI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n the internationalized properties, not {@code null}
     */
    public void setI18n(RichTextEditorI18n i18n) {
        Objects.requireNonNull(i18n, "The I18N properties object should not be null");
        this.i18n = i18n;

        runBeforeClientResponse(ui -> {
            if (i18n == this.i18n) {
                JsonObject i18nObject = (JsonObject) JsonSerializer.toJson(this.i18n);
                for (String key : i18nObject.keys()) {
                    getElement().executeJs("this.set('i18n." + key + "', $0)", i18nObject.get(key));
                }

                getElement().callJsFunction("applyLocalization");
            }
        });
    }

    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    protected String sanitize(String html) {
        var settings = new org.jsoup.nodes.Document.OutputSettings();
        settings.prettyPrint(false);
        return org.jsoup.Jsoup.clean(html, "",
                org.jsoup.safety.Safelist.basic()
                        .addTags("img", "h1", "h2", "h3", "s")
                        .addAttributes("img", "align", "alt", "height", "src",
                                "title", "width")
                        .addAttributes(":all", "style")
                        .addProtocols("img", "src", "data"),
                settings);
    }


    /**
     * The internationalization properties for {@link JmixRichTextEditor}.
     */
    public static class RichTextEditorI18n implements Serializable {
        protected String bold;
        protected String italic;
        protected String underline;
        protected String strike;

        protected String h1;
        protected String h2;
        protected String h3;

        protected String subscript;
        protected String superscript;

        protected String listOrdered;
        protected String listBullet;

        protected String alignStart;
        protected String alignCenter;
        protected String alignEnd;
        protected String alignJustify;

        protected String image;
        protected String link;

        protected String blockquote;
        protected String codeBlock;

        protected String clean;

        /**
         * Gets the translated word for {@code bold}
         *
         * @return the translated word for bold
         */
        public String getBold() {
            return bold;
        }

        /**
         * Sets the translated word for {@code bold}.
         *
         * @param bold the translated word for bold
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setBold(String bold) {
            this.bold = bold;
            return this;
        }

        /**
         * Gets the translated word for {@code italic}
         *
         * @return the translated word for italic
         */
        public String getItalic() {
            return italic;
        }

        /**
         * Sets the translated word for {@code italic}.
         *
         * @param italic the translated word for italic
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setItalic(String italic) {
            this.italic = italic;
            return this;
        }

        /**
         * Gets the translated word for {@code underline}
         *
         * @return the translated word for underline
         */
        public String getUnderline() {
            return underline;
        }

        /**
         * Sets the translated word for {@code underline}.
         *
         * @param underline the translated word for underline
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setUnderline(String underline) {
            this.underline = underline;
            return this;
        }

        /**
         * Gets the translated word for {@code strike}
         *
         * @return the translated word for strike
         */
        public String getStrike() {
            return strike;
        }

        /**
         * Sets the translated word for {@code strike}.
         *
         * @param strike the translated word for strike
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setStrike(String strike) {
            this.strike = strike;
            return this;
        }

        /**
         * Gets the translated word for {@code h1}
         *
         * @return the translated word for h1
         */
        public String getH1() {
            return h1;
        }

        /**
         * Sets the translated word for {@code h1}.
         *
         * @param h1 the translated word for h1
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setH1(String h1) {
            this.h1 = h1;
            return this;
        }

        /**
         * Gets the translated word for {@code h2}
         *
         * @return the translated word for h2
         */
        public String getH2() {
            return h2;
        }

        /**
         * Sets the translated word for {@code h2}.
         *
         * @param h2 the translated word for h2
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setH2(String h2) {
            this.h2 = h2;
            return this;
        }

        /**
         * Gets the translated word for {@code h3}
         *
         * @return the translated word for h3
         */
        public String getH3() {
            return h3;
        }

        /**
         * Sets the translated word for {@code h3}.
         *
         * @param h3 the translated word for h3
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setH3(String h3) {
            this.h3 = h3;
            return this;
        }

        /**
         * Gets the translated word for {@code subscript}
         *
         * @return the translated word for subscript
         */
        public String getSubscript() {
            return subscript;
        }

        /**
         * Sets the translated word for {@code subscript}.
         *
         * @param subscript the translated word for subscript
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setSubscript(String subscript) {
            this.subscript = subscript;
            return this;
        }

        /**
         * Gets the translated word for {@code superscript}
         *
         * @return the translated word for superscript
         */
        public String getSuperscript() {
            return superscript;
        }

        /**
         * Sets the translated word for {@code superscript}.
         *
         * @param superscript the translated word for superscript
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setSuperscript(String superscript) {
            this.superscript = superscript;
            return this;
        }

        /**
         * Gets the translated word for {@code listOrdered}
         *
         * @return the translated word for listOrdered
         */
        public String getListOrdered() {
            return listOrdered;
        }

        /**
         * Sets the translated word for {@code listOrdered}.
         *
         * @param listOrdered the translated word for listOrdered
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setListOrdered(String listOrdered) {
            this.listOrdered = listOrdered;
            return this;
        }

        /**
         * Gets the translated word for {@code listBullet}
         *
         * @return the translated word for listBullet
         */
        public String getListBullet() {
            return listBullet;
        }

        /**
         * Sets the translated word for {@code listBullet}.
         *
         * @param listBullet the translated word for listBullet
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setListBullet(String listBullet) {
            this.listBullet = listBullet;
            return this;
        }

        /**
         * Gets the translated word for {@code alignStart}
         *
         * @return the translated word for alignStart
         */
        public String getAlignStart() {
            return alignStart;
        }

        /**
         * Sets the translated word for {@code alignStart}.
         *
         * @param alignStart the translated word for alignStart
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setAlignStart(String alignStart) {
            this.alignStart = alignStart;
            return this;
        }

        /**
         * Gets the translated word for {@code alignCenter}
         *
         * @return the translated word for alignCenter
         */
        public String getAlignCenter() {
            return alignCenter;
        }

        /**
         * Sets the translated word for {@code alignCenter}.
         *
         * @param alignCenter the translated word for alignCenter
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setAlignCenter(String alignCenter) {
            this.alignCenter = alignCenter;
            return this;
        }

        /**
         * Gets the translated word for {@code alignEnd}
         *
         * @return the translated word for alignEnd
         */
        public String getAlignEnd() {
            return alignEnd;
        }

        /**
         * Sets the translated word for {@code alignEnd}.
         *
         * @param alignEnd the translated word for alignEnd
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setAlignEnd(String alignEnd) {
            this.alignEnd = alignEnd;
            return this;
        }

        /**
         * Gets the translated word for {@code alignJustify}
         *
         * @return the translated word for alignJustify
         */
        public String getAlignJustify() {
            return alignJustify;
        }

        /**
         * Sets the translated word for {@code alignJustify}.
         *
         * @param alignJustify the translated word for alignJustify
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setAlignJustify(String alignJustify) {
            this.alignJustify = alignJustify;
            return this;
        }

        /**
         * Gets the translated word for {@code image}
         *
         * @return the translated word for image
         */
        public String getImage() {
            return image;
        }

        /**
         * Sets the translated word for {@code image}.
         *
         * @param image the translated word for image
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setImage(String image) {
            this.image = image;
            return this;
        }

        /**
         * Gets the translated word for {@code link}
         *
         * @return the translated word for link
         */
        public String getLink() {
            return link;
        }

        /**
         * Sets the translated word for {@code link}.
         *
         * @param link the translated word for link
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setLink(String link) {
            this.link = link;
            return this;
        }

        /**
         * Gets the translated word for {@code blockquote}
         *
         * @return the translated word for blockquote
         */
        public String getBlockquote() {
            return blockquote;
        }

        /**
         * Sets the translated word for {@code blockquote}.
         *
         * @param blockquote the translated word for blockquote
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setBlockquote(String blockquote) {
            this.blockquote = blockquote;
            return this;
        }

        /**
         * Gets the translated word for {@code codeBlock}
         *
         * @return the translated word for codeBlock
         */
        public String getCodeBlock() {
            return codeBlock;
        }

        /**
         * Sets the translated word for {@code codeBlock}.
         *
         * @param codeBlock the translated word for codeBlock
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setCodeBlock(String codeBlock) {
            this.codeBlock = codeBlock;
            return this;
        }

        /**
         * Gets the translated word for {@code clean}
         *
         * @return the translated word for clean
         */
        public String getClean() {
            return clean;
        }

        /**
         * Sets the translated word for {@code clean}.
         *
         * @param clean the translated word for clean
         * @return this instance for method chaining
         */
        public RichTextEditorI18n setClean(String clean) {
            this.clean = clean;
            return this;
        }

        /**
         * Gets the stringified values of the tooltips.
         *
         * @return stringified values of the tooltips
         */
        @Override
        public String toString() {
            return "[" +
                    bold + ", " + italic + ", " + underline + ", " + strike + ", " +
                    h1 + ", " + h2 + ", " + h3 + ", " +
                    subscript + ", " + superscript + ", " +
                    listOrdered + ", " + listBullet + ", " +
                    alignStart + ", " + alignCenter + ", " + alignEnd + ", " + alignJustify + ", " +
                    image + ", " + link + ", " +
                    blockquote + ", " + codeBlock + ", " + clean
                    + "]";
        }
    }
}
