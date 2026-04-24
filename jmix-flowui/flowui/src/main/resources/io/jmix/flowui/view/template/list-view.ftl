<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<view xmlns="http://jmix.io/schema/flowui/view"
      xmlns:c="http://jmix.io/schema/flowui/jpql-condition"
      title="${viewTitle}"
      focusComponent="dataGrid">
    <data>
        <collection id="entityDc"
                    class="${entityMetaClass.javaClass.name}">
            <fetchPlan extends="_base">
                <#list entityMetaClass.properties as property>
                <#if !property.range.cardinality.isMany()>
                <property name="${property.name}"<#if property.range.isClass()> fetchPlan="_base"</#if>/>
                </#if>
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
                <button id="createButton" action="dataGrid.createAction"/>
                <button id="editButton" action="dataGrid.editAction"/>
                <button id="removeButton" action="dataGrid.removeAction"/>
            </startSlot>
            <endSlot>
                <simplePagination id="pagination" dataLoader="entityDl"/>
            </endSlot>
        </hbox>
        <dataGrid id="dataGrid"
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
                <#list entityMetaClass.properties as property>
                <#if !property.range.cardinality.isMany()>
                <column property="${property.name}"/>
                </#if>
                </#list>
            </columns>
        </dataGrid>
        <hbox id="lookupActions" visible="false">
            <button id="selectButton" action="selectAction"/>
            <button id="discardButton" action="discardAction"/>
        </hbox>
    </layout>
</view>
