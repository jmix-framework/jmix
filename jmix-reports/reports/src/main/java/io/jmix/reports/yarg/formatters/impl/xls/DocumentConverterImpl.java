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

package io.jmix.reports.yarg.formatters.impl.xls;

import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.exception.ReportingInterruptedException;
import io.jmix.reports.yarg.formatters.impl.doc.OfficeOutputStream;
import io.jmix.reports.yarg.formatters.impl.doc.connector.NoFreePortsException;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeTask;
import com.sun.star.lang.XComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class DocumentConverterImpl implements DocumentConverter {
    protected static final Logger log = LoggerFactory.getLogger(DocumentConverterImpl.class);

    private static final String XLS_TO_PDF_OUTPUT_FILE = "calc_pdf_Export";
    private static final String ODT_TO_PDF_OUTPUT_FILE = "writer_pdf_Export";
    private static final String XLS_TO_HTML_OUTPUT_FILE = "XHTML Calc File";
    private static final String ODT_TO_HTML_OUTPUT_FILE = "XHTML Writer File";

    protected OfficeIntegrationAPI officeIntegration;

    public DocumentConverterImpl(OfficeIntegrationAPI officeIntegration) {
        this.officeIntegration = officeIntegration;
    }

    public void convertToPdf(FileType fileType, final byte[] documentBytes, final OutputStream outputStream) {
        String convertPattern = FileType.SPREADSHEET == fileType ? XLS_TO_PDF_OUTPUT_FILE : ODT_TO_PDF_OUTPUT_FILE;
        convert(convertPattern, documentBytes, outputStream);
    }

    @Override
    public void convertToHtml(FileType fileType, byte[] documentBytes, OutputStream outputStream) {
        String convertPattern = FileType.SPREADSHEET == fileType ? XLS_TO_HTML_OUTPUT_FILE : ODT_TO_HTML_OUTPUT_FILE;
        convert(convertPattern, documentBytes, outputStream);
    }

    protected void convert(String convertPattern, final byte[] documentBytes, final OutputStream outputStream) {
        try {
            convertOnes(convertPattern, documentBytes, outputStream);
        } catch (ReportingInterruptedException e) {
            throw e;
        } catch (Exception e) {
            convertWithRetries(convertPattern, documentBytes, outputStream, e, 0);
        }
    }

    protected void convertWithRetries(final String convertPattern,
                                      final byte[] documentBytes,
                                      final OutputStream outputStream,
                                      Exception lastException,
                                      int retriesCount) {
        if (officeIntegration.getCountOfRetry() != 0 && retriesCount < officeIntegration.getCountOfRetry()) {
            log.warn("An error occurred while converting to {}. System will retry to convert again (Current attempt: {}).",
                    convertPattern, retriesCount + 1);
            log.debug("Last error:", lastException);
            try {
                Thread.sleep(officeIntegration.getRetryIntervalMs());

                if (Thread.interrupted()) {
                    throw new ReportingInterruptedException("Document conversation task interrupted");
                }
                convertOnes(convertPattern, documentBytes, outputStream);
            } catch (ReportingInterruptedException ie) {
                throw ie;
            } catch (InterruptedException e) {
                throw new ReportingInterruptedException("Document conversation task interrupted");
            } catch (Exception e) {
                convertWithRetries(convertPattern, documentBytes, outputStream, e, ++retriesCount);
            }
        } else {
            if (lastException instanceof NoFreePortsException) {
                throw (NoFreePortsException) lastException;
            }

            throw new ReportingException(String.format("Unable to convert to %s. All attempts failed", convertPattern), lastException);
        }
    }

    protected void convertOnes(final String convertPattern, final byte[] documentBytes, final OutputStream outputStream) throws NoFreePortsException {
        OfficeTask officeTask = ooResourceProvider -> {
            try {
                XComponent xComponent = ooResourceProvider.loadXComponent(documentBytes);
                saveAndClose(ooResourceProvider, xComponent, outputStream, convertPattern);
            } catch (Exception e) {
                throw new ReportingException("An error occurred while running task in Open Office server", e);
            }
        };
        officeIntegration.runTaskWithTimeout(officeTask, officeIntegration.getTimeoutInSeconds());
    }

    protected void saveAndClose(OfficeResourceProvider ooResourceProvider, XComponent xComponent, OutputStream outputStream, String filterName) throws com.sun.star.io.IOException {
        OfficeOutputStream officeOutputStream = new OfficeOutputStream(outputStream);
        ooResourceProvider.saveXComponent(xComponent, officeOutputStream, filterName);
        ooResourceProvider.closeXComponent(xComponent);
    }
}