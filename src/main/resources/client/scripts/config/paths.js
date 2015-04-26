var require = {
    paths: {
        'jquery': '../lib/jquery/jquery',
        'lodash': '../lib/lodash/lodash',
        'angular': '../lib/angular/angular',
        'angular.animate': '../lib/angular-animate/angular-animate',
        'angular.material': '../lib/angular-material/angular-material',
        'angular.spinner': '../lib/angular-spinner/angular-spinner',
        'angular.translate': '../lib/angular-translate/angular-translate',
        'angular.touch': '../lib/angular-touch/angular-touch',
        'autolinker': '../lib/Autolinker.js/dist/Autolinker.js',
        'bootstrap': '../lib/bootstap/dist/js/bootstrap',
        'ckeditor': '../lib/ckeditor/ckeditor',
        'Codemirror': '../lib/codemirror/lib/codemirror',
        'tinymce': '../lib/tinymce/tinymce',
        'domready': '../lib/requirejs-domready/domReady',
        'Recaptcha': 'http://www.google.com/recaptcha/api/js/recaptcha_ajax'
    },
    shim: {
        'angular':{
            exports: 'angular',
            deps: ['jquery']
        },
        'jquery': {
            exports: '$'
        },
        'lodash': {
            exports: '_'
        },
        'backbone': {
            deps: ['jquery']
        }
    },
    deps: ['./bootstrap']
};