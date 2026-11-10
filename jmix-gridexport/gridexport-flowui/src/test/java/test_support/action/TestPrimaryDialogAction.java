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

package test_support.action;

import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.kit.action.ActionVariant;

/**
 * Dialog action that declares itself primary.
 */
@ActionType(TestPrimaryDialogAction.ID)
public class TestPrimaryDialogAction extends TestDialogAction {

    public static final String ID = "test_primaryDialogAction";
    public static final String TEXT = "Primary";

    public TestPrimaryDialogAction() {
        this(ID);
    }

    public TestPrimaryDialogAction(String id) {
        super(id, TEXT);
    }

    @Override
    protected void initAction() {
        super.initAction();
        setVariant(ActionVariant.PRIMARY);
    }
}
