/*
 * Copyright 2021 Haulmont.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static test_support.TestBulkRequestValidationResult.Failure.create;

public class TestBulkRequestValidator {

    private static final Logger log = LoggerFactory.getLogger(TestBulkRequestValidator.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static TestBulkRequestValidationResult validate(Collection<TestBulkRequestValidationData> expectedDataSet, Collection<BulkRequest> actualBulkRequests) {
        TestBulkRequestValidationResult validationResult = new TestBulkRequestValidationResult();

        if (expectedDataSet.size() != actualBulkRequests.size()) {
            validationResult.addFailure(create(
                    String.format(
                            "Wrong amount of bulk requests: Actual=%d, Expected=%d",
                            actualBulkRequests.size(), expectedDataSet.size()
                    )
            ));
        }

        if (expectedDataSet.size() == 1 && actualBulkRequests.size() == 1) {
            TestBulkRequestValidationData expectedData = expectedDataSet.stream().findFirst().get();
            BulkRequest actualBulkRequest = actualBulkRequests.stream().findFirst().get();
            return validate(expectedData, actualBulkRequest);
        } else {
            for (TestBulkRequestValidationData expectedData : expectedDataSet) {
                TestBulkRequestValidationResult localValidationResult;
                boolean found = false;
                for (BulkRequest bulkRequest : actualBulkRequests) {
                    localValidationResult = TestBulkRequestValidator.validate(expectedData, bulkRequest);
                    if (!localValidationResult.hasFailures()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    validationResult.addFailure(create(String.format("Expected data not found: %s", expectedData.toString())));
                }
            }
            return validationResult;
        }
    }

    public static TestBulkRequestValidationResult validate(TestBulkRequestValidationData expectedData, BulkRequest actualBulkRequest) {
        TestBulkRequestValidationData actualData = convertBulkRequest(actualBulkRequest);
        return validate(expectedData, actualData);
    }

    private static TestBulkRequestValidationResult validate(TestBulkRequestValidationData expected, TestBulkRequestValidationData actual) {
        TestBulkRequestValidationResult validationResult = new TestBulkRequestValidationResult();

        if (actual.getIndexActions().size() != expected.getIndexActions().size()) {
            validationResult.addFailure(create(String.format(
                    "Wrong amount of index actions: Actual=%d, Expected=%d",
                    actual.getIndexActions().size(), expected.getIndexActions().size()
            )));
        }

        if (actual.getDeleteActions().size() != expected.getDeleteActions().size()) {
            validationResult.addFailure(create(String.format(
                    "Wrong amount of delete actions: Actual=%d, Expected=%d",
                    actual.getDeleteActions().size(), expected.getDeleteActions().size()
            )));
        }

        if (validationResult.hasFailures()) {
            return validationResult;
        }

        validateIndexActions(expected, actual, validationResult);
        validateDeleteActions(expected, actual, validationResult);
        return validationResult;
    }

    private static void validateIndexActions(
            TestBulkRequestValidationData expected,
            TestBulkRequestValidationData actual,
            TestBulkRequestValidationResult validationResult) {
        validateActions(expected.getIndexActions(), actual.getIndexActions(), validationResult);
    }

    private static void validateDeleteActions(
            TestBulkRequestValidationData expected,
            TestBulkRequestValidationData actual,
            TestBulkRequestValidationResult validationResult) {
        validateActions(expected.getDeleteActions(), actual.getDeleteActions(), validationResult);
    }

    private static <T extends TestAbstractBulkRequestActionValidationData> void validateActions(
            List<T> expectedActions,
            List<T> actualActions,
            TestBulkRequestValidationResult validationResult) {
        for (TestAbstractBulkRequestActionValidationData expectedAction : expectedActions) {
            boolean expectedActionFound = false;
            for (TestAbstractBulkRequestActionValidationData actualAction : actualActions) {
                boolean match = isActionsMatch(expectedAction, actualAction);
                if (match) {
                    expectedActionFound = true;
                    break;
                }
            }
            if (!expectedActionFound) {
                validationResult.addFailure(create("Expected action not found: " + expectedAction.toString()));
            }
        }
    }

    private static <T extends TestAbstractBulkRequestActionValidationData> boolean isActionsMatch(T expected, T actual) {
        log.debug("Compare: \n Expected:\t{}\n Actual:\t{}", expected, actual);
        return actual.equals(expected);
    }

    private static TestBulkRequestValidationData convertBulkRequest(BulkRequest bulkRequest) {
        List<TestBulkRequestIndexActionValidationData> indexActions = new ArrayList<>();
        List<TestBulkRequestDeleteActionValidationData> deleteActions = new ArrayList<>();

        bulkRequest.requests().forEach(request -> {
            switch (request.opType()) {
                case INDEX:
                    indexActions.add(convertIndexRequest((IndexRequest) request));
                    break;
                case DELETE:
                    deleteActions.add(convertDeleteRequest((DeleteRequest) request));
                    break;
                default:
            }
        });

        return new TestBulkRequestValidationData(indexActions, deleteActions);
    }

    private static TestBulkRequestIndexActionValidationData convertIndexRequest(IndexRequest indexRequest) {
        String id = indexRequest.id();
        String index = indexRequest.index();
        JsonNode source = objectMapper.convertValue(indexRequest.sourceAsMap(), JsonNode.class);
        return new TestBulkRequestIndexActionValidationData(index, id, source);
    }

    private static TestBulkRequestDeleteActionValidationData convertDeleteRequest(DeleteRequest deleteRequest) {
        String id = deleteRequest.id();
        String index = deleteRequest.index();
        return new TestBulkRequestDeleteActionValidationData(index, id);
    }
}
