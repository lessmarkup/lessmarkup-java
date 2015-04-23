import ng = require('angular');
import app = require('app');

interface LoginDialogDirectiveScope extends ng.IScope {

}

class LoginDialogDirective {
    constructor(scope: LoginDialogDirectiveScope) {

    }
}

app.directive('loginDialog', ['', () => {
    return <ng.IDirective>{
        template: '/views/login.html',
        restrict: 'E',
        replace: true,
        scope: true,
        controller: ['$scope', LoginDialogDirective]
    };
}]);