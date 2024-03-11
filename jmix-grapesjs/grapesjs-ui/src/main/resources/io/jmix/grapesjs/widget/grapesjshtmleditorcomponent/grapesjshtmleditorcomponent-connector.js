io_jmix_grapesjs_widget_grapesjshtmleditorcomponent_GrapesJsHtmlEditorComponent = function() {
    var connector = this;
    var element = connector.getElement();
    var state = connector.getState();

    $(element).html("<div id=\"gjs\" style=\"height:100%\"></div>");
    $(element).css("width", "100%");
    $(element).css("height", "100%");

    var defaultConfig = {
        avoidInlineStyle: 1,
        height: '100%',
        container: '#gjs',
        forceClass: false,
        components: connector.getState().html,
        showOffsets: 1,
        domComponents: {
            draggableComponents: false
        },
        assetManager: {
            embedAsBase64: 1,
            //          assets: images
        },
        styleManager: {
            clearProperties: 1
        },
        colorPicker: { appendTo: 'parent', offset: { top: 26, left: -155, }, },
        plugins: [],
        pluginsOpts: {},

    };

    var pluginsSize = defaultConfig.plugins.length;
    if (state.plugins != null) {
        state.plugins.forEach(function(item, i, arr) {
            var pluginName = item.name;
            var pluginOptions = item.options;
            defaultConfig.plugins[pluginsSize + i] = pluginName;

            if (pluginOptions != null) {
                defaultConfig.pluginsOpts[pluginName] = strictEvalFunc('(' + pluginOptions + ')');
            }
        });

    }
    var editor = grapesjs.init(defaultConfig);

    clear(editor);

    var pn = editor.Panels;
    var modal = editor.Modal;
    editor.Commands.add('canvas-clear', function() {
        if (confirm('Are you sure to clean the canvas?')) {
            var comps = editor.DomComponents.clear();
            setTimeout(function() {
                localStorage.clear()
            }, 0)
            var tmpl = getHtml(editor, state);
            connector.valueChanged(tmpl);
        }
    });

    // Show borders by default
    pn.getButton('options', 'sw-visibility').set('active', 1);

    // remove default buttons before we add custom ones
    pn.removeButton('options', 'undo');
    pn.removeButton('options', 'redo');
    pn.removeButton('options', 'preview');
    pn.removeButton('options', 'canvas-clear');

    pn.addButton('options', [{
        id: 'preview',
        className: 'fa fa-eye icon-blank',
        attributes: {title: 'Preview'},
        command: e => e.runCommand('preview'),
      }, {
        id: 'undo',
        className: 'fa fa-undo',
        attributes: {title: 'Undo'},
        command: function(){ editor.runCommand('core:undo') }
      },{
        id: 'redo',
        className: 'fa fa-repeat',
        attributes: {title: 'Redo'},
        command: function(){ editor.runCommand('core:redo') }
      },{
        id: 'clear-all',
        className: 'fa fa-trash icon-blank',
        attributes: {title: 'Clear canvas'},
        command: {
          run: function(editor, sender) {
            sender && sender.set('active', false);
            if(confirm('Are you sure to clean the canvas?')){
              editor.DomComponents.clear();
              setTimeout(function(){
                clear(editor)
                var tmpl = getHtml(editor, state);
                if (tmpl) {
                    connector.valueChanged(tmpl);
                }
              },0)
            }
          }
        }
      }]);

    // Store and load events
    editor.on('storage:load', function(e) {
        console.log('Loaded ', e)
    });
    editor.on('storage:store', function(e) {
        console.log('Stored ', e)
    });

    let cmdm = editor.Commands;
    var opt = {};

    // Do stuff on load
    editor.on('load', function() {
        var $ = grapesjs.$;

        // Load and show settings and style manager
        var openTmBtn = pn.getButton('views', 'open-tm');
        openTmBtn && openTmBtn.set('active', 1);
        var openSm = pn.getButton('views', 'open-sm');
        openSm && openSm.set('active', 1);

        // Open block manager
        var openBlocksBtn = editor.Panels.getButton('views', 'open-blocks');
        openBlocksBtn && openBlocksBtn.set('active', 1);
    });

    connector.getState().disabledBlocks.forEach(function(entry) {
        editor.BlockManager.getAll().remove(entry);
    });

    state.blocks.forEach(function(item, i, arr) {
        editor.BlockManager.add(item.name, {
            label: item.label,
            content: item.content,
            category: item.category,
            attributes: strictEvalFunc('(' + item.attributes + ')')
        });
    });

    editor.on('change:changesCount', (component, argument) => {
        var tmpl = getHtml(editor, state);
        if (tmpl) {
            connector.valueChanged(tmpl);
        }
    });

    editor.on('undo', (component, argument) => {
        var tmpl = getHtml(editor, state);
        if (tmpl) {
            connector.valueChanged(tmpl);
        }
    });

    editor.on('redo', (component, argument) => {
        var tmpl = getHtml(editor, state);
        if (tmpl) {
            connector.valueChanged(tmpl);
        }
    });

    connector.onStateChange = function() {
        var state = connector.getState();
        editor.setComponents(state.html);
    }

    connector.runCommand = function (command) {
        editor.runCommand(command)
    };

    connector.stopCommand = function (command) {
            editor.stopCommand(command)
        };

    editor.onReady(function () {
        // Wait for CKEDITOR load
        setTimeout(() => {
            if (CKEDITOR) {
                CKEDITOR.dtd.$editable.span = 1
                CKEDITOR.dtd.$editable.a = 1
                CKEDITOR.dtd.$editable.strong = 1
                CKEDITOR.dtd.$editable.b = 1
                CKEDITOR.dtd.$editable.i = 1
                CKEDITOR.dtd.$editable.li = 1
            }
        }, 200);
    });
}

function getHtml(editor, state) {
    if (state.inlineCss) {
        return editor.runCommand('gjs-get-inlined-html');
    } else {
        return editor.getHtml() + `<style>${editor.getCss()}</style>`;
    }
}

function clear(editor) {
     var comps = editor.DomComponents.clear();
     localStorage.clear();
}

function strictEvalFunc(code) {
    'use strict';
    return eval.call(null, code);
}