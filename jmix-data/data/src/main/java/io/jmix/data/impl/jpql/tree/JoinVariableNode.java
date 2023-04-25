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

package io.jmix.data.impl.jpql.tree;

import io.jmix.data.impl.jpql.ErrorRec;
import io.jmix.data.impl.jpql.QueryBuilder;
import io.jmix.data.impl.jpql.TreeToQuery;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeVisitor;

import jakarta.annotation.Nullable;
import java.util.List;

public class JoinVariableNode extends BaseJoinNode {
    private String joinSpec;

    public JoinVariableNode(Token token, String joinSpec, String variableName) {
        super(token, variableName);
        this.joinSpec = joinSpec;
    }

    public JoinVariableNode(int type, String joinSpec, String variableName) {
        this(new CommonToken(type, ""), joinSpec, variableName);
    }

    @Override
    public String toString() {
        return (token != null ? token.getText() : "") + "Join variable: " + variableName;
    }

    @Override
    public Tree dupNode() {
        JoinVariableNode result = new JoinVariableNode(token, joinSpec, variableName);
        dupChildren(result);
        return result;
    }

    @Override
    public CommonTree treeToQueryPre(QueryBuilder sb, List<ErrorRec> invalidNodes) {
        int childCount = getChildCount();
        if (childCount == 0) {
            invalidNodes.add(new ErrorRec(this, "No children found"));
            return null;
        }
        sb.appendSpace();
        sb.appendString(joinSpec);
        sb.appendSpace();
        sb.appendString(toQuery(getChild(0)));
        sb.appendSpace();
        sb.appendString(variableName);
        if (childCount > 1) {
            sb.appendSpace();
            sb.appendString("on");
            for (int idx = 1; idx < childCount; idx++) {
                sb.appendSpace();
                sb.appendString(toQuery(getChild(idx)));
            }
        }
        return null;
    }

    @Nullable
    public PathNode findPathNode() {
        for (Object child : getChildren()) {
            if (child instanceof PathNode) {
                return (PathNode) child;
            }
        }
        return null;
    }

    public String getJoinSpec() {
        return joinSpec;
    }

    protected String toQuery(Tree tree) {
        TreeVisitor visitor = new TreeVisitor();
        TreeToQuery treeToQuery = new TreeToQuery();
        visitor.visit(tree, treeToQuery);
        return treeToQuery.getQueryString().trim();
    }
}