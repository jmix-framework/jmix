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

package component.menubar.test_support;

import io.jmix.flowui.kit.component.menubar.JmixMenuBar;

/**
 * Counts button re-renders. The scheduled re-render itself cannot be observed in tests: {@code TestUI} runs
 * {@code beforeClientResponse} callbacks synchronously, which inverts the order {@code updateButtons()} relies on.
 */
public class TestMenuBar extends JmixMenuBar {

    protected int updateButtonsCalls;

    @Override
    protected void updateButtons() {
        updateButtonsCalls++;
        super.updateButtons();
    }

    public int getUpdateButtonsCalls() {
        return updateButtonsCalls;
    }

    public void resetUpdateButtonsCalls() {
        updateButtonsCalls = 0;
    }
}
