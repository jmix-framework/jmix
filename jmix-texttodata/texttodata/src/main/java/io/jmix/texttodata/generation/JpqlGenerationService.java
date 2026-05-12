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

package io.jmix.texttodata.generation;

import io.jmix.texttodata.TextToDataProperties;
import io.jmix.texttodata.introspection.search.DomainModelSearchCandidate;
import io.jmix.texttodata.introspection.search.DomainModelSearchService;
import io.jmix.texttodata.prompt.PromptContextBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("textdt_TextToJpqlGenerationService")
public class JpqlGenerationService {

    @Autowired
    protected TextToDataProperties textToDataProperties;

    @Autowired
    protected DomainModelSearchService domainModelSearchService;

    @Autowired
    protected PromptContextBuilder promptContextBuilder;

    @Autowired
    protected ObjectProvider<JpqlGenerator> jpqlGeneratorProvider;

    public JpqlGenerationRequest prepareRequest(String userText) {
        return prepareRequest(userText, textToDataProperties.getMaxEntityCandidates());
    }

    public JpqlGenerationRequest prepareRequest(String userText, int candidateLimit) {
        List<DomainModelSearchCandidate> candidates = domainModelSearchService.search(userText, candidateLimit);
        String promptContext = promptContextBuilder.build(candidates);
        return new JpqlGenerationRequest(userText, candidates, promptContext);
    }

    public GeneratedJpqlResult generate(String userText) {
        return generate(prepareRequest(userText));
    }

    public GeneratedJpqlResult generate(JpqlGenerationRequest request) {
        JpqlGenerator jpqlGenerator = jpqlGeneratorProvider.getIfAvailable();
        if (jpqlGenerator == null) {
            throw new IllegalStateException("No "+ JpqlGenerator.class.getSimpleName() + " bean is configured");
        }
        return jpqlGenerator.generate(request);
    }
}
