/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.impl.controller;

import io.jmix.rest.impl.service.filter.data.EnumInfo;
import io.jmix.rest.impl.service.EnumsControllerManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * REST controller that is used for getting an information about enums
 */
@RestController("rest_EnumsController")
@RequestMapping(path = "/rest/metadata/enums", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class EnumsController {
    @Autowired
    protected EnumsControllerManager enumsControllerManager;

    @GetMapping
    public List<EnumInfo> getAllEnumInfos() {
        return enumsControllerManager.getAllEnumInfos();
    }

    @GetMapping("/{enumClassName:.+}")
    public EnumInfo getEnumInfo(@PathVariable String enumClassName) {
        return enumsControllerManager.getEnumInfo(enumClassName);
    }
}
