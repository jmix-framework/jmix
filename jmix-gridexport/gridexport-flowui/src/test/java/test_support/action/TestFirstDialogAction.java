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

/**
 * Dialog action shown as "First".
 */
@ActionType(TestFirstDialogAction.ID)
public class TestFirstDialogAction extends TestDialogAction {

    public static final String ID = "test_firstDialogAction";
    public static final String TEXT = "First";

    public TestFirstDialogAction() {
        this(ID);
    }

    public TestFirstDialogAction(String id) {
        super(id, TEXT);
    }
}
