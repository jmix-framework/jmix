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
package com.haulmont.cuba.gui.export;

import io.jmix.ui.download.DownloadFormat;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Format of data exported by {@link ExportDisplay}.
 * <p>
 * Wrapper for {@link DownloadFormat} for compatibility with legacy code.
 */
public final class ExportFormat implements Serializable {
    private static final long serialVersionUID = -8448531804422711852L;

    public static final ExportFormat HTML = new ExportFormat(DownloadFormat.HTML);
    public static final ExportFormat HTM = new ExportFormat(DownloadFormat.HTM);
    public static final ExportFormat PDF = new ExportFormat(DownloadFormat.PDF);
    public static final ExportFormat XLS = new ExportFormat(DownloadFormat.XLS);
    public static final ExportFormat XLSX = new ExportFormat(DownloadFormat.XLSX);
    public static final ExportFormat RTF = new ExportFormat(DownloadFormat.RTF);
    public static final ExportFormat DOC = new ExportFormat(DownloadFormat.DOC);
    public static final ExportFormat DOCX = new ExportFormat(DownloadFormat.DOCX);
    public static final ExportFormat XML = new ExportFormat(DownloadFormat.XML);
    public static final ExportFormat CSV = new ExportFormat(DownloadFormat.CSV);
    public static final ExportFormat JPEG = new ExportFormat(DownloadFormat.JPEG);
    public static final ExportFormat JPG = new ExportFormat(DownloadFormat.JPG);
    public static final ExportFormat PNG = new ExportFormat(DownloadFormat.PNG);
    public static final ExportFormat RAR = new ExportFormat(DownloadFormat.RAR);
    public static final ExportFormat ZIP = new ExportFormat(DownloadFormat.ZIP);
    public static final ExportFormat GZ = new ExportFormat(DownloadFormat.GZ);
    public static final ExportFormat JSON = new ExportFormat(DownloadFormat.JSON);
    public static final ExportFormat OCTET_STREAM = new ExportFormat(DownloadFormat.OCTET_STREAM);
    public static final ExportFormat TEXT = new ExportFormat(DownloadFormat.TEXT);

    public static final List<ExportFormat> DEFAULT_FORMATS = Collections.unmodifiableList(
            Arrays.asList(HTML, HTM, PDF, XLS, XLSX, RTF, DOC, DOCX, XML, CSV, JPEG, JPG, PNG, RAR, GZ, ZIP, OCTET_STREAM, JSON));

    protected final DownloadFormat downloadFormat;

    public ExportFormat(DownloadFormat downloadFormat) {
        this.downloadFormat = downloadFormat;
    }

    public ExportFormat(String contentType, String fileExt) {
        this(new DownloadFormat(contentType, fileExt));
    }

    public static ExportFormat getByExtension(String extension) {
        if (StringUtils.isEmpty(extension)) {
            return OCTET_STREAM;
        }

        String extLowerCase = StringUtils.lowerCase(extension);

        List<ExportFormat> formats = DEFAULT_FORMATS;
        for (ExportFormat f : formats) {
            if (f.getFileExt().equals(extLowerCase))
                return f;
        }
        return OCTET_STREAM;
    }

    public String getContentType() {
        return downloadFormat.getContentType();
    }

    public String getFileExt() {
        return downloadFormat.getFileExt();
    }

    public DownloadFormat getDownloadFormat() {
        return downloadFormat;
    }
}