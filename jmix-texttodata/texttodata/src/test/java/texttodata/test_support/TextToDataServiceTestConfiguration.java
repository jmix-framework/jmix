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

package texttodata.test_support;

import io.jmix.texttodata.dataload.generation.*;
import io.jmix.texttodata.dataload.repair.JpqlRepairer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TextToDataServiceTestConfiguration {

    @Bean
    JpqlGenerator testTextToJpqlGenerator() {
        return request -> {
            if (request.getUserText().contains("limit")) {
                return paginatedResult();
            }
            return isInvalidRequest(request) ? invalidResult() : validResult();
        };
    }

    @Bean
    JpqlRepairer testTextToJpqlRepairer() {
        return request -> request.getGenerationRequest().getUserText().contains("forever")
                ? invalidResult()
                : validResult();
    }

    protected boolean isInvalidRequest(JpqlGenerationRequest request) {
        return request.getUserText().contains("invalid");
    }

    protected GeneratedJpqlResult validResult() {
        return new GeneratedJpqlResult(
                "select e from textdt_Order e where e.customer.name like :customerName",
                "textdt_Order",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                List.of("textdt_Order", "textdt_Customer"),
                List.of("customer.name"),
                "Valid test result",
                List.of()
        );
    }

    protected GeneratedJpqlResult invalidResult() {
        return new GeneratedJpqlResult(
                "select e from textdt_Order e where e.customer.fullTitle like :customerName",
                "textdt_Order",
                List.of(new GeneratedJpqlParameter("customerName", "String", "%Acme%")),
                List.of("textdt_Order", "textdt_Customer"),
                List.of("customer.fullTitle"),
                "Invalid test result",
                List.of()
        );
    }

    protected GeneratedJpqlResult paginatedResult() {
        return new GeneratedJpqlResult(
                "select e from textdt_Order e limit 10 offset 5",
                "textdt_Order",
                List.of(),
                List.of("textdt_Order"),
                List.of(),
                "Paginated test result",
                List.of()
        );
    }
}
