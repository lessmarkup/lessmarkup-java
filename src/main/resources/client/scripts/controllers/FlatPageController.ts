/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import $ = require('jquery');
import _ = require('lodash');

class FlatPageVisibleElement {
    position: number;
    id: string;
    height: number;
}

class FlatPageController {

    private static scrollSpyId: string = "";
    private static scrollSetManually: boolean = false;

    private scope: FlatPageControllerScope;

    private static getHeaderHeight(): number {
        return $("#header").height();
    }

    private scrollToPage(anchor: string, exact: boolean) {

        var position = $("#" + anchor).offset().top;
        var headerHeight = FlatPageController.getHeaderHeight();

        if (!exact) {
            position -= headerHeight;
            position -= 10;
        }

        FlatPageController.setScrollSpyId(anchor);

        FlatPageController.scrollSetManually = true;

        $("html,body").stop().animate({ scrollTop: position }, 1000, "swing", () => {
            FlatPageController.scrollSetManually = false;
        });
    }

    private initializePages() {
        this.scope.flat = this.scope.configuration.flat;
        this.scope.tree = this.scope.configuration.tree;
        this.scope.position = this.scope.configuration.position;

        for (var i = 0; i < this.scope.flat.length; i++) {
            var page = this.scope.flat[i];
            var pageScope: any = this.scope.$new();
            pageScope.configuration = page.configuration;
        }
    }

    constructor(scope: FlatPageControllerScope) {
        var pageToScope = {};

        if (scope.configuration.scripts.length > 0) {
            require(scope.configuration.scripts, () => this.initializePages);
        } else {
            this.initializePages();
        }
    }

    private static onScrollChanged() {
        if (FlatPageController.scrollSetManually) {
            return;
        }

        var headerHeight = FlatPageController.getHeaderHeight();

        var windowHeight = $(window).height();

        var scrollPosition = $(window).scrollTop();

        var selectedId:string = "";

        var visibleElements: FlatPageVisibleElement[] = [];

        $('.spyelement').each((i, el) => {
            var position = $(el).offset().top - headerHeight - 10 - scrollPosition;

            if (visibleElements.length > 0) {
                var previous = visibleElements[visibleElements.length - 1];
                previous.height = position - previous.position;
            }

            visibleElements.push({
                position: position,
                id: $(el).attr("id"),
                height: windowHeight - headerHeight - 10 - position
            });
        });

        if (visibleElements.length == 0) {
            return;
        }

        if (scrollPosition == $("html")[0].scrollHeight - $(window).height()) {
            selectedId = visibleElements[visibleElements.length - 1].id;
        } else {
            var maxVisibleHeight = 0;
            var maxVisibleElement: FlatPageVisibleElement = null;
            var topVisibleElement: FlatPageVisibleElement = null;

            _.forEach(visibleElements, (element: FlatPageVisibleElement) => {

                if (element.position >= windowHeight) {
                    return;
                }

                var visibleHeight = element.height;
                if (element.position < 0) {
                    visibleHeight += element.position;
                }
                if (element.position + element.height > windowHeight) {
                    visibleHeight -= element.position + element.height - windowHeight;
                }

                if (visibleHeight > maxVisibleHeight) {
                    maxVisibleElement = element;
                    maxVisibleHeight = visibleHeight;
                }

                if (topVisibleElement == null && visibleHeight > 20) {
                    topVisibleElement = element;
                }
            });

            if (maxVisibleHeight >= windowHeight / 2) {
                selectedId = maxVisibleElement.id;
            } else if (topVisibleElement != null) {
                selectedId = topVisibleElement.id;
            }
        }

        if (selectedId == FlatPageController.scrollSpyId) {
            return;
        }

        //console.log("checkScrollChanged");

        FlatPageController.setScrollSpyId(selectedId);
    }

    public static setScrollSpyId(id: string) {
        FlatPageController.scrollSpyId = id;

        var selectedRef: JQuery = null;

        $('.spyref').each((i, el) => {
            var anchor: string = $(el).data('anchor');
            if (!anchor || anchor.length == 0) {
                return;
            }
            if (anchor == FlatPageController.scrollSpyId) {
                selectedRef = $(el);
            }
            $(el).removeClass('active');
        });

        if (selectedRef == null) {
            return;
        }

        selectedRef.addClass('active');

        selectedRef.parents('.spyref').each((i, el) => {
            $(el).addClass('active');
        });
    }

    public static enableScrollSpy() {
        $(window).on('scroll', FlatPageController.onScrollChanged);
        $(window).on('resize', FlatPageController.onScrollChanged);
        FlatPageController.scrollSpyId = "";
        FlatPageController.onScrollChanged();
    }

    public static disableScrollSpy() {
        $(window).off('scroll', FlatPageController.onScrollChanged);
        $(window).off('resize', FlatPageController.onScrollChanged);
        FlatPageController.scrollSpyId = "";
    }
}

import module = require('./module');
module.controller("flatPage", ['$scope', FlatPageController]);

export = FlatPageController;