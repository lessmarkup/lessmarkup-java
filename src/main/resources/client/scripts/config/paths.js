var require = {
    paths: {
        'jquery': 'lib/jquery/jquery',
        'lodash': 'lib/lodash/lodash',
        'angular': 'lib/angular/angular',
        'angular.animate': 'lib/angular-animate/angular-animate',
        'angular.aria': 'lib/angular-aria/angular-aria',
        'angular.material': 'lib/angular-material/angular-material',
        'angular.messages': 'lib/angular-messages/angular-messages',
        'angular.spinner': 'lib/angular-spinner/angular-spinner',
        'angular.translate': 'lib/angular-translate/angular-translate',
        'angular.touch': 'lib/angular-touch/angular-touch',
        'autolinker': 'lib/Autolinker.js/dist/Autolinker',
        'bootstrap': 'lib/bootstrap/dist/js/bootstrap',
        'ckeditor': 'lib/ckeditor/ckeditor',
        'tinymce': 'lib/tinymce/tinymce',
        'domready': 'lib/requirejs-domready/domReady',
        'Recaptcha': 'http://www.google.com/recaptcha/api/js/recaptcha_ajax',
        'requirejs.text': 'lib/requirejs-text/text',
        'sha512': 'lib/cryptojslib/rollups/sha512',
        'codemirror': 'lib/codemirror/lib/codemirror',
        'codemirror.htmlmixed': 'lib/codemirror/mode/htmlmixed/htmlmixed',
        'codemirror.htmlembedded': 'lib/codemirror/mode/htmlembedded/htmlembedded',
        'codemirror.htmlhint': 'lib/codemirror/addon/hint/html-hint',
        'codemirror.xml': 'lib/codemirror/mode/xml/xml',
        'codemirror.javascript': 'lib/codemirror/mode/javascript/javascript',
        'codemirror.css': 'lib/codemirror/mode/css/css',
        'codemirror.multiplex': 'lib/codemirror/addon/mode/multiplex',
        'codemirror.xmlhint': 'lib/codemirror/addon/hint/xml-hint'
    },
    map: {
        '*': {
            '../lib/codemirror': 'codemirror',
            '../../lib/codemirror': 'codemirror',
            '../xml/xml': 'codemirror.xml',
            '../javascript/javascript': 'codemirror.javascript',
            '../css/css': 'codemirror.css',
            '../htmlmixed/htmlmixed': 'codemirror.htmlmixed',
            '../../addon/mode/multiplex': 'codemirror.multiplex',
            'xml-hint': 'codemirror.xmlhint'
        }
    },
    shim: {
        'angular':{
            exports: 'angular',
            deps: ['jquery', 'requirejs.text']
        },
        'jquery': {
            exports: '$'
        },
        'lodash': {
            exports: '_'
        },
        'backbone': {
            deps: ['jquery']
        },
        'angular.animate': {
            deps: ['angular']
        },
        'angular.messages': {
            deps: ['angular']
        },
        'angular.material': {
            deps: ['angular', 'angular.animate', 'angular.aria', 'angular.messages']
        },
        'angular.spinner': {
            deps: ['angular']
        },
        'angular.translate': {
            deps: ['angular']
        },
        'angular.aria': {
            deps: ['angular']
        },
        'angular.touch': {
            deps: ['angular']
        },
        'bootstrap': {
            deps: ['jquery']
        },
        'ckeditor': {
            deps: ['jquery'],
            exports: 'CKEDITOR'
        },
        'codemirror': {
            deps: ['jquery']
        },
        'tinymce': {
            deps: ['jquery']
        }
    }
};
