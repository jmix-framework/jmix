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

import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Creates HTML from a list of {@link CreditsItem} objects.
 *
 * @see #render(Collection)
 */
public class CreditsHtmlRenderer {

    private boolean completePage;

    /**
     * Constructor.
     * @param completePage if true, renders the complete page with html, header and body. Otherwise, renders an HTML
     *                     table only.
     */
    public CreditsHtmlRenderer(boolean completePage) {
        this.completePage = completePage;
    }

    /**
     * Returns credits as a byte array containing an HTML document.
     */
    public byte[] render(Collection<CreditsItem> items) {
        StringBuilder sb = new StringBuilder();
        if (completePage) {
            sb.append("<html>\n" +
                    "<head>\n" +
                    "<title>Credits</title>\n" +
                    "<style>\n" +
                    "  td { padding-right: 1em; padding-top: 0.5em; }\n" +
                    "</style>\n" +
                    "<head>\n" +
                    "<body>\n");
        }
        sb.append("<h1>Credits</h1>\n");
        sb.append("<table>\n");
        for (CreditsItem item : items) {
            sb.append("<tr>\n");
            sb.append(String.format(
                            "<td><a href='%s' target='_blank'>%s</a></td>\n<td><a href='%s' target='_blank'>%s</a></td>\n",
                            item.getUrl(), item.getName(), item.getLicenseUrl(), item.getLicenseName()
                    )
            );
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");

        if (completePage) {
            sb.append("</body>\n</html>");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
