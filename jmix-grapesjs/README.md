# Jmix GrapesJS HTML Editor

- [Overview](#overview)
- [Installation](#installation)
- [Usage](#usage)
    - [Adding HTML Editor to the Screen](#adding-html-editor-to-the-screen)
    - [UI Components](#ui-components)
    - [Commands](#commands)
    - [Custom Blocks](#custom-blocks)
    - [User Interface of HTML editor](#user-interface-of-html-editor)
        - [The Blocks Tab](#blocks)
        - [The Style Manager Tab](#the-style-manager-tab)
        - [The Trait Manager Tab](#trait)
        - [The Layers Tab](#layers)
        - [The Top Panel Buttons](#buttons)


# Overview <a name="overview"></a>

The add-on provides a visual HTML editor based on a [GrapesJs](https://grapesjs.com/) JavaScript library with the extensive set of HTML elements. It allows building HTML templates without any knowledge of coding. All you need is to drag an element into the canvas. The wide range of options enables independent styling of any element inside the canvas.

Key features:
- Wide variety of built-in HTML elements.
- Viewing representation for different devices.
- Using CSS properties.
- Downloading/uploading HTML code.

![html-editor](img/editor.gif)

# Installation <a name="installation"></a>

The add-on can be added to your project using dependencies :
```groovy
implementation 'io.jmix.grapesjs:jmix-grapesjs-starter'
}
```

Because of GrapesJS add-on uses custom styles for the GrapesJS component it is required to use a custom theme in the project instead of a compiled theme.

If a custom theme is not used in the project, it is required to create the new one. The new theme will extend one of the existing themes.

A custom theme can be created and configured using the following rules:
1. Theme must have a unique name.
2. Theme and all its resources should be located in the folder: ```src/main/themes/<theme-name>```. 
   This folder should contain ```styles.scss``` file which represents the entry point to compile the theme. <br/>
    Example of ```styles.scss``` file for the theme with the name ```custom-name```:
```
        @import "custom-theme-defaults";
        @import "addons";
        @import "custom-theme";
        
        .custom-theme {
          @include addons;
          @include custom-theme;
        }     
```        
   Here ```addons``` is a special file that is generated automatically based on used addons and themes.
3. Add ```<theme-name>-theme.properties``` file in project resources.

4. Specify theme name and path to ```<theme-name>-theme.properties``` file in ```application.properties```.<br/>
Example for the theme with the name ```custom-theme```:
```
jmix.ui.theme = custom-theme
jmix.ui.theme-config=com/company/sample/theme/custom-theme-theme.properties
```

After the theme is added and configured, add the following dependencies: 
```
dependencies {
    //...
    themes 'io.jmix.ui:jmix-ui-themes'
    themes 'io.jmix.grapesjs:jmix-grapesjs'
    //...
}
```

# Usage <a name="usage"></a>

To use the component you need to add HTML editor to the screen. Then add UI components.

## Adding HTML Editor to the Screen <a name="adding-html-editor-to-the-screen"></a>

To use the `GrapesJS` component in your screen, you need to add the special scheme `http://jmix.io/schema/grapesjs/ui` in the XML descriptor of the screen and then add a namespace like `grapesjs` for the schema. The schema contains information about the `grapesJsHtmlEditor` tag.

Look at the example of usage:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<window xmlns="http://jmix.io/schema/ui/window"
        caption="msg://caption"
        xmlns:et="http://jmix.io/schema/grapesjs/ui">
    ...
        <et:grapesJsHtmlEditor id="htmlEditor">
            <et:disabledBlocks>
                 //comma separated panel names which should be disabled, for example "map,tabs"
            </et:disabledBlocks>
        </et:grapesJsHtmlEditor>
    ...
```

`grapesjs` UI component provides `setValue(String value)` and `getValue()` methods to set and get HTML content for the component.

## UI Components <a name="ui-components"></a>

After adding HTML editor to the screen you need to add one of the following UI components:

- `grapesJsHtmlEditor` - a base HTML editor without any applied plugins.
- `grapesJsWebpageHtmlEditor` - an HTML editor suitable for webpage development with applied `webpage, customcode` plugins.
- `grapesJsNewsletterHtmlEditor` - an HTML editor suitable for newletter development with applied `newsletter, customcode` plugins.

`grapesJsNewsletterHtmlEditor` component has an additional `inlineCss`. If enabled then CSS classes will be inlined in HTML.

UI components can be extended with plugins using `plugin` tag.
A plugin can be selected from the list of predefined plugins or can be configured as a new plugin.

Default available plugins:
- `basicBlocks` - this plugin contains some basic blocks for the GrapesJS editor ([documentation](https://github.com/artf/grapesjs-blocks-basic)).
- `ckeditor` - this plugin replaces the default Rich Text Editor with the one from CKEditor ([documentation](https://github.com/artf/grapesjs-plugin-ckeditor)).
- `customcode` - this plugin adds a possibility to embed custom code ([documentation](https://github.com/artf/grapesjs-custom-code)).
- `flexBlocks` - this plugin adds the Flexbox block which allows creating easily flexible and responsive columns ([documentation](https://github.com/artf/grapesjs-blocks-flexbox))
- `forms` - this plugin adds some basic form components and blocks to make working with forms easier ([documentation](https://github.com/artf/grapesjs-plugin-forms)).
- `newsletter` - this preset configures GrapesJS to be used as a Newsletter Builder with some unique features and blocks composed specifically for being rendered correctly inside all major email clients ([documentation](https://github.com/artf/grapesjs-preset-newsletter)).
- `postcss` - this plugin enables custom CSS parser via PostCSS ([documentation](https://github.com/artf/grapesjs-parser-postcss)).
- `styleFilter` - add filter type input to the Style Manager in GrapesJS ([documentation](https://github.com/artf/grapesjs-style-filter)).
- `tabs` - simple tabs component plugin for GrapesJS ([documentation](https://github.com/artf/grapesjs-tabs)).
- `tooltip` - simple, CSS only, tooltip component for GrapesJS ([documentation](https://github.com/artf/grapesjs-tooltip)).
- `touch` - this plugin enables touch support for the GrapesJS editor ([documentation](https://github.com/artf/grapesjs-touch))
- `tuiImageEditor` - add the [TOAST UI Image Editor](https://ui.toast.com/tui-image-editor/) on Image Components in GrapesJS ([documentation](https://github.com/artf/grapesjs-tui-image-editor)).
- `webpage` - this preset configures GrapesJS to be used as a Webpage Builder ([plugin documentation](https://github.com/artf/grapesjs-preset-webpage)).

Custom project plugins can be registered via `io.jmix.grapesjs.component.GjsBlocksRepository` class.

Example:
```xml
<et:grapesJsNewsletterHtmlEditor
       id="templateEditor"
       inlineCss="true"
       height="100%" width="100%">
   <et:disabledBlocks>
       map,tabs
    </et:disabledBlocks>
   <et:plugin name="ckeditor">
       <!-- path to plugin configuration-->
       <et:optionsPath>/io/jmix/grapesjs/plugins/gjs-plugin-ckeditor.js</et:optionsPath>
   </et:plugin>
   <et:plugin name="forms"/>
   <et:plugin name="flexBlocks"/>
   <et:plugin name="tuiImageEditor"/>
   <et:plugin name="customcode"/>
   <et:plugin name="postcss"/>
   <et:plugin name="touch">
       <et:options>
           <![CDATA[
               ... custom plugin settings ...
           ]]>
       </et:options>
   </et:plugin>
   <et:plugin name="styleFilter"/>
   <et:plugin>
       <et:name>customPlugin</et:name>
       <et:options>
           <![CDATA[
               ... custom plugin settings ...
           ]]>
       </et:options>
   </et:plugin>
</et:grapesJsNewsletterHtmlEditor>
```

## Commands <a name="commands"></a>
`GrapesJsHtmlEditor` component provides two methods to run and stop commands

- `runCommand(String command)` - run command
- `stopCommand(String command)` - stop command

## Custom Blocks <a name="custom-blocks"></a>

Custom blocks can be added to the component using `block` tag with the following parameters:
- *name* - a unique block id.
- *label* - a name of the block.
- *category* - group the block inside a category.
- *content* - HTML content.
- *contentPath* - path to HTML content
- *attributes* - block attributes.

Block example:
```xml
<et:block>
 <et:name>h1-block</et:name>
 <et:label>Heading</et:label>
 <et:category>Basic</et:category>
 <et:content>
   <![CDATA[
           <h1>Put your title here</h1>
       ]]>
 </et:content>
 <et:attributes>
   <![CDATA[
           {
              title: 'Insert h1 block',
              class:'fa fa-th'
           }
       ]]>
 </et:attributes>
</et:block>
```

Custom project blocks can be registered via `io.jmix.grapesjs.component.GjsBlocksRepository` class.
Registered blocks can be added to UI component by `name` attribute. Example `<et:block name="custom block name"/>`.

Please use `class:'fa <fa-icon>'` in block attributes to use Font Awesome icon.

## User Interface of HTML editor <a name="user-interface-of-html-editor"></a>

You can add, set and delete elements from the canvas. In addition, you can import and export HTML and CSS code. Here is a description of setting panels of the editor.

### The Blocks Tab <a name="blocks"></a>

After opening the editor in your project you can see the *Blocks* tab. Set of elements depends on added [UI components](#ui-components). The following elements can be enabled for adding:

- in the *Basic* section: Columns, Text, Image, Video, Map, Link block, Text section;
- in the *Extra* section: Navbar, Tabs, Custom code;
- in the *Forms* section: Form, Input, Text area, Select, Button, Label, Checkbox, Radio.

![editor-block-manager](img/editor-block-manager.png)

### The Style Manager Tab <a name="the-style-manager-tab"></a>

You can set the properties of the elements. Select the element and go to the *Style Manager* tab.

![editor-style-manager](img/editor-style-manager.png)

### The Trait Manager Tab <a name="trait"></a>

You can define parameters of an element. Select the element and go to the *Trait Manager* tab.

![editor-trait-manager](img/editor-trait-manager.png)

### The Layers Tab <a name="layers"></a>

To watch the list of elements on the canvas go to the *Layers* tab. You can hide elements on the canvas while editing.

![editor-layers](img/editor-layers.png)

### The Top Panel Buttons <a name="buttons"></a>

The following buttons on the *Top Panel* are available:
- the *Show borders* button
- the *Preview* button
- the *Full-screen* button
- the *Export* button
- the *Undo* button
- the *Redo* button
- the *Import* button
- the *Clear canvas* button

and buttons for changing the screen width.

![editor-top-panel-buttons](img/editor-top-panel-buttons.png)
