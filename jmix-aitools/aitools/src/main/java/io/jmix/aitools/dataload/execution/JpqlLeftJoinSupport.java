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

import io.jmix.data.impl.jpql.JPA2RecognitionException;
import io.jmix.data.impl.jpql.NodesFinder;
import io.jmix.data.impl.jpql.Parser;
import io.jmix.data.impl.jpql.QueryTree;
import io.jmix.data.impl.jpql.TreeToQuery;
import io.jmix.data.impl.jpql.tree.BaseJoinNode;
import io.jmix.data.impl.jpql.tree.GroupByNode;
import io.jmix.data.impl.jpql.tree.IdentificationVariableNode;
import io.jmix.data.impl.jpql.tree.JoinVariableNode;
import io.jmix.data.impl.jpql.transform.QueryTreeTransformer;
import io.jmix.data.impl.jpql.tree.PathNode;
import io.jmix.data.impl.jpql.tree.QueryNode;
import io.jmix.data.impl.jpql.tree.SelectedItemsNode;
import io.jmix.data.impl.jpql.tree.SelectionSourceNode;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeVisitor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Narrows the left joins of a data-load query in their own {@code on} clause, so that a record hidden from the
 * current user is joined as an absent one instead of removing the row. The public {@code QueryTransformer} can only
 * add to the {@code where}, so this rewrites the parser's syntax tree.
 */
@Component("aitls_JpqlLeftJoinSupport")
public class JpqlLeftJoinSupport {

    private static final Logger log = LoggerFactory.getLogger(JpqlLeftJoinSupport.class);

    @Autowired
    protected JpqlDeclarationSupport declarationSupport;

    /**
     * Starts a rewrite of the query.
     *
     * @param jpql query text the JPQL parser can read
     * @return the rewrite; its result is the query text with every change applied
     * @throws JPA2RecognitionException if the JPQL parser cannot read the text
     */
    public Rewrite rewrite(String jpql) {
        return new Rewrite(new QueryTree(declarationSupport.getDomainModel(), jpql));
    }

    /**
     * Changes to one query, applied to its syntax tree in the order they are made.
     */
    public static class Rewrite {

        protected static final String VARIABLE_PREFIX = "aitlsJoin";

        protected final QueryTree tree;
        protected final Set<String> usedVariables = new HashSet<>();
        protected int variableCounter;

        protected Rewrite(QueryTree tree) {
            this.tree = tree;
            for (IdentificationVariableNode node
                    : tree.visit(NodesFinder.of(IdentificationVariableNode.class)).getFoundNodes()) {
                usedVariables.add(lowerCase(node.getVariableName()));
            }
            for (BaseJoinNode node : tree.visit(NodesFinder.of(BaseJoinNode.class)).getFoundNodes()) {
                if (node.getVariableName() != null) {
                    usedVariables.add(lowerCase(node.getVariableName()));
                }
            }
        }

        protected static String lowerCase(String name) {
            return name.toLowerCase(Locale.ROOT);
        }

        /**
         * Returns a variable name the query does not declare and this rewrite has not returned before.
         *
         * @return the variable name
         */
        public String newVariable() {
            String name;
            do {
                name = VARIABLE_PREFIX + ++variableCounter;
            } while (!usedVariables.add(lowerCase(name)));
            return name;
        }

        /**
         * Adds a condition to the main query's {@code where}, in conjunction with its own condition if any.
         *
         * @param condition JPQL condition
         * @throws IllegalStateException if the condition cannot be parsed
         */
        public void addWhereCondition(String condition) {
            new QueryTreeTransformer(tree).mixinWhereConditionsIntoTree(parseCondition(condition));
        }

        /**
         * Adds a condition to the {@code on} clause of the join declaring the variable, in conjunction with the
         * clause's own condition if any.
         *
         * @param variable  variable declared by a join of the query
         * @param condition JPQL condition
         * @throws IllegalStateException if no join declares the variable, or the condition cannot be parsed
         */
        public void addOnCondition(String variable, String condition) {
            JoinVariableNode join = findJoin(variable);
            String combined = join.getChildCount() > 1
                    ? "(" + onConditionText(join) + ") and (" + condition + ")"
                    : condition;
            CommonTree parsed = parseCondition(combined);

            while (join.getChildCount() > 1) {
                join.deleteChild(1);
            }
            for (Object child : new ArrayList<>(parsed.getChildren())) {
                join.addChild((Tree) child);
            }
            join.freshenParentAndChildIndexes();
        }

        /**
         * Joins the entity a to-one reference path leads to under a new variable, and points every path reading
         * the primary key through that reference at the new variable instead; a {@code group by} item equal to the
         * reference path becomes the new variable's key too, so that the selected key stays a grouped expression.
         * A path in the {@code on} clause of the inner join declaring the path's variable is left as it is, since
         * the new join has to follow that declaration.
         *
         * @param referencePath path from a variable of the main query to a to-one reference, such as
         *                      {@code d.parent}
         * @param primaryKey    name of the referenced entity's primary key attribute
         * @return the new variable, if any, and whether some paths were left in an inner join's {@code on}
         * @throws IllegalArgumentException      if the path is not a variable followed by one attribute
         * @throws IllegalStateException         if the path's variable is not declared in the main query's
         *                                       {@code from}, or the query reads no primary key through the path
         * @throws JpqlAccessConstraintException if such a path occurs in the {@code on} clause of the left join
         *                                       declaring its variable: the new join cannot precede that join, and
         *                                       the path left as it is would read a hidden record. The query is
         *                                       left unchanged.
         */
        public JoinedIdPath joinIdPath(String referencePath, String primaryKey) {
            int dot = referencePath.indexOf('.');
            if (dot < 1 || dot == referencePath.length() - 1 || referencePath.indexOf('.', dot + 1) >= 0) {
                throw new IllegalArgumentException("Not a reference of a variable: " + referencePath);
            }
            String owner = referencePath.substring(0, dot);
            String reference = referencePath.substring(dot + 1);

            DeclarationSite declaration = declarationOf(owner);
            List<PathNode> inOwnerOn = new ArrayList<>();
            if (declaration.node() instanceof JoinVariableNode join) {
                for (int i = 1; i < join.getChildCount(); i++) {
                    inOwnerOn.addAll(idPaths(join.getChild(i), owner, reference, primaryKey));
                }
                if (!inOwnerOn.isEmpty() && JpqlDeclarationSupport.isLeftJoin(join)) {
                    throw new JpqlAccessConstraintException(String.format(
                            "The query reads %1$s.%2$s.%3$s in the on clause of the join declaring %1$s, where it "
                                    + "cannot be narrowed to the records available to the current user. To test "
                                    + "whether %1$s refers to a record, compare the reference itself: "
                                    + "%1$s.%2$s is not null reads only the foreign key of %1$s. To compare the key "
                                    + "with a value, join %1$s.%2$s with a variable of its own and use that "
                                    + "variable's %3$s; a condition in the where clause removes rows that the left "
                                    + "join keeps",
                            owner, reference, primaryKey));
                }
            }

            List<PathNode> toRewrite = new ArrayList<>(idPaths(tree.getAstTree(), owner, reference, primaryKey));
            toRewrite.removeIf(path -> inOwnerOn.stream().anyMatch(kept -> kept == path));
            if (toRewrite.isEmpty() && inOwnerOn.isEmpty()) {
                // The caller found the path in the query: a path left unrewritten would read a hidden record.
                throw new IllegalStateException("The query reads no primary key through " + referencePath);
            }
            if (toRewrite.isEmpty()) {
                return new JoinedIdPath(null, true, false);
            }

            boolean selected = toRewrite.stream().anyMatch(this::isInMainSelect);
            String alias = newVariable();
            for (PathNode path : toRewrite) {
                path.deleteChild(0);
                path.renameVariableTo(alias);
            }
            // `group by <owner>.<reference>` groups by the owner's foreign key; the key now selected is the new
            // variable's, so the grouping follows it, or the selected key is no longer a grouped expression.
            for (PathNode path : referencePathsInGroupBy(owner, reference)) {
                path.deleteChild(0);
                path.addDefaultChild(primaryKey);
                path.renameVariableTo(alias);
            }
            // Right after the owner's declaration: an earlier join's `on` may read the alias, and a variable
            // must be declared before it is used.
            declaration.source().insertChild(declaration.index() + 1,
                    parseJoin("left join " + referencePath + " " + alias));
            declaration.source().freshenParentAndChildIndexes();
            return new JoinedIdPath(alias, !inOwnerOn.isEmpty(), selected);
        }

        /**
         * Returns the query text with every change applied.
         *
         * @return query text
         */
        public String getResult() {
            return tree.visit(new TreeToQuery()).getQueryString().trim();
        }

        protected JoinVariableNode findJoin(String variable) {
            List<JoinVariableNode> found = tree.visit(NodesFinder.of(JoinVariableNode.class)).getFoundNodes().stream()
                    .filter(node -> variable.equalsIgnoreCase(node.getVariableName()))
                    .toList();
            if (found.size() != 1) {
                throw new IllegalStateException("No single join declares " + variable);
            }
            return found.get(0);
        }

        protected List<PathNode> idPaths(Tree root, String owner, String reference, String primaryKey) {
            NodesFinder<PathNode> finder = NodesFinder.of(PathNode.class);
            new TreeVisitor().visit(root, finder);
            return finder.getFoundNodes().stream()
                    .filter(path -> isIdPath(path, owner, reference, primaryKey))
                    .toList();
        }

        protected boolean isInMainSelect(PathNode path) {
            for (Tree node = path.getParent(); node != null; node = node.getParent()) {
                if (node instanceof SelectedItemsNode) {
                    return node.getParent() == tree.getAstTree();
                }
                if (node instanceof QueryNode) {
                    return false;
                }
            }
            return false;
        }

        protected List<PathNode> referencePathsInGroupBy(String owner, String reference) {
            List<PathNode> found = new ArrayList<>();
            for (GroupByNode groupBy : tree.visit(NodesFinder.of(GroupByNode.class)).getFoundNodes()) {
                NodesFinder<PathNode> finder = NodesFinder.of(PathNode.class);
                new TreeVisitor().visit(groupBy, finder);
                for (PathNode path : finder.getFoundNodes()) {
                    if (owner.equalsIgnoreCase(path.getEntityVariableName())
                            && path.getChildCount() == 1
                            && reference.equals(path.getChild(0).getText())) {
                        found.add(path);
                    }
                }
            }
            return found;
        }

        protected boolean isIdPath(PathNode path, String owner, String reference, String primaryKey) {
            return owner.equalsIgnoreCase(path.getEntityVariableName())
                    && path.getChildCount() == 2
                    && reference.equals(path.getChild(0).getText())
                    && primaryKey.equals(path.getChild(1).getText());
        }

        protected DeclarationSite declarationOf(String variable) {
            CommonTree from = tree.getAstFromNode();
            for (int i = 0; i < from.getChildCount(); i++) {
                if (!(from.getChild(i) instanceof SelectionSourceNode source)) {
                    continue;
                }
                for (int j = 0; j < source.getChildCount(); j++) {
                    Tree child = source.getChild(j);
                    if (child instanceof IdentificationVariableNode fromNode
                            && variable.equalsIgnoreCase(fromNode.getVariableName())
                            || child instanceof BaseJoinNode joinNode
                            && variable.equalsIgnoreCase(joinNode.getVariableName())) {
                        return new DeclarationSite(source, j, child);
                    }
                }
            }
            throw new IllegalStateException("Variable " + variable + " is not declared in the main query");
        }

        protected String onConditionText(JoinVariableNode join) {
            List<String> parts = new ArrayList<>();
            for (int i = 1; i < join.getChildCount(); i++) {
                parts.add(toQuery(join.getChild(i)));
            }
            return String.join(" ", parts);
        }

        protected String toQuery(Tree node) {
            TreeToQuery treeToQuery = new TreeToQuery();
            new TreeVisitor().visit(node, treeToQuery);
            return treeToQuery.getQueryString().trim();
        }

        protected CommonTree parseCondition(String condition) {
            try {
                return Parser.parseWhereClause("where " + condition);
            } catch (RecognitionException | RuntimeException e) {
                // The condition carries row-level policy text, which the message must not pass on to the caller.
                log.debug("Cannot parse condition [{}]", condition, e);
                throw new IllegalStateException("Cannot parse a condition to add to the query");
            }
        }

        protected JoinVariableNode parseJoin(String join) {
            try {
                return Parser.parseJoinClause(join).get(0);
            } catch (RecognitionException | RuntimeException e) {
                log.debug("Cannot parse join [{}]", join, e);
                throw new IllegalStateException("Cannot parse a join to add to the query");
            }
        }

        /**
         * What {@link #joinIdPath(String, String)} did to the paths reading a primary key through a reference.
         *
         * @param variable          the variable now joining the reference, or {@code null} when every such path
         *                          was left in an inner join's {@code on}
         * @param leftInInnerJoinOn whether some paths were left in the {@code on} of the inner join declaring the
         *                          path's variable; the reference still has to be narrowed in the {@code where}
         * @param selected          whether a rewritten path is selected by the main query. A selected path is an
         *                          inner join, which the added left join is not: the caller keeps the rows without
         *                          a reference out
         */
        public record JoinedIdPath(@Nullable String variable, boolean leftInInnerJoinOn, boolean selected) {
        }

        /**
         * A variable's declaration: the selection source holding it, its child index there and the node itself.
         */
        protected record DeclarationSite(SelectionSourceNode source, int index, Tree node) {
        }
    }
}
