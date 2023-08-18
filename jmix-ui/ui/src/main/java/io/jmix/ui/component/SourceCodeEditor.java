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
import io.jmix.ui.meta.*;
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

        ABAP("abap"),
        ABC("abc"),
        ActionScript("actionscript"),
        ADA("ada"),
        ApacheConf("apache_conf"),
        AsciiDoc("asciidoc"),
        AssemblyX86("assembly_x86"),
        AutoHot("autohotkey"),
        BatchFile("batchfile"),
        C9SearchResults("c9search"),
        Clojure("clojure"),
        Cobol("cobol"),
        CoffeeScript("coffee"),
        ColdFusion("coldfusion"),
        CSharp("csharp"),
        CSS("css"),
        Curly("curly"),
        CCpp("c_cpp"),
        D("d"),
        Dart("dart"),
        Diff("diff"),
        Django("django"),
        Dot("dot"),
        EJS("ejs"),
        Elixir("elixir"),
        Elm("elm"),
        Erlang("erlang"),
        Forth("forth"),
        FreeMarker("ftl"),
        Glsl("glsl"),
        Go("golang"),
        Groovy("groovy"),
        HAML("haml"),
        Handlebars("handlebars"),
        Haskell("haskell"),
        HaXe("haxe"),
        HTML("html"),
        HTMLCompletions("html_completions"),
        HTMLRuby("html_ruby"),
        INI("ini"),
        Jack("jack"),
        Jade("jade"),
        Java("java"),
        JavaScript("javascript"),
        JSON("json"),
        JSONiq("jsoniq"),
        JSP("jsp"),
        JSX("jsx"),
        Julia("julia"),
        LaTeX("latex"),
        Lean("lean"),
        LESS("less"),
        Liquid("liquid"),
        Lisp("lisp"),
        LiveScript("livescript"),
        LogiQL("logiql"),
        LSL("lsl"),
        Lua("lua"),
        LuaPage("luapage"),
        Lucene("lucene"),
        Makefile("makefile"),
        Markdown("markdown"),
        MATLAB("matlab"),
        MEL("mel"),
        MIPSAssembler("mips_assembler"),
        MUSHCode("mushcode"),
        MUSHCodeHighRules("mushcode_high_rules"),
        MySQL("mysql"),
        Nix("nix"),
        ObjectiveC("objectivec"),
        OCaml("ocaml"),
        Pascal("pascal"),
        Perl("perl"),
        PgSQL("pgsql"),
        PHP("php"),
        PlainText("plain_text"),
        Powershell("powershell"),
        Prolog("prolog"),
        Properties("properties"),
        Protobuf("protobuf"),
        Python("python"),
        R("r"),
        RDoc("rdoc"),
        RHTML("rhtml"),
        Ruby("ruby"),
        Rust("rust"),
        SASS("sass"),
        SCAD("scad"),
        Scala("scala"),
        Scheme("scheme"),
        SCSS("scss"),
        SH("sh"),
        SJS("sjs"),
        Snippets("snippets"),
        SoyTemplate("soy_template"),
        Space("space"),
        SQL("sql"),
        Stylus("stylus"),
        SVG("svg"),
        Tcl("tcl"),
        Tex("tex"),
        Text("text"),
        Textile("textile"),
        Toml("toml"),
        Twig("twig"),
        Typescript("typescript"),
        VBScript("vbscript"),
        Velocity("velocity"),
        Verilog("verilog"),
        VHDL("vhdl"),
        XML("xml"),
        XQuery("xquery"),
        YAML("yaml");

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
            options = {"ABAP", "ABC", "ActionScript", "ADA", "ApacheConf", "AsciiDoc", "AssemblyX86", "AutoHot",
                    "BatchFile", "C9SearchResults", "Clojure", "Cobol", "CoffeeScript", "ColdFusion", "CSharp", "CSS",
                    "Curly", "CCpp", "D", "Dart", "Diff", "Django", "Dot", "EJS", "Elixir", "Elm", "Erlang", "Forth",
                    "FreeMarker", "Glsl", "Go", "Groovy", "HAML", "Handlebars", "Haskell", "HaXe", "HTML",
                    "HTMLCompletions", "HTMLRuby", "INI", "Jack", "Jade", "Java", "JavaScript", "JSON", "JSONiq", "JSP",
                    "JSX", "Julia", "LaTeX", "Lean", "LESS", "Liquid", "Lisp", "LiveScript", "LogiQL", "LSL", "Lua",
                    "LuaPage", "Lucene", "Makefile", "Markdown", "MATLAB", "MEL", "MIPSAssembler", "MUSHCode",
                    "MUSHCodeHighRules", "MySQL", "Nix", "ObjectiveC", "OCaml", "Pascal", "Perl", "PgSQL", "PHP",
                    "PlainText", "Powershell", "Prolog", "Properties", "Protobuf", "Python", "R", "RDoc", "RHTML",
                    "Ruby", "Rust", "SASS", "SCAD", "Scala", "Scheme", "SCSS", "SH", "SJS", "Snippets", "SoyTemplate",
                    "Space", "SQL", "Stylus", "SVG", "Tcl", "Tex", "Text", "Textile", "Toml", "Twig", "Typescript",
                    "VBScript", "Velocity", "Verilog", "VHDL", "XML", "XQuery", "YAML"})
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
