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

package io.jmix.reports.yarg.formatters.impl.docx;

import io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.docx4j.wml.P;

import java.net.URLDecoder;
import java.util.List;

public class UrlVisitor extends TraversalUtil.CallbackImpl {
    protected DocxFormatterDelegate docxFormatter;
    protected MainDocumentPart mainDocumentPart;

    public UrlVisitor(DocxFormatterDelegate docxFormatter, MainDocumentPart mainDocumentPart) {
        this.docxFormatter = docxFormatter;
        this.mainDocumentPart = mainDocumentPart;
    }

    @Override
    public List<Object> apply(Object o) {
        if (o instanceof P.Hyperlink) {
            P.Hyperlink hyperlink = (P.Hyperlink) o;
            try {
                Relationships contents = mainDocumentPart.getRelationshipsPart().getContents();
                List<Relationship> relationships = contents.getRelationship();
                for (Relationship relationship : relationships) {
                    if (relationship.getId().equals(hyperlink.getId())) {
                        relationship.setTarget(docxFormatter.handleStringWithAliases(URLDecoder.decode(relationship.getTarget(), "UTF-8")));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("An error occurred while processing URL with aliases",e);
            }
        }
        return null;
    }
}
