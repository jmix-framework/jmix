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
package io.jmix.reports.yarg.formatters.impl;

import io.jmix.reports.yarg.formatters.impl.doc.OfficeComponent;
import io.jmix.reports.yarg.formatters.impl.doc.OfficeOutputStream;
import io.jmix.reports.yarg.formatters.impl.doc.TableManager;
import io.jmix.reports.yarg.formatters.impl.doc.connector.NoFreePortsException;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
import io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeTask;
import io.jmix.reports.yarg.formatters.impl.inline.ContentInliner;
import io.jmix.reports.yarg.exception.OpenOfficeException;
import io.jmix.reports.yarg.exception.ReportingException;
import io.jmix.reports.yarg.exception.ReportingInterruptedException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.io.IOException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.table.XCell;
import com.sun.star.text.*;
import com.sun.star.util.XReplaceable;
import com.sun.star.util.XSearchDescriptor;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportFieldFormat;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.jmix.reports.yarg.formatters.impl.doc.UnoConverter.as;

/**
 * Document formatter for '.doc' and '.odt' file types
 */
public class DocFormatter extends AbstractFormatter {
    protected static final Logger log = LoggerFactory.getLogger(DocFormatter.class);

    protected static final String SEARCH_REGULAR_EXPRESSION = "SearchRegularExpression";

    protected static final String PDF_OUTPUT_FILE = "writer_pdf_Export";
    protected static final String MS_WORD_OUTPUT_FILE = "MS Word 97";

    protected XComponent xComponent;

    protected OfficeComponent officeComponent;

    protected OfficeIntegrationAPI officeIntegration;

    public DocFormatter(FormatterFactoryInput formatterFactoryInput, OfficeIntegrationAPI officeIntegration) {
        super(formatterFactoryInput);
        checkNotNull(officeIntegration, "\"officeIntegration\" parameter can not be null");

        this.officeIntegration = officeIntegration;
        supportedOutputTypes.add(ReportOutputType.doc);
        supportedOutputTypes.add(ReportOutputType.pdf);
    }

    public void renderDocument() {
        try {
            doCreateDocument(outputStream);
        } catch (ReportingInterruptedException ie) {
            throw ie;
        } catch (Exception e) {
            doCreateDocumentWithRetries(outputStream, e, 0);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    protected void doCreateDocumentWithRetries(final OutputStream outputStream, Exception lastException, int currentAttempt) {
        if (officeIntegration.getCountOfRetry() != 0 && currentAttempt < officeIntegration.getCountOfRetry()) {
            log.warn("An error occurred while generating doc report {}. System will retry to generate doc report again (current attempt: {}).",
                    reportTemplate.getDocumentName(), currentAttempt + 1);
            log.debug("Last error:", lastException);
            try {
                Thread.sleep(officeIntegration.getRetryIntervalMs());

                checkThreadInterrupted();
                doCreateDocument(outputStream);
            } catch (ReportingInterruptedException ie) {
                throw ie;
            } catch (InterruptedException e) {
                throw new ReportingInterruptedException("Doc report task interrupted");
            } catch (Exception e) {
                doCreateDocumentWithRetries(outputStream, e, ++currentAttempt);
            }
        } else {
            if (lastException instanceof NoFreePortsException) {
                throw (NoFreePortsException) lastException;
            }
            throw wrapWithReportingException("An error occurred while generating doc report. All attempts failed", lastException);
        }
    }

    protected void doCreateDocument(final OutputStream outputStream) throws NoFreePortsException {
        OfficeTask officeTask = ooResourceProvider -> {
            try {
                loadDocument(ooResourceProvider);

                // Handling tables
                fillTables(ooResourceProvider.getXDispatchHelper());
                // Handling text
                replaceAllAliasesInDocument();
                replaceAllAliasesInDocument();//we do it second time to handle several open office bugs (page breaks in html, etc). Do not remove.
                // Saving document to output stream and closing
                saveAndClose(ooResourceProvider, xComponent, outputType, outputStream);
            } catch (Exception e) {
                throw wrapWithReportingException("An error occurred while running task in Open Office server", e);
            }
        };

        officeIntegration.runTaskWithTimeout(officeTask, officeIntegration.getTimeoutInSeconds());
    }

    protected void loadDocument(OfficeResourceProvider ooResourceProvider) throws com.sun.star.lang.IllegalArgumentException, IOException {
        xComponent = ooResourceProvider.loadXComponent(reportTemplate.getDocumentContent());
        officeComponent = new OfficeComponent(ooResourceProvider, xComponent);
    }

    protected void saveAndClose(OfficeResourceProvider ooResourceProvider, XComponent xComponent, ReportOutputType outputType, OutputStream outputStream)
            throws IOException {
        OfficeOutputStream ooos = new OfficeOutputStream(outputStream);
        String filterName;
        if (ReportOutputType.pdf.equals(outputType)) {
            filterName = PDF_OUTPUT_FILE;
        } else {
            filterName = MS_WORD_OUTPUT_FILE;
        }
        ooResourceProvider.saveXComponent(xComponent, ooos, filterName);
        ooResourceProvider.closeXComponent(xComponent);
    }

    protected void fillTables(XDispatchHelper xDispatchHelper) throws com.sun.star.uno.Exception {
        List<String> tablesNames = TableManager.getTablesNames(xComponent);

        for (String tableName : tablesNames) {
            TableManager tableManager = new TableManager(xComponent, tableName);
            BandFinder bandFinder = new BandFinder(tableManager).find();

            BandData band = bandFinder.getBand();
            String bandName = bandFinder.getBandName();
            int numberOfRowWithAliases = tableManager.findRowWithAliases();

            if (band != null && numberOfRowWithAliases > -1) {
                XTextTable xTextTable = tableManager.getXTextTable();

                // try to select one cell without it workaround
                int columnCount = xTextTable.getColumns().getCount();
                if (columnCount < 2) {
                    xTextTable.getColumns().insertByIndex(columnCount, 1);
                }

                fillTable(band.getName(), band.getParentBand(), tableManager, xDispatchHelper, numberOfRowWithAliases);

                // end of workaround ->
                if (columnCount < 2) {
                    xTextTable.getColumns().removeByIndex(columnCount, 1);
                }
            } else if (numberOfRowWithAliases > -1
                    && rootBand.getFirstLevelBandDefinitionNames() != null
                    && rootBand.getFirstLevelBandDefinitionNames().contains(bandName)) {
                //if table is linked with band and has aliases on it, but no band data found -
                //we are removing the row
                tableManager.deleteRow(numberOfRowWithAliases);
            }
        }
    }

    protected void fillTable(String name, BandData parentBand, TableManager tableManager, XDispatchHelper xDispatchHelper, int numberOfRowWithAliases)
            throws com.sun.star.uno.Exception {
        // Lock clipboard, cause uno uses it to grow tables, relevant for desktops
        synchronized (clipboardLock) {
            if (officeIntegration.isDisplayDeviceAvailable()) {
                clearClipboard();
            }

            List<BandData> childrenByName = parentBand.getChildrenByName(name);
            for (BandData ignored : childrenByName) {
                tableManager.copyRow(xDispatchHelper, as(XTextDocument.class, xComponent).getCurrentController(), numberOfRowWithAliases);
            }

            int i = numberOfRowWithAliases;
            for (BandData child : childrenByName) {
                if (name.equals(child.getName())) {
                    fillRow(child, tableManager, i);
                    i++;
                }
            }
            tableManager.deleteRow(i);
        }
    }

    protected void fillRow(BandData band, TableManager tableManager, int row)
            throws com.sun.star.lang.IndexOutOfBoundsException, NoSuchElementException, WrappedTargetException {
        List<String> cellNamesForTheRow = tableManager.getCellNamesForTheRow(row);
        for (int col = 0; col < cellNamesForTheRow.size(); col++) {
            fillCell(band, tableManager.getXCell(col, row));
        }
    }

    protected void fillCell(BandData band, XCell xCell) throws NoSuchElementException, WrappedTargetException {
        checkThreadInterrupted();
        XText xText = as(XText.class, xCell);
        String cellText = xText.getString();
        cellText = cellText.replace("\r\n", "\n");//just a workaround for Windows \r\n break symbol
        List<String> parametersToInsert = new ArrayList<String>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(cellText);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
            XTextCursor xTextCursor = xText.createTextCursor();

            String paramStr = "${" + parameterName + "}";
            int index = cellText.indexOf(paramStr);
            while (index >= 0) {
                xTextCursor.gotoStart(false);
                xTextCursor.goRight((short) (index + paramStr.length()), false);
                xTextCursor.goLeft((short) paramStr.length(), true);

                insertValue(xText, xTextCursor, band, parameterName);
                cellText = formatCellText(xText.getString());

                index = cellText.indexOf(paramStr);
            }
        }
    }

    /**
     * Replaces all aliases ${bandname.paramname} in document text.
     *
     * @throws ReportingException If there is not appropriate band or alias is bad
     */
    protected void replaceAllAliasesInDocument() {
        XTextDocument xTextDocument = as(XTextDocument.class, xComponent);
        XReplaceable xReplaceable = as(XReplaceable.class, xTextDocument);
        XSearchDescriptor searchDescriptor = xReplaceable.createSearchDescriptor();
        searchDescriptor.setSearchString(ALIAS_WITH_BAND_NAME_REGEXP);
        try {
            searchDescriptor.setPropertyValue(SEARCH_REGULAR_EXPRESSION, true);
        } catch (Exception e) {
            throw new OpenOfficeException("An error occurred while setting search properties in Open office", e);
        }

        XIndexAccess indexAccess = xReplaceable.findAll(searchDescriptor);
        for (int i = 0; i < indexAccess.getCount(); i++) {
            try {
                XTextRange textRange = as(XTextRange.class, indexAccess.getByIndex(i));
                String alias = unwrapParameterName(textRange.getString());

                BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);

                BandData band = findBandByPath(bandAndParameter.getBandPath());

                if (band != null) {
                    insertValue(textRange.getText(), textRange, band, bandAndParameter.getParameterName());
                } else {
                    throw wrapWithReportingException(String.format("No band for alias [%s] found", alias));
                }
            } catch (ReportingException e) {
                throw e;
            } catch (Exception e) {
                throw wrapWithReportingException(String.format("An error occurred while replacing aliases in document. Regexp [%s]. Replacement number [%d]", ALIAS_WITH_BAND_NAME_REGEXP, i), e);
            }
        }
    }

    protected void insertValue(XText text, XTextRange textRange, BandData band, String parameterName) {
        checkThreadInterrupted();
        String fullParameterName = band.getName() + "." + parameterName;
        Object paramValue = band.getParameterValue(parameterName);

        Map<String, ReportFieldFormat> formats = rootBand.getReportFieldFormats();
        try {
            boolean handled = false;

            if (paramValue != null) {
                if ((formats != null) && (formats.containsKey(fullParameterName))) {
                    String format = formats.get(fullParameterName).getFormat();
                    // Handle doctags
                    for (ContentInliner contentInliner : contentInliners) {
                        Matcher matcher = contentInliner.getTagPattern().matcher(format);
                        if (matcher.find()) {
                            contentInliner.inlineToDoc(officeComponent, textRange, text, paramValue, matcher);
                            handled = true;
                        }
                    }
                }
                if (!handled) {
                    String valueString = formatValue(paramValue, parameterName, fullParameterName);
                    text.insertString(textRange, valueString, true);
                }
            } else {
                text.insertString(textRange, "", true);
            }
        } catch (Exception ex) {
            throw wrapWithReportingException(String.format("An error occurred while inserting parameter [%s] into text line [%s]", parameterName, text.getString()), ex);
        }
    }

    //delete nonexistent symbols from cell text
    protected String formatCellText(String cellText) {
        if (cellText != null) {
            return cellText.replace("\r", "");
        } else {
            return null;
        }
    }

    protected static final Object clipboardLock = new Object();

    protected static void clearClipboard() {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[0];
                }

                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return false;
                }

                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    throw new UnsupportedFlavorException(flavor);
                }
            }, null);
        } catch (IllegalStateException ignored) {
            //ignore exception
        }
    }

    protected class BandFinder {
        protected String tableName;
        protected TableManager tableManager;
        protected String bandName;
        protected BandData band;

        public BandFinder(TableManager tableManager) {
            this.tableName = tableManager.getTableName();
            this.tableManager = tableManager;
        }

        public String getBandName() {
            return bandName;
        }

        public BandData getBand() {
            return band;
        }

        public BandFinder find() {
            bandName = tableName;
            band = rootBand.findBandRecursively(bandName);
            if (band == null) {
                XText xText = tableManager.findFirstEntryInRow(BAND_NAME_DECLARATION_PATTERN, 0);
                if (xText != null) {
                    Matcher matcher = BAND_NAME_DECLARATION_PATTERN.matcher(xText.getString());
                    if (matcher.find()) {
                        bandName = matcher.group(1);
                        band = rootBand.findBandRecursively(bandName);
                        XTextCursor xTextCursor = xText.createTextCursor();

                        xTextCursor.gotoStart(false);
                        xTextCursor.goRight((short) matcher.end(), false);
                        xTextCursor.goLeft((short) matcher.group().length(), true);

                        xText.insertString(xTextCursor, "", true);
                    }
                }
            }
            return this;
        }
    }
}