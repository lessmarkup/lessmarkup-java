///<amd-dependency path="../controllers/index" />
///<amd-dependency path="../directives/index" />
///<amd-dependency path="../services/index" />
///<amd-dependency path="../filters/index" />

import ng = require('angular');

var a = ng.module('app', [
    'app.services',
    'app.controllers',
    'app.directives',
    'app.filters'
]);

export = a;

