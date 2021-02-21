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

package serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ChartSampleJsonHelper {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private ChartSampleJsonHelper() {
    }

    public static String readFile(String fileName) throws IOException, URISyntaxException {
        URL resource = ChartSampleJsonHelper.class.getResource("/serialization/" + fileName);
        byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String prettyJson(String json) {
        JsonParser parser = new JsonParser();
        JsonElement parsedJson = parser.parse(json);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(parsedJson);
    }
}