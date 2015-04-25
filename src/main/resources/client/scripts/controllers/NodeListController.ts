interface NodeListNode {
    data: TreeNode;
    level: number;
    parent: NodeListNode;
    index: number;
    parentIndex: number;
}

interface NodeListControllerConfiguration {
    root: any;
    nodeSettingsModelId: string;
}

interface NodeListControllerScope extends ng.IScope {
    nodes: NodeListNode[];
    updateProgress: boolean;
    updateError: string;
    rootNode: TreeNode;
    configuration: NodeListControllerConfiguration;
    getLevelStyle: (node: NodeListNode) => any;
    nodeAccessPage: (node: NodeListNode) => string;
    nodeEnabled: (node: NodeListNode) => boolean;
    upDisabled: (node: NodeListNode) => boolean;
    downDisabled: (node: NodeListNode) => boolean;
    leftDisabled: (node: NodeListNode) => boolean;
    rightDisabled: (node: NodeListNode) => boolean;
    moveUp: (node: NodeListNode) => void;
    moveDown: (node: NodeListNode) => void;
    moveLeft: (node: NodeListNode) => void;
    moveRight: (node: NodeListNode) => void;
    createNode: (parent: NodeListNode) => void;
    canBeDeleted: (node: NodeListNode) => boolean;
    deleteNode: (node: NodeListNode) => void;
    hasSettings: (node: NodeListNode) => boolean;
    changeSettings: (node: NodeListNode) => void;
    changeProperties: (node: NodeListNode) => void;
    path: string;
}

import CommandProcessorService = require('../services/CommandProcessorService');
import InputFormService = require('../services/InputFormService');

class NodeListController {

    private scope: NodeListControllerScope;
    private commandProcessor: CommandProcessorService;
    private inputForm: InputFormService;
    private qService: ng.IQService;

    constructor(scope: NodeListControllerScope, commandProcessor: CommandProcessorService, inputForm: InputFormService, qService: ng.IQService) {

        this.scope = scope;
        this.commandProcessor = commandProcessor;
        this.inputForm = inputForm;
        this.qService = qService;

        scope.nodes = [];
        scope.updateProgress = false;
        scope.updateError = "";
        scope.rootNode = scope.configuration.root;


        scope.getLevelStyle = (node: NodeListNode) : any => {
            return {
                "padding-left": (node.level * 35).toString() + "px"
            };
        };

        scope.nodeAccessPage = (node: NodeListNode): string => {
            return this.scope.path + "/" + node.data.nodeId.toString() + "/access";
        };

        scope.nodeEnabled = (node: NodeListNode): boolean => {
            for (; node != null; node = node.parent) {
                if (!node.data.enabled) {
                    return false;
                }
            }
            return true;
        };

        scope.upDisabled = (node: NodeListNode): boolean => {
            return node.index == 0;
        };

        scope.downDisabled = (node: NodeListNode): boolean => {
            if (node.parent == null) {
                return true;
            }
            return node.parentIndex == node.parent.data.children.length - 1 && node.parent.parent == null;
        };

        scope.leftDisabled = (node: NodeListNode) => {
            return node.parent == null || node.index == 0;
        };

        scope.rightDisabled = (node: NodeListNode) => {
            return node.parentIndex == 0;
        };


        scope.moveUp = (node: NodeListNode) => { this.moveUp(node); };

        scope.moveDown = (node: NodeListNode) => { this.moveDown(node); };

        scope.moveLeft = (node: NodeListNode) => {
            if (node.parent == null) {
                return;
            }

            this.changeParent(node, node.parent.parent, node.parent.parentIndex);
        };

        scope.moveRight = (node: NodeListNode) => {
            if (node.parentIndex == 0) {
                return;
            }

            this.changeParent(node, scope.nodes[node.index - 1], 0);
        };

        scope.createNode = (parentNode: NodeListNode) => { this.createNode(parentNode); };

        scope.canBeDeleted = (node: NodeListNode): boolean => {
            return node.data.children.length == 0;
        };

        scope.deleteNode = (node: NodeListNode) => { this.deleteNode(node); };

        scope.hasSettings = (node: NodeListNode): boolean => { return node.data.customizable; };

        scope.changeSettings = (node: NodeListNode) => { this.changeSettings(node); };

        scope.changeProperties = (node: NodeListNode) => { this.changeProperties(node); };

        this.refreshFlatList();
    }

    private addNodeToFlatList(node: any, level: number, parent: NodeListNode, parentIndex: number) {
        var target: NodeListNode = {
            data: node,
            level: level,
            parent: parent,
            index: this.scope.nodes.length,
            parentIndex: parentIndex
        };

        this.scope.nodes.push(target);

        for (var i = 0; i < node.children.length; i++) {
            this.addNodeToFlatList(node.children[i], level + 1, target, i);
        }
    }

    private refreshFlatList() {
        this.scope.nodes = [];

        if (this.scope.rootNode != null) {
            this.addNodeToFlatList(this.scope.rootNode, 0, null, 0);
        }
    }

    private changeParent(node: NodeListNode, parent: NodeListNode, order: number) {
        if (parent != null && order > parent.data.children.length) {
            order = parent.data.children.length;
        }

        this.commandProcessor.sendCommand("UpdateParent", {
            nodeId: node.data.nodeId,
            parentId: parent != null ? parent.data.nodeId : null,
            order: order
        }).then((data: any) => {
                this.scope.rootNode = data.root;
                this.refreshFlatList();
            }, (message: string) => {
                this.inputForm.message(message);
            });
    }

    private moveUp(node: NodeListNode): void {
        if (node.index <= 0) {
            return;
        }

        if (node.index == 1) {
            this.changeParent(node, null, 0);
            return;
        }

        if (node.parent == null) {
            return;
        }

        if (node.parentIndex > 0) {
            this.changeParent(node, node.parent, node.parentIndex - 1);
            return;
        }

        if (node.parent.parent == null) {
            return;
        }

        this.changeParent(node, node.parent.parent, node.parent.parentIndex);
    }

    private moveDown(node: NodeListNode): void {
        if (node.parent == null) {
            return;
        }

        if (node.parentIndex + 1 < node.parent.data.children.length) {
            this.changeParent(node, node.parent, node.parentIndex + 1);
            return;
        }

        if (node.parent.parent == null) {
            return;
        }

        this.changeParent(node, node.parent.parent, node.parent.parentIndex + 1);
    }

    private createNode(parentNode: NodeListNode) {
        if (parentNode == null && this.scope.nodes.length > 0) {
            return;
        }

        this.inputForm.editObject<TreeNode>(null, this.scope.configuration.nodeSettingsModelId, function (node: TreeNode): ng.IPromise<void> {
            if (parentNode == null) {
                // create root node
                node.parentId = null;
                node.order = 0;
            } else {
                node.parentId = parentNode.data.nodeId;
                node.order = parentNode.data.children.length;
            }

            var deferred: ng.IDeferred<void> = this.qService.defer();

            this.commandProcessor.sendCommand("CreateNode", {
                node: node
            })
                .then((data: TreeNode) => {
                    if (parentNode == null) {
                        this.scope.rootNode = data;
                    } else {
                        parentNode.data.children.push(data);
                    }
                    this.refreshFlatList();
                    deferred.resolve();
                }, (message: string) => {
                    deferred.reject(message);
                });

            return deferred.promise;
        });
    }

    private deleteNode(node: NodeListNode) {
        if (node.data.children.length != 0) {
            return;
        }

        this.inputForm.question("Do you want to delete node?", "Delete Nodes", () => {

            var deferred = this.qService.defer<void>();

            this.commandProcessor.sendCommand("DeleteNode", { id: node.data.nodeId })
                .then(() => {
                    if (node.parent == null) {
                        this.scope.rootNode = null;
                    } else {
                        node.parent.data.children.splice(node.parentIndex, 1);
                    }
                    this.refreshFlatList();
                    deferred.resolve();
                },
                (message: string) => {
                    deferred.reject(message);
                });

            return deferred.promise;
        });
    }

    private changeSettings(node: NodeListNode): void {

        if (!node.data.customizable) {
            return;
        }
        this.inputForm.editObject(node.data.settings, node.data.settingsModelId, (settings) => {

            var deferred = this.qService.defer<void>();

            this.commandProcessor.sendCommand("ChangeSettings", {
                nodeId: node.data.nodeId,
                settings: settings
            }).then((data) => {
                node.data.settings = data;
                this.refreshFlatList();
                deferred.resolve();
            }, (message: string) => {
                deferred.reject(message);
            });

            return deferred.promise;
        });
    }

    private changeProperties(node: NodeListNode): void {

        this.inputForm.editObject<TreeNode>(node.data, this.scope.configuration.nodeSettingsModelId, (updatedNode: TreeNode) => {
            updatedNode.children = null;

            var deferred = this.qService.defer<void>();

            this.commandProcessor.sendCommand<TreeNode>("UpdateNode", {
                node: updatedNode
            }).then((returnedNode: TreeNode) => {
                for (var property in returnedNode) {
                    if (property != "children") {
                        node.data[property] = returnedNode[property];
                    }
                }

                this.refreshFlatList();
                deferred.resolve();
            }, (message: string) => {
                deferred.reject(message);
            });

            return deferred.promise;
        });
    }
}

import module = require('./module');
module.controller("nodeList", ['$scope', 'commandProcessor', 'inputForm', '$q', NodeListController]);
