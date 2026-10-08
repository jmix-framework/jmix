<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<view xmlns="http://jmix.io/schema/flowui/view"
      title="${viewTitle}"
      focusComponent="form">
    <#assign properties = templateHelper.getProperties(entityMetaClass, includeProperties![], excludeProperties![])>
    <#assign collectionProperties = templateHelper.getCollectionProperties(entityMetaClass, excludeProperties![])>
    <#macro formLayout>
        <formLayout id="form" dataContainer="entityDc">
            <#list properties as property>
            ${componentXmlFactory.createComponentXml(property, null)}
            </#list>
        </formLayout>
    </#macro>
    <data>
        <instance id="entityDc"
                  class="${entityMetaClass.javaClass.name}">
                <fetchPlan extends="_base">
                    <#list properties as property>
                    <property name="${property.name}"<#if property.range.isClass()> fetchPlan="_base"</#if>/>
                    </#list>
                    <#list collectionProperties as collectionProperty>
                    <property name="${collectionProperty.name}" fetchPlan="_base"/>
                    </#list>
                </fetchPlan>
            <#list collectionProperties as collectionProperty>
            <collection id="${collectionProperty.name}Dc" property="${collectionProperty.name}"/>
            </#list>
            <loader/>
        </instance>
        <#list properties as property>
        ${componentXmlFactory.createItemsContainerXml(property)}
        </#list>
    </data>
    <facets>
        <dataLoadCoordinator auto="true"/>
    </facets>
    <actions>
        <action id="saveCloseAction" type="detail_saveClose"/>
        <action id="closeAction" type="detail_close"/>
    </actions>
    <layout>
        <#if collectionProperties?has_content>
        <tabSheet id="contentTabSheet" alignSelf="STRETCH">
            <tab id="generalTab" label="msg:///viewTemplate.generalTab">
                <@formLayout/>
            </tab>
            <#list collectionProperties as collectionProperty>
            <#assign columnProperties = templateHelper.getProperties(collectionProperty.range.asClass(), [], [])>
            <tab id="${collectionProperty.name}Tab" label="msg://${entityMetaClass.javaClass.package.name}/${entityMetaClass.javaClass.simpleName}.${collectionProperty.name}">
                <vbox id="${collectionProperty.name}TabContent" padding="false" width="100%">
                    <hbox id="${collectionProperty.name}ButtonsPanel" classNames="buttons-panel">
                        <button action="${collectionProperty.name}DataGrid.createAction"/>
                        <button action="${collectionProperty.name}DataGrid.editAction"/>
                        <button action="${collectionProperty.name}DataGrid.removeAction"/>
                    </hbox>
                    <dataGrid id="${collectionProperty.name}DataGrid"
                              dataContainer="${collectionProperty.name}Dc"
                              width="100%"
                              minHeight="20em"
                              columnReorderingAllowed="true">
                        <actions>
                            <action id="createAction" type="list_create">
                                <properties>
                                    <property name="openMode" value="DIALOG"/>
                                </properties>
                            </action>
                            <action id="editAction" type="list_edit">
                                <properties>
                                    <property name="openMode" value="DIALOG"/>
                                </properties>
                            </action>
                            <action id="removeAction" type="list_remove"/>
                        </actions>
                        <columns resizable="true">
                            <#list columnProperties as column>
                            <column property="${column.name}"/>
                            </#list>
                        </columns>
                    </dataGrid>
                </vbox>
            </tab>
            </#list>
        </tabSheet>
        <#else>
        <@formLayout/>
        </#if>
        <hbox id="detailActions">
            <button id="saveAndCloseButton" action="saveCloseAction"/>
            <button id="closeButton" action="closeAction"/>
        </hbox>
    </layout>
</view>
