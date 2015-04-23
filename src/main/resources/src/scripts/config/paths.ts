require.config({
    baseUrl: '../',
    paths: {
        'jquery': 'lib/jquery/jquery',
        'lodash': 'lib/lodash/lodash',
        'angular': 'lib/angular/angular',
        'angular.animate': 'lib/angular-animate/angular-animate',
        'angular.material': 'lib/angular-material/angular-material',
        'angular.route': 'lib/angular-route/angular-route',
        'angular.spinner': 'lib/angular-spinner/angular-spinner',
        'angular.translate': 'lib/angular-translate/angular-translate',
        'autolinker': 'lib/Autolinker.js/dist/Autolinker.js',
        'bootstrap': 'lib/bootstap/dist/js/bootstrap',
        'ckeditor': 'lib/ckeditor/ckeditor',
        'codemirror': 'lib/codemirror/lib/codemirror',
        'tinymce': 'lib/tinymce/tinymce',
        'domReady': 'lib/requirejs-domready/domReady'
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
});