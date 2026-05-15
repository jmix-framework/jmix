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

package io.jmix.aitools.dataload.postprocess;

import io.jmix.aitools.dataload.generation.GeneratedJpqlResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("textdt_JpqlPostProcessingService")
public class JpqlPostProcessingService {

    @Autowired(required = false)
    protected List<JpqlResultPostProcessor> postProcessors = List.of();

    public PostProcessedResult process(GeneratedJpqlResult generatedJpqlResult) {
        PostProcessedResult postProcessedResult = new PostProcessedResult(
                generatedJpqlResult,
                generatedJpqlResult.getMaxResults(),
                generatedJpqlResult.getFirstResult()
        );

        for (JpqlResultPostProcessor postProcessor : postProcessors) {
            postProcessedResult = postProcessor.process(postProcessedResult);
        }

        return postProcessedResult;
    }
}
