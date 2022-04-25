package io.jmix.flowui.sys;

import io.jmix.core.DevelopmentException;
import io.jmix.core.Resources;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads screen XML descriptors.
 */
@Component("flowui_ScreenXmlLoader")
public class ScreenXmlLoader {

    protected Resources resources;
    protected ScreenXmlDocumentCache screenXmlCache;
    protected ScreenXmlParser screenXmlParser;

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Autowired
    public void setScreenXmlCache(ScreenXmlDocumentCache screenXmlCache) {
        this.screenXmlCache = screenXmlCache;
    }

    @Autowired
    public void setScreenXmlParser(ScreenXmlParser screenXmlParser) {
        this.screenXmlParser = screenXmlParser;
    }

    /**
     * Loads a descriptor.
     *
     * @param resourcePath path to the resource containing the XML
     * @return root XML element
     */
    public Element load(String resourcePath) {
        String template = loadTemplate(resourcePath);
        Document document = getDocument(template);

        return document.getRootElement();
    }

    private String loadTemplate(String resourcePath) {
        try (InputStream stream = resources.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new DevelopmentException("Template is not found " + resourcePath, "Path", resourcePath);
            }

            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read screen template");
        }
    }

    protected Document getDocument(String template) {
        Document document = screenXmlCache.get(template);
        if (document == null) {
            document = createDocument(template);
            screenXmlCache.put(template, document);
        }

        return document;
    }

    protected Document createDocument(String template) {
        return screenXmlParser.parseDescriptor(template);
    }
}
