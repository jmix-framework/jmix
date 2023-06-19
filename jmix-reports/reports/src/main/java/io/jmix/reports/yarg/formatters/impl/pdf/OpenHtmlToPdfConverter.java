/*
 * Copyright 2018 Haulmont
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
package io.jmix.reports.yarg.formatters.impl.pdf;

import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class OpenHtmlToPdfConverter implements HtmlToPdfConverter {
    protected PdfRendererBuilder builder;

    public OpenHtmlToPdfConverter() {
        this(new PdfRendererBuilder());
    }

    public OpenHtmlToPdfConverter(PdfRendererBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void addFont(File file) throws IOException {
        try {
            builder.useFont(file, Font.createFont(Font.TRUETYPE_FONT, file).getFamily());
        } catch (FontFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void convert(String sourceUrl, OutputStream destination) throws Exception {
        builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
        builder.useUnicodeBidiReorderer(new ICUBidiReorderer());
        builder.withUri(sourceUrl);
        builder.toStream(destination);
        builder.run();
    }
}
