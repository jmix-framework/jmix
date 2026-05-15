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

package io.jmix.texttodata.dataload.generation;

import io.jmix.texttodata.introspection.search.DomainModelSearchCandidate;

import java.util.List;

public class JpqlGenerationRequest {

    protected String userText;
    protected List<DomainModelSearchCandidate> candidates;
    protected String promptContext;

    public JpqlGenerationRequest(String userText,
                                 List<DomainModelSearchCandidate> candidates,
                                 String promptContext) {
        this.userText = userText;
        this.candidates = candidates;
        this.promptContext = promptContext;
    }

    public String getUserText() {
        return userText;
    }

    public List<DomainModelSearchCandidate> getCandidates() {
        return candidates;
    }

    public String getPromptContext() {
        return promptContext;
    }

    @Override
    public String toString() {
        return "TextToJpqlGenerationRequest{" +
                "userText='" + userText + '\'' +
                ", candidates=" + candidates +
                ", promptContext='" + promptContext + '\'' +
                '}';
    }
}
