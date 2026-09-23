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

package test_support;

import io.jmix.aitoolsflowui.service.AiConversationTitleGenerator;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * {@link AiConversationTitleGenerator} for tests: returns a preset title or throws a preset failure.
 */
public class TestAiConversationTitleGenerator implements AiConversationTitleGenerator {

    protected Optional<String> next = Optional.empty();
    @Nullable
    protected RuntimeException failure;

    public void setNextTitle(@Nullable String title) {
        this.next = Optional.ofNullable(title);
        this.failure = null;
    }

    public void failWith(RuntimeException failure) {
        this.failure = failure;
    }

    public void reset() {
        this.next = Optional.empty();
        this.failure = null;
    }

    @Override
    public Optional<String> generateTitle(String firstUserMessage) {
        if (failure != null) {
            throw failure;
        }
        return next;
    }
}
