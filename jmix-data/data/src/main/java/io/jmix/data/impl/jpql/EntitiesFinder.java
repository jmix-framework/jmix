/*
 * Copyright 2019 Haulmont.
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

package io.jmix.data.impl.jpql;

import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import io.jmix.data.impl.jpql.pointer.HasEntityPointer;
import io.jmix.data.impl.jpql.pointer.Pointer;
import io.jmix.data.impl.jpql.tree.IdentificationVariableNode;
import io.jmix.data.impl.jpql.tree.JoinVariableNode;
import io.jmix.data.impl.jpql.tree.PathNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeVisitorAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntitiesFinder implements TreeVisitorAction {

    protected List<CommonTree> filteredNodes = new ArrayList<>();

    @Override
    @SuppressWarnings("unchecked")
    public Object pre(Object t) {
        if (t instanceof PathNode) {
            filteredNodes.add((CommonTree) t);
        }
        if (t instanceof IdentificationVariableNode) {
            filteredNodes.add((CommonTree) t);
        }
        if (t instanceof JoinVariableNode) {
            filteredNodes.add((CommonTree) t);
        }
        return t;
    }

    @Override
    public Object post(Object t) {
        return t;
    }

    @SuppressWarnings("unchecked")
    public Set<String> resolveEntityNames(DomainModel model, QueryVariableContext queryVariableContext) {
        Set<String> entityNames = new HashSet<>();
        filteredNodes.forEach(node -> {
            if (node instanceof PathNode) {
                resolveEntityNames(entityNames, (PathNode) node, model, queryVariableContext);
            }
            if (node instanceof IdentificationVariableNode) {
                resolveEntityNames(entityNames, (IdentificationVariableNode) node, model, queryVariableContext);
            }
            if (node instanceof JoinVariableNode) {
                resolveEntityNames(entityNames, (JoinVariableNode) node, model, queryVariableContext);
            }
        });
        return entityNames;
    }

    protected void resolveEntityNames(Set<String> entityNames, PathNode node, DomainModel model, QueryVariableContext queryVariableContext) {
        List<Pointer> pointers = node.resolveTransitionalPointers(model, queryVariableContext);
        pointers.stream().filter(pointer -> pointer instanceof HasEntityPointer).forEach(pointer -> {
            JpqlEntityModel entityModel = ((HasEntityPointer) pointer).getEntity();
            if (entityModel != null) {
                entityNames.add(entityModel.getName());
            }
        });
    }

    protected void resolveEntityNames(Set<String> entityNames, IdentificationVariableNode node, DomainModel model, QueryVariableContext queryVariableContext) {
        entityNames.add(node.getEffectiveEntityName());
    }

    protected void resolveEntityNames(Set<String> entityNames, JoinVariableNode node, DomainModel model, QueryVariableContext queryVariableContext) {
        JpqlEntityModel entityModel = queryVariableContext.getEntityByVariableName(node.getVariableName());
        if (entityModel != null) {
            entityNames.add(entityModel.getName());
        }
    }
}
