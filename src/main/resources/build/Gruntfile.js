module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-contrib-copy');
    grunt.loadNpmTasks('grunt-contrib-less');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-sass');
    grunt.loadNpmTasks('grunt-contrib-cssmin');
    grunt.loadNpmTasks('grunt-ts');
    grunt.loadNpmTasks('grunt-tsd');

    grunt.initConfig(grunt.file.readJSON('config/grunt.config.json'));
    
    grunt.registerTask('default', []);
    grunt.registerTask('build', ['tsd', 'ts', 'copy:ts', 'copy:bower', 'sass', 'cssmin']);
};
