///*
// * Copyright (c) 2008-2020 Haulmont.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package io.jmix.reports.role;
//TODO Minimal role
//import com.haulmont.cuba.security.app.role.AnnotatedRoleDefinition;
//import com.haulmont.cuba.security.app.role.annotation.EntityAccess;
//import com.haulmont.cuba.security.app.role.annotation.EntityAttributeAccess;
//import com.haulmont.cuba.security.app.role.annotation.Role;
//import com.haulmont.cuba.security.app.role.annotation.ScreenAccess;
//import io.jmix.core.security.EntityOp;
//import com.haulmont.cuba.security.role.EntityAttributePermissionsContainer;
//import com.haulmont.cuba.security.role.EntityPermissionsContainer;
//import com.haulmont.cuba.security.role.ScreenPermissionsContainer;
//import io.jmix.reports.entity.Report;
//import io.jmix.reports.entity.ReportGroup;
//import io.jmix.reports.entity.ReportTemplate;
//import io.jmix.security.role.annotation.Role;
//
///**
// * System role that grants minimal permissions for run reports required for all users of generic UI client.
// */
//@Role(name = ReportsMinimalRoleDefinition.ROLE_NAME, code = ReportsMinimalRoleDefinition.ROLE_NAME)
//public class ReportsMinimalRoleDefinition extends AnnotatedRoleDefinition {
//
//    public static final String ROLE_NAME = "system-reports-minimal";
//
//    @Override
//    @ScreenAccess(screenIds = {
//            "report$inputParameters",
//            "report$Report.run",
//            "report$showReportTable",
//            "report$showPivotTable",
//            "report$showChart",
//            "commonLookup"
//    })
//    public ScreenPermissionsContainer screenPermissions() {
//        return super.screenPermissions();
//    }
//
//    @Override
//    @EntityAccess(entityClass = Report.class, operations = {EntityOp.READ})
//    @EntityAccess(entityClass = ReportGroup.class, operations = {EntityOp.READ})
//    @EntityAccess(entityClass = ReportTemplate.class, operations = {EntityOp.READ})
//    public EntityPermissionsContainer entityPermissions() {
//        return super.entityPermissions();
//    }
//
//    @Override
//    @EntityAttributeAccess(entityClass = Report.class, view = {"locName", "description", "code", "updateTs", "group"})
//    @EntityAttributeAccess(entityClass = ReportGroup.class, view = {"title", "localeNames"})
//    @EntityAttributeAccess(entityClass = ReportTemplate.class, view = {"code","name","customDefinition","custom","alterable"})
//    public EntityAttributePermissionsContainer entityAttributePermissions() {
//        return super.entityAttributePermissions();
//    }
//
//    @Override
//    public String getLocName() {
//        return "Reports Minimal";
//    }
//}
