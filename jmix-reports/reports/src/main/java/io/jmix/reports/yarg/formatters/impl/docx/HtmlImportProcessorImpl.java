/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.formatters.impl.docx;

import io.jmix.reports.yarg.formatters.impl.docx.HtmlImportProcessor;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

public class HtmlImportProcessorImpl implements HtmlImportProcessor {
    @Override
    public String processHtml(String source) {
        org.jsoup.nodes.Document document = Jsoup.parse(source);
        processHtmlDocument(document);
        document.outputSettings()
                .syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
                .prettyPrint(false)
                .escapeMode(Entities.EscapeMode.xhtml);


        return document.html();
    }

    protected void processHtmlDocument(org.jsoup.nodes.Document document) {
        processFontColor(document);
    }

    protected void processFontColor(org.jsoup.nodes.Document document) {
        Elements elements = document.getElementsByTag("font");
        for (Element element : elements) {
            String color = element.attr("color");
            if (StringUtils.isNotEmpty(color)) {
                String style = StringUtils.trim(element.attr("style"));
                if (style != null) {
                    if (StringUtils.endsWith(style, ";")) {
                        style = style + ";";
                    }
                    style = style + "color:" + color;
                } else {
                    style = "color:" + color;
                }
                element.attr("style", style);
                element.removeAttr("color");
            }
        }
    }
}
