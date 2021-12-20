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

package io.jmix.core.credits;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import io.jmix.core.Resources;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides information about third-party software by loading {@code credits.xml} files of Jmix modules.
 * <p>
 * Example usage:
 * <pre>
 * &#64;Autowired
 * private CreditsLoader creditsLoader;
 * &#64;Autowired
 * private Downloader downloader;
 *
 * public void showCredits() {
 *     byte[] html = creditsLoader.getCreditsHtml(true);
 *     downloader.download(html, "credits.html");
 * }
 * </pre>
 * @see #getCredits()
 * @see #getCreditsHtml(boolean)
 */
@Component("core_CreditsLoader")
public class CreditsLoader {

    private static final Logger log = LoggerFactory.getLogger(CreditsLoader.class);

    @Autowired
    private JmixModules modules;

    @Autowired
    private Resources resources;

    @Autowired
    private Dom4jTools dom4jTools;

    /**
     * Returns credits as a list of {@link CreditsItem} objects.
     */
    public List<CreditsItem> getCredits() {
        Map<String, LicenseItem> licenses = new HashMap<>();
        Map<String, CreditsItem> creditsItems = new HashMap<>();

        modules.getAll().stream()
                .map(JmixModuleDescriptor::getBasePackage)
                .forEach(basePackage -> {
                    loadCredits(basePackage, creditsItems, licenses);
                });

        List<CreditsItem> result = new ArrayList<>(creditsItems.values());
        Collections.sort(result);

        return result;
    }

    /**
     * Returns credits as a byte array containing an HTML document rendered using {@link CreditsHtmlRenderer}.
     *
     * @param completePage if true, renders the complete page with html, header and body. Otherwise, returns an HTML
     *                     table only.
     */
    public byte[] getCreditsHtml(boolean completePage) {
        CreditsHtmlRenderer renderer = new CreditsHtmlRenderer(completePage);
        return renderer.render(getCredits());
    }

    private void loadCredits(String basePackage, Map<String, CreditsItem> creditsItems, Map<String, LicenseItem> licenses) {
        String location = basePackage.replace('.', '/') + "/credits.xml";
        String resource = resources.getResourceAsString(location);
        if (resource == null) {
            return;
        }

        try {
            Element rootEl = dom4jTools.readDocument(resource).getRootElement();

            Element licensesEl = rootEl.element("licenses");
            if (licensesEl != null) {
                for (Element licenseEl : licensesEl.elements("license")) {
                    String id = licenseEl.attributeValue("id");
                    String name = licenseEl.attributeValue("name");
                    String url = licenseEl.attributeValue("url");
                    licenses.computeIfAbsent(id, k -> new LicenseItem(name, url));
                }
            }

            Element itemsEl = rootEl.element("items");
            if (itemsEl != null) {
                for (Element itemEl : itemsEl.elements("item")) {
                    String name = itemEl.attributeValue("name");
                    String url = itemEl.attributeValue("url");
                    String licenseName = null;
                    String licenseUrl = null;
                    Element licenseEl = itemEl.element("license");
                    if (licenseEl != null) {
                        licenseName = licenseEl.attributeValue("name");
                        if (licenseName == null) {
                            String ref = licenseEl.attributeValue("ref");
                            if (ref != null) {
                                LicenseItem licenseItem = licenses.get(ref);
                                if (licenseItem != null) {
                                    licenseName = licenseItem.name;
                                    licenseUrl = licenseItem.url;
                                }
                            }
                        } else {
                            licenseUrl = licenseEl.attributeValue("url");
                        }
                    }
                    if (licenseName == null) {
                        log.warn("Cannot resolve license for " + name);
                    }
                    CreditsItem creditsItem = new CreditsItem(name, url, licenseName, licenseUrl);
                    creditsItems.compute(name, (k, v) -> creditsItem);
                }
            }

        } catch (Exception e) {
            log.warn("Error loading credits from " + location + ": " + e.getMessage());
        }
    }

    private static class LicenseItem {
        String name;
        String url;

        LicenseItem(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
