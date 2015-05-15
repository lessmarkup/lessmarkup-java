module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-sass');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-ts');
    grunt.loadNpmTasks('grunt-tsd');
    grunt.loadNpmTasks('grunt-haml');
    grunt.loadNpmTasks('grunt-contrib-csslint');
    grunt.loadNpmTasks('grunt-protractor-runner');

    grunt.initConfig(grunt.file.readJSON('config/grunt.config.json'));

    grunt.config('haml.default.files', grunt.file.expandMapping(['../views/**/*.haml'], '../views/', {
        rename: function(base, path) {
            return base + path.replace(/\.haml$/, '.html');
        }
    }));
    
    grunt.registerTask('default', []);
    grunt.registerTask('build', ['tsd', 'ts', 'copy:ts', 'copy:bower', 'copy:resources', 'sass', 'csslint', 'cssmin', 'haml']);
};
