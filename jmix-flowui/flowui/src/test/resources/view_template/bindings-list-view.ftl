<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<view xmlns="http://jmix.io/schema/flowui/view"
      xmlns:c="http://jmix.io/schema/flowui/jpql-condition"
      title="${viewTitle}"
      focusComponent="customersGrid">
    <#assign properties = templateHelper.getProperties(entityMetaClass, includeProperties![], excludeProperties![])>
    <data>
        <collection id="entityDc"
                    class="${entityMetaClass.javaClass.name}">
            <fetchPlan extends="_base">
                <#list properties as property>
                <property name="${property.name}"<#if property.range.isClass()> fetchPlan="_base"</#if>/>
                </#list>
            </fetchPlan>
            <loader id="entityDl" readOnly="true">
                <query>
                    <![CDATA[select e from ${entityMetaClass.name} e]]>
                </query>
            </loader>
        </collection>
    </data>
    <facets>
        <dataLoadCoordinator auto="true"/>
        <urlQueryParameters>
            <genericFilter component="genericFilter"/>
            <pagination component="pagination"/>
        </urlQueryParameters>
    </facets>
    <actions>
        <action id="selectAction" type="lookup_select"/>
        <action id="discardAction" type="lookup_discard"/>
    </actions>
    <layout>
        <genericFilter id="genericFilter" dataLoader="entityDl">
            <properties include=".*"/>
        </genericFilter>
        <hbox id="buttonsPanel" classNames="buttons-panel">
            <startSlot>
                <button id="createButton" action="customersGrid.createAction"/>
                <button id="editButton" action="customersGrid.editAction"/>
                <button id="removeButton" action="customersGrid.removeAction"/>
            </startSlot>
            <endSlot>
                <simplePagination id="pagination" dataLoader="entityDl"/>
            </endSlot>
        </hbox>
        <dataGrid id="customersGrid"
                  width="100%"
                  columnReorderingAllowed="true"
                  minHeight="20em"
                  dataContainer="entityDc">
            <actions>
                <action id="createAction" type="list_create"/>
                <action id="editAction" type="list_edit"/>
                <action id="removeAction" type="list_remove"/>
            </actions>
            <columns resizable="true">
                <#list properties as property>
                <column property="${property.name}"/>
                </#list>
            </columns>
        </dataGrid>
        <hbox id="lookupActions" visible="false">
            <button id="selectButton" action="selectAction"/>
            <button id="discardButton" action="discardAction"/>
        </hbox>
    </layout>
</view>
