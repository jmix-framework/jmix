/*
 * Copyright 2023 Haulmont.
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
package io.jmix.flowui.kit.component.codeeditor;

import jakarta.annotation.Nullable;

import java.util.Objects;

public enum CodeEditorMode {

    ABAP("abap"),
    ABC("abc"),
    ACTIONSCRIPT("actionscript"),
    ADA("ada"),
    ALDA("alda"),
    APACHE_CONF("apache_conf"),
    APEX("apex"),
    APPLESCRIPT("applescript"),
    AQL("aql"),
    ASCIIDOC("asciidoc"),
    ASL("asl"),
    ASSEMBLY_X_86("assembly_x86"),
    AUTOHOTKEY("autohotkey"),
    BATCHFILE("batchfile"),
    BIBTEX("bibtex"),
    C_9_SEARCH("c9search"),
    C_CPP("c_cpp"),
    CIRRU("cirru"),
    CLOJURE("clojure"),
    COBOL("cobol"),
    COFFEE("coffee"),
    COLDFUSION("coldfusion"),
    CRYSTAL("crystal"),
    CSHARP("csharp"),
    CSOUND_DOCUMENT("csound_document"),
    CSOUND_ORCHESTRA("csound_orchestra"),
    CSOUND_SCORE("csound_score"),
    CSP("csp"),
    CSS("css"),
    CURLY("curly"),
    D("d"),
    DART("dart"),
    DIFF("diff"),
    DJANGO("django"),
    DOCKERFILE("dockerfile"),
    DOT("dot"),
    DROOLS("drools"),
    EDIFACT("edifact"),
    EIFFEL("eiffel"),
    EJS("ejs"),
    ELIXIR("elixir"),
    ELM("elm"),
    ERLANG("erlang"),
    FORTH("forth"),
    FORTRAN("fortran"),
    FSHARP("fsharp"),
    FSL("fsl"),
    FTL("ftl"),
    GCODE("gcode"),
    GHERKIN("gherkin"),
    GITIGNORE("gitignore"),
    GLSL("glsl"),
    GOBSTONES("gobstones"),
    GOLANG("golang"),
    GRAPHQLSCHEMA("graphqlschema"),
    GROOVY("groovy"),
    HAML("haml"),
    HANDLEBARS("handlebars"),
    HASKELL("haskell"),
    HASKELL_CABAL("haskell_cabal"),
    HAXE("haxe"),
    HJSON("hjson"),
    HTML("html"),
    HTML_ELIXIR("html_elixir"),
    HTML_RUBY("html_ruby"),
    INI("ini"),
    IO("io"),
    ION("ion"),
    JACK("jack"),
    JADE("jade"),
    JAVA("java"),
    JAVASCRIPT("javascript"),
    JEXL("jexl"),
    JSON("json"),
    JSON_5("json5"),
    JSONIQ("jsoniq"),
    JSP("jsp"),
    JSSM("jssm"),
    JSX("jsx"),
    JULIA("julia"),
    KOTLIN("kotlin"),
    LATEX("latex"),
    LATTE("latte"),
    LESS("less"),
    LIQUID("liquid"),
    LISP("lisp"),
    LIVESCRIPT("livescript"),
    LOGIQL("logiql"),
    LOGTALK("logtalk"),
    LSL("lsl"),
    LUA("lua"),
    LUAPAGE("luapage"),
    LUCENE("lucene"),
    MAKEFILE("makefile"),
    MARKDOWN("markdown"),
    MASK("mask"),
    MATLAB("matlab"),
    MAZE("maze"),
    MEDIAWIKI("mediawiki"),
    MEL("mel"),
    MIPS("mips"),
    MIXAL("mixal"),
    MUSHCODE("mushcode"),
    MYSQL("mysql"),
    NGINX("nginx"),
    NIM("nim"),
    NIX("nix"),
    NSIS("nsis"),
    NUNJUCKS("nunjucks"),
    OBJECTIVEC("objectivec"),
    OCAML("ocaml"),
    PARTIQL("partiql"),
    PASCAL("pascal"),
    PERL("perl"),
    PGSQL("pgsql"),
    PHP("php"),
    PHP_LARAVEL_BLADE("php_laravel_blade"),
    PIG("pig"),
    PLAIN_TEXT("plain_text"),
    PLSQL("plsql"),
    POWERSHELL("powershell"),
    PRAAT("praat"),
    PRISMA("prisma"),
    PROLOG("prolog"),
    PROPERTIES("properties"),
    PROTOBUF("protobuf"),
    PUPPET("puppet"),
    PYTHON("python"),
    QML("qml"),
    R("r"),
    RAKU("raku"),
    RAZOR("razor"),
    RDOC("rdoc"),
    RED("red"),
    REDSHIFT("redshift"),
    RHTML("rhtml"),
    ROBOT("robot"),
    RST("rst"),
    RUBY("ruby"),
    RUST("rust"),
    SAC("sac"),
    SASS("sass"),
    SCAD("scad"),
    SCALA("scala"),
    SCHEME("scheme"),
    SCRYPT("scrypt"),
    SCSS("scss"),
    SH("sh"),
    SJS("sjs"),
    SLIM("slim"),
    SMARTY("smarty"),
    SMITHY("smithy"),
    SNIPPETS("snippets"),
    SOY_TEMPLATE("soy_template"),
    SPACE("space"),
    SPARQL("sparql"),
    SQL("sql"),
    SQLSERVER("sqlserver"),
    STYLUS("stylus"),
    SVG("svg"),
    SWIFT("swift"),
    TCL("tcl"),
    TERRAFORM("terraform"),
    TEX("tex"),
    TEXT("text"),
    TEXTILE("textile"),
    TOML("toml"),
    TSX("tsx"),
    TURTLE("turtle"),
    TWIG("twig"),
    TYPESCRIPT("typescript"),
    VALA("vala"),
    VBSCRIPT("vbscript"),
    VELOCITY("velocity"),
    VERILOG("verilog"),
    VHDL("vhdl"),
    VISUALFORCE("visualforce"),
    WOLLOK("wollok"),
    XML("xml"),
    XQUERY("xquery"),
    YAML("yaml"),
    ZEEK("zeek");

    private final String id;

    CodeEditorMode(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Nullable
    public static CodeEditorMode fromId(String id) {
        for (CodeEditorMode mode : values()) {
            if (Objects.equals(mode.getId(), id)) {
                return mode;
            }
        }

        return null;
    }
}


