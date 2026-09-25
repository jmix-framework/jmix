/*
 * Copyright 2026 Haulmont.
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

package io.jmix.aitools.dataload.execution;

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.data.impl.jpql.DomainModel;
import io.jmix.data.impl.jpql.DomainModelBuilder;
import io.jmix.data.impl.jpql.QueryTree;
import io.jmix.data.impl.jpql.tree.BaseJoinNode;
import io.jmix.data.impl.jpql.tree.IdentificationVariableNode;
import io.jmix.data.impl.jpql.tree.PathNode;
import io.jmix.data.impl.jpql.tree.QueryNode;
import io.jmix.data.impl.jpql.tree.SelectionSourceNode;
import io.jmix.data.impl.jpql.tree.TreatPathNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Reads the identification variables a JPQL query declares, subqueries included, for the data-load access checks.
 * The public query parser names one outer alias per entity and no subquery variable, which is not enough to tell
 * where an entity occurs, so this reads the parser's own syntax tree.
 */
@Component("aitls_JpqlDeclarationSupport")
public class JpqlDeclarationSupport {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    @Qualifier("regular")
    protected DomainModelBuilder domainModelBuilder;

    @Nullable
    protected volatile DomainModel domainModel;

    /**
     * Reads every identification variable the query declares in {@code from}, {@code join} and {@code in (…)},
     * including subqueries.
     *
     * @param jpql query text
     * @return the declarations in the order they appear
     * @throws JpqlAccessConstraintException if a variable name is declared more than once in the query, subqueries
     *                                       included
     * @throws IllegalStateException         if a declaration cannot be read
     */
    public List<Declaration> readDeclarations(String jpql) {
        QueryTree tree = new QueryTree(getDomainModel(), jpql);
        List<Declaration> declarations = new ArrayList<>();
        CommonTree root = tree.getAstTree();
        collectDeclarations(root, root instanceof QueryNode ? 0 : 1, declarations, new HashMap<>());
        return declarations;
    }

    protected void collectDeclarations(Tree node, int queryDepth, List<Declaration> declarations,
                                       Map<String, String> variableEntities) {
        int depth = node instanceof QueryNode ? queryDepth + 1 : queryDepth;
        boolean nested = depth > 1;

        if (node instanceof SelectionSourceNode) {
            checkDeclarationsReadable(node);
        } else if (node instanceof IdentificationVariableNode variableNode) {
            String effectiveEntityName = variableNode.getEffectiveEntityName();
            String entityName = effectiveEntityName != null
                    ? effectiveEntityName
                    : variableNode.getEntityNameFromQuery();
            addDeclaration(declarations, variableEntities,
                    new Declaration(variableNode.getVariableName(), entityName, null, null, nested));
        } else if (node instanceof BaseJoinNode joinNode && joinNode.getVariableName() != null) {
            addDeclaration(declarations, variableEntities, joinDeclaration(joinNode, variableEntities, nested));
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            collectDeclarations(node.getChild(i), depth, declarations, variableEntities);
        }
    }

    /**
     * Fails on a declaration that the syntax tree keeps as bare tokens, such as a subquery's {@code from o.lines l}
     * or a {@code join fetch}. Their variables would otherwise go unseen.
     *
     * @param selectionSource a {@code from} item of the tree
     * @throws IllegalStateException if the item holds something other than declarations
     */
    protected void checkDeclarationsReadable(Tree selectionSource) {
        for (int i = 0; i < selectionSource.getChildCount(); i++) {
            Tree child = selectionSource.getChild(i);
            if (!(child instanceof IdentificationVariableNode || child instanceof BaseJoinNode
                    || child instanceof SelectionSourceNode)) {
                throw new IllegalStateException("Unsupported declaration: " + child.getText());
            }
        }
    }

    protected Declaration joinDeclaration(BaseJoinNode joinNode, Map<String, String> variableEntities,
                                          boolean nested) {
        Tree source = joinNode.getChildCount() > 0 ? joinNode.getChild(0) : null;
        if (source instanceof TreatPathNode treatNode) {
            return new Declaration(joinNode.getVariableName(), treatNode.getSubtype(), treatNode.asPathString(), null,
                    nested);
        }
        if (source instanceof PathNode pathNode) {
            String joinPath = pathNode.asPathString();
            MetaProperty joined = joinedProperty(pathNode, variableEntities);
            // A left join finding nothing is told by the owner's foreign key, which an `on` clause cannot change.
            String nullCheck = !joined.getRange().getCardinality().isMany() && metadataTools.isOwningSide(joined)
                    ? joinPath
                    : null;
            return new Declaration(joinNode.getVariableName(), joined.getRange().asClass().getName(), joinPath,
                    nullCheck, nested);
        }
        if (source != null && metadata.findClass(source.getText()) != null) {
            // An entity join: `join aitls_Customer c on ...`.
            return new Declaration(joinNode.getVariableName(), source.getText(), null, null, nested);
        }
        throw new IllegalStateException("Unsupported join declaration of " + joinNode.getVariableName());
    }

    protected void addDeclaration(List<Declaration> declarations, Map<String, String> variableEntities,
                                  Declaration declaration) {
        if (variableEntities.putIfAbsent(lowerCase(declaration.variable()), declaration.entityName()) != null) {
            throw new JpqlAccessConstraintException(String.format(
                    "Variable %s is declared more than once. A name is resolved to its first declaration wherever "
                            + "it is used, so give every declaration in the query, subqueries included, a name of "
                            + "its own", declaration.variable()));
        }
        declarations.add(declaration);
    }

    protected MetaProperty joinedProperty(PathNode pathNode, Map<String, String> variableEntities) {
        String joinPath = pathNode.asPathString();
        String ownerEntity = variableEntities.get(lowerCase(pathNode.getEntityVariableName()));
        MetaClass owner = ownerEntity != null ? metadata.findClass(ownerEntity) : null;
        MetaPropertyPath propertyPath = owner != null && joinPath.contains(".")
                ? owner.getPropertyPath(joinPath.substring(joinPath.indexOf('.') + 1))
                : null;
        if (propertyPath == null || !propertyPath.getRange().isClass()) {
            throw new IllegalStateException("Cannot resolve the entity joined by " + joinPath);
        }
        return propertyPath.getMetaProperty();
    }

    protected DomainModel getDomainModel() {
        DomainModel model = domainModel;
        if (model == null) {
            model = domainModelBuilder.produce();
            domainModel = model;
        }
        return model;
    }

    protected String lowerCase(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * An identification variable the query declares.
     *
     * @param variable   the variable
     * @param entityName the entity it ranges over
     * @param joinPath   the path it joins, or {@code null} for a {@code from} declaration or an entity join
     * @param nullCheck  the path whose {@code is null} tells that the join found no record, or {@code null}
     * @param nested     whether it is declared in a subquery
     */
    public record Declaration(String variable, String entityName, @Nullable String joinPath,
                                 @Nullable String nullCheck, boolean nested) {
    }
}
