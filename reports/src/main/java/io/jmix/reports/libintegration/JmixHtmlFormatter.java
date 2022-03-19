/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reports.libintegration;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.HtmlFormatter;
import com.haulmont.yarg.formatters.impl.pdf.HtmlToPdfConverter;
import com.haulmont.yarg.formatters.impl.pdf.ITextPdfConverter;
import com.haulmont.yarg.structure.BandData;
import com.lowagie.text.Image;
import freemarker.ext.util.WrapperTemplateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import io.jmix.core.*;
import io.jmix.reports.ReportsProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.ImageResource;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Component("report_JmixHtmlFormatter")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixHtmlFormatter extends HtmlFormatter {
    protected static final String JMIX_FONTS_DIR = "/jmix/fonts";

    public static final String RESOURCE_PREFIX = "resource://";

    @Autowired
    protected Messages messages;
    @Autowired
    protected ReportsProperties reportsProperties;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FileStorageLocator fileStorageLocator;
    @Autowired
    protected Resources resources;

    public JmixHtmlFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    //todo degtyarjov, artamonov - get rid of custom processing of file descriptors, use field formats
    // we can append <content> with Base64 to html and put reference to <img> for html
    // and some custom reference if we need pdf and then implement ResourcesITextUserAgentCallback which will
    // take base64 from appropriate content
    @Override
    protected void renderPdfDocument(String htmlContent, OutputStream outputStream) {
        ITextRenderer renderer = new ITextRenderer();
        try {
            htmlContent = Pattern.compile("(?i)<!doctype").matcher(htmlContent).replaceAll("<!DOCTYPE");
            File tmpFile = File.createTempFile("htmlReport", ".htm");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(tmpFile));
            dataOutputStream.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            dataOutputStream.close();

            HtmlToPdfConverter converter = new ITextPdfConverter(renderer);
            loadFonts(converter);

            ResourcesITextUserAgentCallback userAgentCallback =
                    new ResourcesITextUserAgentCallback(renderer.getOutputDevice());
            userAgentCallback.setSharedContext(renderer.getSharedContext());

            renderer.getSharedContext().setUserAgentCallback(userAgentCallback);

            String url = tmpFile.toURI().toURL().toString();
            converter.convert(url, outputStream);

            FileUtils.deleteQuietly(tmpFile);
        } catch (Exception e) {
            throw wrapWithReportingException("", e);
        }
    }

    @Override
    protected void loadFonts(HtmlToPdfConverter converter) {
        String fontsPath = coreProperties.getConfDir() + JMIX_FONTS_DIR;

        File fontsDir = new File(fontsPath);

        loadFontsFromDirectory(converter, fontsDir);

        if (StringUtils.isNotBlank(reportsProperties.getPdfFontsDirectory())) {
            File systemFontsDir = new File(reportsProperties.getPdfFontsDirectory());
            loadFontsFromDirectory(converter, systemFontsDir);
        }
    }

    protected class ResourcesITextUserAgentCallback extends ITextUserAgent {

        public ResourcesITextUserAgentCallback(ITextOutputDevice outputDevice) {
            super(outputDevice);
        }

        @Override
        public ImageResource getImageResource(String uri) {
            FileRef fileRef = getFileRef(uri);
            if (fileRef != null) {
                ImageResource resource;
                resource = (ImageResource) _imageCache.get(uri);
                if (resource == null) {
                    resource = createImageResource(uri);
                    if (resource != null) {
                        _imageCache.put(uri, resource);
                    }
                }

                if (resource != null) {
                    ITextFSImage image = (ITextFSImage) resource.getImage();

                    Image imageObject;
                    // use reflection for access to internal image
                    try {
                        Field imagePrivateField = image.getClass().getDeclaredField("_image");
                        imagePrivateField.setAccessible(true);

                        imageObject = (Image) imagePrivateField.get(image);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        throw new ReportFormattingException("Error while clone internal image in Itext");
                    }

                    resource = new ImageResource(uri, new ITextFSImage(imageObject));
                } else {
                    resource = new ImageResource(uri, null);
                }

                return resource;
            } else if (StringUtils.startsWith(uri, RESOURCE_PREFIX)) {
                ImageResource resource = createImageResource(uri);
                if (resource == null) {
                    resource = new ImageResource(uri, null);
                }
                return resource;
            }

            return super.getImageResource(uri);
        }

        @Nullable
        protected ImageResource createImageResource(String uri) {
            ImageResource resource = null;
            InputStream is = resolveAndOpenStream(uri);
            if (is != null) {
                try {
                    Image image = Image.getInstance(IOUtils.toByteArray(is));

                    scaleToOutputResolution(image);
                    resource = new ImageResource(uri, new ITextFSImage(image));
                    //noinspection unchecked
                } catch (Exception e) {
                    throw wrapWithReportingException(
                            format("Can't read image file; unexpected problem for URI '%s'", uri), e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
            return resource;
        }

        protected void scaleToOutputResolution(Image image) {
            float factor = getSharedContext().getDotsPerPixel();
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }

        @Override
        @Nullable
        protected InputStream resolveAndOpenStream(String uri) {
            FileRef fileRef = getFileRef(uri);
            if (fileRef != null) {
                try {
                    return fileStorageLocator.getByName(fileRef.getStorageName()).openStream(fileRef);
                } catch (FileStorageException e) {
                    throw wrapWithReportingException(
                            format("An error occurred while loading file with URI [%s] from file storage", uri), e);
                }
            } else if (StringUtils.startsWith(uri, RESOURCE_PREFIX)) {
                String resolvedUri = resolveResourcePrefix(uri);
                return resources.getResourceAsStream(resolvedUri);
            } else {
                return getInputStream(uri);
            }
        }

        @Nullable
        protected FileRef getFileRef(String uri) {
            try {
                return FileRef.fromString(uri);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        protected InputStream getInputStream(String uri) {
            uri = resolveURI(uri);
            InputStream inputStream;
            try {
                URL url = new URL(uri);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setConnectTimeout(reportsProperties.getHtmlExternalResourcesTimeoutSec() * 1000);
                inputStream = urlConnection.getInputStream();
            } catch (SocketTimeoutException e) {
                throw new ReportFormattingException(format("Loading resource [%s] has been stopped by timeout", uri), e);
            } catch (MalformedURLException e) {
                throw new ReportFormattingException(format("Bad URL given: [%s]", uri), e);
            } catch (FileNotFoundException e) {
                throw new ReportFormattingException(format("Resource at URL [%s] not found", uri));
            } catch (IOException e) {
                throw new ReportFormattingException(format("An IO problem occurred while loading resource [%s]", uri), e);
            }

            return inputStream;
        }
    }

    protected String resolveResourcePrefix(String uri) {
        return uri.replace(RESOURCE_PREFIX, "");
    }

    @SuppressWarnings("unchecked")
    protected Map getTemplateModel(BandData rootBand) {
        Map model = super.getTemplateModel(rootBand);
        if (reportTemplate.isGroovy()) {
            model.put("messages", messages);
        } else {
            model.put("getMessage", (TemplateMethodModelEx) arguments -> {
                checkArgsCount("getMessage", arguments, 1, 2);
                if (arguments.size() == 1) {
                    Object arg = arguments.get(0);
                    if (arg instanceof WrapperTemplateModel && ((WrapperTemplateModel) arg).getWrappedObject() instanceof Enum) {
                        return messages.getMessage((Enum) ((WrapperTemplateModel) arg).getWrappedObject());
                    } else {
                        throwIncorrectArgType("getMessage", 1, "Enum");
                    }
                }
                if (arguments.size() == 2) {
                    Object arg1 = arguments.get(0);
                    Object arg2 = arguments.get(1);
                    if (!(arg1 instanceof TemplateScalarModel)) {
                        throwIncorrectArgType("getMessage", 1, "String");
                    }
                    if (!(arg2 instanceof TemplateScalarModel)) {
                        throwIncorrectArgType("getMessage", 2, "String");
                    }
                    return messages.getMessage(((TemplateScalarModel) arg1).getAsString(), ((TemplateScalarModel) arg2).getAsString());
                }
                return null;
            });
            model.put("getMainMessage", (TemplateMethodModelEx) arguments -> {
                checkArgsCount("getMainMessage", arguments, 1);
                Object arg = arguments.get(0);
                if (arg instanceof TemplateScalarModel) {
                    return messages.getMessage(((TemplateScalarModel) arg).getAsString());
                } else {
                    throwIncorrectArgType("getMainMessage", 1, "String");
                }
                return null;
            });
        }

        return model;
    }

    @Override
    protected Map getBandModel(BandData band) {
        Map<String, Object> model = new HashMap<>();

        Map<String, Object> bands = new HashMap<>();
        for (String bandName : band.getChildrenBands().keySet()) {
            List<BandData> subBands = band.getChildrenBands().get(bandName);
            List<Map> bandModels = new ArrayList<>();
            for (BandData child : subBands)
                bandModels.add(getBandModel(child));

            bands.put(bandName, bandModels);
        }
        model.put("bands", bands);
        Map<String, Object> data = new HashMap<>();
        for (String key : band.getData().keySet()) {
            if (band.getData().get(key) instanceof Enum)
                data.put(key, defaultFormat(band.getData().get(key)));
            else
                data.put(key, band.getData().get(key));
        }
        model.put("fields", data);

        return model;
    }

    protected void checkArgsCount(String methodName, @Nullable List arguments, int... count) throws TemplateModelException {
        if ((arguments == null || arguments.size() == 0) && Arrays.binarySearch(count, 0) == -1)
            throw new TemplateModelException(format("Arguments not specified for method: %s", methodName));
        if (arguments != null && Arrays.binarySearch(count, arguments.size()) == -1) {
            throw new TemplateModelException(format("Incorrect arguments count: %s. Expected count: %s", arguments.size(), Arrays.toString(count)));
        }
    }

    protected void throwIncorrectArgType(String methodName, int argIdx, String type) throws TemplateModelException {
        throw new TemplateModelException(format("Incorrect argument[%s] type for method %s. Expected type %s", argIdx, methodName, type));
    }
}