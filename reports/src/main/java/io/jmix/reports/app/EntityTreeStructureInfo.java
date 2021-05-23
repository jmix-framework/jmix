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

package io.jmix.reports.app;

import java.io.Serializable;

/**
 * That class is used in report wizard creator cause that wizard need info about entity attributes
 */
public class EntityTreeStructureInfo implements Serializable {
    private static final long serialVersionUID = -7636338880001636048L;
    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeRootHasCollections;

    public boolean isEntityTreeHasSimpleAttrs() {
        return entityTreeHasSimpleAttrs;
    }

    public void setEntityTreeHasSimpleAttrs(boolean entityTreeHasSimpleAttrs) {
        this.entityTreeHasSimpleAttrs = entityTreeHasSimpleAttrs;
    }

    public boolean isEntityTreeRootHasCollections() {
        return entityTreeRootHasCollections;
    }

    public void setEntityTreeRootHasCollections(boolean entityTreeRootHasCollections) {
        this.entityTreeRootHasCollections = entityTreeRootHasCollections;
    }

}
