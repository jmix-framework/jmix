<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<view xmlns="http://jmix.io/schema/flowui/view"
      title="${viewTitle}"
      focusComponent="form">
    <#assign properties = templateHelper.getProperties(entityMetaClass, includeProperties![], excludeProperties![])>
    <data>
        <instance id="entityDc"
                  class="${entityMetaClass.javaClass.name}">
                <fetchPlan extends="_base">
                    <#list properties as property>
                    <property name="${property.name}"<#if property.range.isClass()> fetchPlan="_base"</#if>/>
                    </#list>
                </fetchPlan>
            <loader/>
        </instance>
    </data>
    <facets>
        <dataLoadCoordinator auto="true"/>
    </facets>
    <actions>
        <action id="saveCloseAction" type="detail_saveClose"/>
        <action id="closeAction" type="detail_close"/>
    </actions>
    <layout>
        <formLayout id="form" dataContainer="entityDc">
            <#list properties as property>
            ${componentXmlFactory.createComponentXml(property, null)}
            </#list>
        </formLayout>
        <hbox id="detailActions">
            <button id="saveAndCloseButton" action="saveCloseAction"/>
            <button id="closeButton" action="closeAction"/>
        </hbox>
    </layout>
</view>
