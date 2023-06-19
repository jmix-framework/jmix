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

import io.jmix.reports.yarg.formatters.impl.AbstractFormatter;
import io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate;
import io.jmix.reports.yarg.formatters.impl.docx.RegexpCollectionFinder;
import io.jmix.reports.yarg.formatters.impl.docx.RegexpFinder;
import io.jmix.reports.yarg.formatters.impl.docx.TableManager;
import io.jmix.reports.yarg.formatters.impl.docx.TextMerger;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;

public class TableCollector extends TraversalUtil.CallbackImpl {
    private DocxFormatterDelegate docxFormatter;
    protected Stack<io.jmix.reports.yarg.formatters.impl.docx.TableManager> currentTables = new Stack<io.jmix.reports.yarg.formatters.impl.docx.TableManager>();
    protected Set<io.jmix.reports.yarg.formatters.impl.docx.TableManager> tableManagers = new LinkedHashSet<io.jmix.reports.yarg.formatters.impl.docx.TableManager>();

    public TableCollector(DocxFormatterDelegate docxFormatter) {this.docxFormatter = docxFormatter;}

    public List<Object> apply(Object object) {
        final io.jmix.reports.yarg.formatters.impl.docx.TableManager currentTable = !currentTables.isEmpty() ? currentTables.peek() : null;
        if (currentTable == null || currentTable.isSkipIt()) {
            return null;
        }

        if (object instanceof Tr) {
            Tr currentRow = (Tr) object;

            if (currentTable.firstRow == null) {
                currentTable.firstRow = currentRow;

                findNameForCurrentTable(currentTable);

                if (currentTable.bandName == null) {
                    currentTable.setSkipIt(true);
                } else {
                    tableManagers.add(currentTable);
                }
            }

            if (currentTable.rowWithAliases == null) {
                RegexpCollectionFinder<P> aliasFinder = new RegexpCollectionFinder<P>(docxFormatter, AbstractFormatter.UNIVERSAL_ALIAS_PATTERN, P.class);
                new TraversalUtil(currentRow, aliasFinder);
                List<String> foundAliases = aliasFinder.getValues();
                if (!foundAliases.isEmpty()) {
                    boolean fromCurrentBand = false;
                    for (String foundAlias : foundAliases) {
                        String parameterName = docxFormatter.unwrapParameterName(foundAlias);
                        if (parameterName != null) {
                            String[] parts = parameterName.split("\\.");
                            if (parts.length == 1) {
                                fromCurrentBand = true;
                                break;
                            } else if (docxFormatter.findBandByPath(parts[0]) == null) {
                                fromCurrentBand = true;
                                break;
                            }
                        }
                    }
                    if (fromCurrentBand) {
                        currentTable.rowWithAliases = currentRow;
                    }
                }
            }
        }

        return null;
    }

    protected void findNameForCurrentTable(final io.jmix.reports.yarg.formatters.impl.docx.TableManager currentTable) {
        new TraversalUtil(currentTable.firstRow,
                new RegexpFinder<P>(docxFormatter, AbstractFormatter.BAND_NAME_DECLARATION_PATTERN, P.class) {
                    @Override
                    protected void onFind(P paragraph, Matcher matcher) {
                        if (currentTable.bandName == null) {
                            super.onFind(paragraph, matcher);
                            currentTable.bandName = matcher.group(1);
                            String bandNameDeclaration = matcher.group();
                            Set<Text> mergedTexts = new TextMerger(paragraph, bandNameDeclaration).mergeMatchedTexts();
                            for (Text text : mergedTexts) {
                                text.setValue(text.getValue().replace(bandNameDeclaration, ""));
                            }
                        }
                    }
                });
    }

    // Depth first
    public void walkJAXBElements(Object parent) {
        List children = getChildren(parent);
        if (children != null) {

            for (Object o : children) {
                o = XmlUtils.unwrap(o);

                if (o instanceof Child && !(parent instanceof SdtBlock)) {
                    ((Child) o).setParent(parent);
                }

                if (o instanceof Tbl) {
                    currentTables.push(new io.jmix.reports.yarg.formatters.impl.docx.TableManager(docxFormatter, (Tbl) o));
                }

                this.apply(o);

                if (this.shouldTraverse(o)) {
                    walkJAXBElements(o);
                }

                if (o instanceof Tbl) {
                    TableManager currentTable = currentTables.pop();
                    currentTable.setSkipIt(false);
                }
            }
        }
    }
}
