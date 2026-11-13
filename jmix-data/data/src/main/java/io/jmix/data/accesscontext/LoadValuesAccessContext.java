/*
 * Copyright 2020 Haulmont.
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

package io.jmix.data.accesscontext;

import io.jmix.core.Metadata;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.core.accesscontext.AccessContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import org.springframework.lang.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines an authorization point for entity attributes in a JPQL query loading scalar values.
 */
public class LoadValuesAccessContext implements AccessContext {

    protected String queryString;
    protected final QueryParser queryParser;
    protected final Metadata metadata;

    protected boolean permitted = true;
    protected List<Integer> deniedSelectedIndexes;

    public LoadValuesAccessContext(String queryString,
                                   QueryTransformerFactory transformerFactory,
                                   Metadata metadata) {
        this.queryString = queryString;
        this.queryParser = transformerFactory.parser(queryString);
        this.metadata = metadata;
    }

    public Collection<MetaClass> getEntityClasses() {
        return queryParser.getQueryPaths().stream()
                .filter(QueryParser.QueryPath::isSelectedPath)
                .map(path -> metadata.getClass(path.getEntityName()))
                .collect(Collectors.toList());
    }

    public Collection<MetaPropertyPath> getSelectedPropertyPaths() {
        return queryParser.getQueryPaths().stream()
                .filter(QueryParser.QueryPath::isSelectedPath)
                .map(path -> metadata.getClass(path.getEntityName()).getPropertyPath(path.getPropertyPath()))
                .collect(Collectors.toList());
    }

    public Collection<MetaPropertyPath> getAllPropertyPaths() {
        return queryParser.getQueryPaths().stream()
                .filter(path -> !path.isSelectedPath())
                .map(path -> metadata.getClass(path.getEntityName()).getPropertyPath(path.getPropertyPath()))
                .collect(Collectors.toList());
    }

    public void setDenied() {
        permitted = false;
    }

    public boolean isPermitted() {
        return permitted;
    }

    public List<Integer> getDeniedSelectedIndexes() {
        return deniedSelectedIndexes == null ? Collections.emptyList() : deniedSelectedIndexes;
    }

    /**
     * Returns the positions of the select-clause items (result columns) that use the given property path.
     * A column is counted when the path appears anywhere in it: alone, inside an expression or inside
     * a subquery.
     *
     * @param propertyPath property path to look for
     * @return column positions in select-clause order, without duplicates
     */
    public List<Integer> getSelectedIndexes(MetaPropertyPath propertyPath) {
        List<Integer> indexes = new ArrayList<>();
        for (QueryParser.QueryPath path : queryParser.getQueryPaths()) {
            int index = path.getSelectedItemIndex();
            if (path.isSelectedPath() && index >= 0 && !indexes.contains(index)) {
                MetaPropertyPath currentPropertyPath =
                        metadata.getClass(path.getEntityName()).getPropertyPath(path.getPropertyPath());
                if (Objects.equals(propertyPath, currentPropertyPath)) {
                    indexes.add(index);
                }
            }
        }
        return indexes;
    }

    public void addDeniedSelectedIndex(int index) {
        if (deniedSelectedIndexes == null) {
            deniedSelectedIndexes = new ArrayList<>();
        }
        if (!deniedSelectedIndexes.contains(index)) {
            deniedSelectedIndexes.add(index);
        }
    }

    @Nullable
    @Override
    public String explainConstraints() {
        if (!permitted) {
            return "query {" + queryString + "}";
        }
        if (!getDeniedSelectedIndexes().isEmpty()) {
            return "properties " + getDeniedSelectedIndexes() + " in query {" + queryString + "}";
        }
        return null;
    }
}
