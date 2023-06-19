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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class ITextPdfConverter implements HtmlToPdfConverter {
    protected ITextRenderer renderer;

    public ITextPdfConverter() {
        this(new ITextRenderer());
    }

    public ITextPdfConverter(ITextRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void addFont(File file) throws IOException {
        try {
            renderer.getFontResolver().addFont(file.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        } catch (DocumentException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void convert(String sourceUrl, OutputStream destination) throws Exception {
        renderer.setDocument(sourceUrl);
        renderer.layout();
        renderer.createPDF(destination);
    }
}
