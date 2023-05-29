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

import io.jmix.data.impl.jpql.*;
import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import io.jmix.data.impl.jpql.model.NoJpqlEntityModel;
import io.jmix.data.impl.jpql.pointer.EntityPointer;
import io.jmix.data.impl.jpql.pointer.NoPointer;
import io.jmix.data.impl.jpql.pointer.Pointer;
import io.jmix.data.impl.jpql.pointer.SimpleAttributePointer;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

import jakarta.annotation.Nullable;
import java.util.Deque;
import java.util.List;

public class IdentificationVariableNode extends BaseCustomNode {
    private String variableName;
    private String effectiveEntityName;

    private IdentificationVariableNode(Token token, String variableName) {
        super(token);
        this.variableName = variableName;
    }

    public IdentificationVariableNode(int type, String variableName) {
        this(new CommonToken(type, ""), variableName);
    }

    public String getVariableName() {
        return variableName;
    }

    public void identifyVariableEntity(
            DomainModel model,
            Deque<QueryVariableContext> stack,
            List<ErrorRec> invalidIdVarNodes) {
        List children = getChildren();

        if (variableName == null) {
            invalidIdVarNodes.add(new ErrorRec(this, "No entity variable name"));
            return;
        }

        if (children == null) {
            invalidIdVarNodes.add(new ErrorRec(this, "Null children"));
            return;
        }

        if (children.size() != 1) {
            invalidIdVarNodes.add(new ErrorRec(this, "Number of children not equals 1"));
            return;
        }

        if (children.get(0) instanceof CommonErrorNode) {
            invalidIdVarNodes.add(new ErrorRec(this, "Child 0 is an error node"));
            return;
        }

        CommonTree child0 = (CommonTree) children.get(0);
        String variableName = getVariableName();

        if (child0 instanceof QueryNode) {
            QueryVariableContext queryVC;
            do {
                queryVC = stack.removeLast();
            } while (!queryVC.isPropagateVariablesUpstairs());
            deductFields(queryVC, child0, model);
            stack.peekLast().addEntityVariable(variableName, queryVC.getEntity());
        } else {
            if (variableName != null) {
                try {
                    String entityName = child0.token.getText();
                    JpqlEntityModel entity = model.getEntityByName(entityName);
                    effectiveEntityName = entity.getName();
                    stack.peekLast().addEntityVariable(variableName, entity);
                } catch (UnknownEntityNameException e) {
                    stack.peekLast().addEntityVariable(variableName, NoJpqlEntityModel.getInstance());
                }
            }
        }
    }

    public void deductFields(QueryVariableContext queryVC, CommonTree node, DomainModel model) {
        List children = node.getChildren();
        CommonTree T_SELECTED_ITEMS_NODE = (CommonTree) children.get(0);
        for (Object o : T_SELECTED_ITEMS_NODE.getChildren()) {
            o = ((SelectedItemNode) o).getChild(0);
            if (!(o instanceof PathNode)) {
                throw new RuntimeException("Not a path node");
            }

            PathNode pathNode = (PathNode) o;
            Pointer pointer = pathNode.resolvePointer(model, queryVC);

            if (pointer instanceof NoPointer) {
                queryVC.setEntity(NoJpqlEntityModel.getInstance());
                return;
            }

            if (pointer instanceof SimpleAttributePointer) {
                SimpleAttributePointer saPointer = (SimpleAttributePointer) pointer;
                queryVC.getEntity().addAttributeCopy(saPointer.getAttribute());
            } else if (pointer instanceof EntityPointer) {
                if (T_SELECTED_ITEMS_NODE.getChildren().size() != 1) {
                    //todo implement
                    throw new UnsupportedOperationException("Unimplemented variant with returned array");
                } else {
                    queryVC.setEntity(((EntityPointer) pointer).getEntity());
                }
            }
        }
    }

    @Override
    public String toString() {
        return (token != null ? token.getText() : "") + "Variable: " + variableName;
    }

    @Override
    public Tree dupNode() {
        IdentificationVariableNode result = new IdentificationVariableNode(token, variableName);
        dupChildren(result);
        return result;
    }


    @Override
    public CommonTree treeToQueryPost(QueryBuilder sb, List<ErrorRec> invalidNodes) {
        sb.appendSpace();
        sb.appendString(variableName);
        return this;
    }

    public String getEntityNameFromQuery() {
        return getChild(0).getText();
    }

    @Nullable
    public String getEffectiveEntityName() {
        return effectiveEntityName;
    }
}