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

import java.util.ArrayList;
import java.util.List;

/**
 * Collects the {@link TestDialogAction} instances in the order they were created, so that a test can reach the
 * actions an export dialog has created for itself.
 */
public class TestDialogActionRecorder {

    protected final List<TestDialogAction> createdActions = new ArrayList<>();

    public void add(TestDialogAction action) {
        createdActions.add(action);
    }

    public List<TestDialogAction> getCreatedActions() {
        return createdActions;
    }

    public void reset() {
        createdActions.clear();
    }
}
