{
  position: 'center',
  options: {
    startupFocus: true,
    extraAllowedContent: '*(*);*{*}',
    allowedContent: true,
    enterMode: 2, // CKEDITOR.ENTER_BR
    extraPlugins: 'sharedspace,justify,colorbutton,panelbutton,font',
    toolbar: [{
      name: 'styles',
      items: ['Font', 'FontSize']
    },
      ['Bold', 'Italic', 'Underline', 'Strike'],
      {
        name: 'paragraph',
        items: ['NumberedList', 'BulletedList']
      },
      {
        name: 'links',
        items: ['Link', 'Unlink']
      },
      {
        name: 'colors',
        items: ['TextColor', 'BGColor']
      },
    ],
  }
}