package io.jmix.flowui.sys;

import io.jmix.core.DevelopmentException;
import io.jmix.core.Resources;
import io.jmix.flowui.view.View;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads view XML descriptors.
 */
@Component("flowui_ViewXmlLoader")
public class ViewXmlLoader {

    protected Resources resources;
    protected ViewXmlDocumentCache viewXmlDocumentCache;
    protected ViewXmlParser viewXmlParser;

    @Autowired
    public void setResources(Resources resources) {
        this.resources = resources;
    }

    @Autowired
    public void setViewXmlDocumentCache(ViewXmlDocumentCache viewXmlDocumentCache) {
        this.viewXmlDocumentCache = viewXmlDocumentCache;
    }

    @Autowired
    public void setViewXmlParser(ViewXmlParser viewXmlParser) {
        this.viewXmlParser = viewXmlParser;
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
            throw new RuntimeException(String.format("Unable to read %s template", View.class.getSimpleName()));
        }
    }

    protected Document getDocument(String template) {
        Document document = viewXmlDocumentCache.get(template);
        if (document == null) {
            document = createDocument(template);
            viewXmlDocumentCache.put(template, document);
        }

        return document;
    }

    protected Document createDocument(String template) {
        return viewXmlParser.parseDescriptor(template);
    }
}
