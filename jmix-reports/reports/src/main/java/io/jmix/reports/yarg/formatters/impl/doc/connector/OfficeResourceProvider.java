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
package io.jmix.reports.yarg.formatters.impl.doc.connector;

import io.jmix.reports.yarg.exception.OpenOfficeException;
import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.formatters.impl.doc.OfficeInputStream;
import io.jmix.reports.yarg.structure.ReportTemplate;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XCloseable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import static io.jmix.reports.yarg.formatters.impl.doc.UnoConverter.as;

public class OfficeResourceProvider {
    private static AtomicLong counter = new AtomicLong();

    protected XComponentContext xComponentContext;
    protected io.jmix.reports.yarg.formatters.impl.doc.connector.OfficeIntegration officeIntegration;
    private File temporaryFile;

    public OfficeResourceProvider(XComponentContext xComponentContext, OfficeIntegration officeIntegration) throws Exception {
        this.xComponentContext = xComponentContext;
        this.officeIntegration = officeIntegration;
    }

    public XComponentContext getXComponentContext() {
        return xComponentContext;
    }

    public XDispatchHelper getXDispatchHelper() {
        try {
            return createXDispatchHelper();
        } catch (Exception e) {
            throw new OpenOfficeException("Unable to create Open office components.", e);
        }
    }

    public XComponentLoader getXComponentLoader() {
        try {
            return as(XComponentLoader.class, createDesktop());
        } catch (Exception e) {
            throw new OpenOfficeException("Unable to create Open office components.", e);
        }
    }

    public XComponent loadXComponent(InputStream inputStream) throws com.sun.star.lang.IllegalArgumentException, IOException {
        try {
            return loadXComponent(IOUtils.toByteArray(inputStream));
        } catch (java.io.IOException e) {
            throw new ReportFormattingException("An error occurred while reading bytes", e);
        }
    }

    public XComponent loadXComponent(byte[] bytes) throws com.sun.star.lang.IllegalArgumentException, IOException {
        XComponentLoader xComponentLoader = getXComponentLoader();

        PropertyValue[] props = new PropertyValue[1];
        props[0] = new PropertyValue();
        props[0].Name = "Hidden";
        props[0].Value = Boolean.TRUE;

        File tempFile = createTempFile(bytes);

        return xComponentLoader.loadComponentFromURL(toURL(tempFile), "_blank", 0, props);
    }

    public XComponent loadXComponent(XInputStream inputStream) throws com.sun.star.lang.IllegalArgumentException, IOException {
        XComponentLoader xComponentLoader = getXComponentLoader();

        PropertyValue[] props = new PropertyValue[2];
        props[0] = new PropertyValue();
        props[1] = new PropertyValue();
        props[0].Name = "InputStream";
        props[0].Value = inputStream;
        props[1].Name = "Hidden";
        props[1].Value = true;
        return xComponentLoader.loadComponentFromURL("private:stream", "_blank", 0, props);
    }

    public XInputStream getXInputStream(ReportTemplate reportTemplate) {
        try {
            return new OfficeInputStream(IOUtils.toByteArray(reportTemplate.getDocumentContent()));
        } catch (java.io.IOException e) {
            throw new OpenOfficeException("An error occurred while converting template to XInputStream", e);
        }
    }

    public void closeXComponent(XComponent xComponent) {
        XCloseable xCloseable = as(XCloseable.class, xComponent);
        try {
            xCloseable.close(false);
        } catch (com.sun.star.util.CloseVetoException e) {
            xComponent.dispose();
        }
        FileUtils.deleteQuietly(temporaryFile);
    }

    public void saveXComponent(XComponent xComponent, XOutputStream xOutputStream, String filterName) throws IOException {
        PropertyValue[] props = new PropertyValue[2];
        props[0] = new PropertyValue();
        props[1] = new PropertyValue();
        props[0].Name = "OutputStream";
        props[0].Value = xOutputStream;
        props[1].Name = "FilterName";
        props[1].Value = filterName;
        XStorable xStorable = as(XStorable.class, xComponent);
        xStorable.storeToURL("private:stream", props);
    }


    protected XDispatchHelper createXDispatchHelper() throws Exception {
        Object o = xComponentContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.frame.DispatchHelper", xComponentContext);
        return as(XDispatchHelper.class, o);
    }

    protected XDesktop createDesktop() throws Exception {
        Object o = xComponentContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.frame.Desktop", xComponentContext);
        return as(XDesktop.class, o);
    }


    protected File createTempFile(byte[] bytes) {
        try {
            String tempFileName = String.format("document%d", counter.incrementAndGet());
            String tempFileExt = ".tmp";
            if (StringUtils.isNotBlank(officeIntegration.getTemporaryDirPath())) {
                Path tempDir = Paths.get(officeIntegration.getTemporaryDirPath());
                tempDir.toFile().mkdirs();

                temporaryFile = Files.createTempFile(
                        tempDir,
                        tempFileName,
                        tempFileExt)
                        .toFile();
            } else {
                temporaryFile = File.createTempFile(tempFileName, tempFileExt);
            }
            FileUtils.writeByteArrayToFile(temporaryFile, bytes);
            return temporaryFile;
        } catch (java.io.IOException e) {
            throw new ReportFormattingException("Could not create temporary file for pdf conversion", e);
        }
    }

    protected String toURL(File file) {
        return "file://" + file.toURI().getRawPath();
    }
}
