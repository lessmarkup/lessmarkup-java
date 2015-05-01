/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

interface InputFormControllerScope extends ng.IScope {
    fields: InputFieldDefinition[];
    submitError: string;
    isApplying: boolean;
    submitWithCaptcha: boolean;
    isDisabled(): boolean;
    codeMirrorDefaultOptions: CodeMirror.EditorConfiguration;
    isNewObject: boolean;
    object: any;
    fieldValueSelected: (field: InputFieldDefinition, select: SelectValueDefinition) => boolean;
    getValue: (field: InputFieldDefinition) => any;
    readOnly: (field: InputFieldDefinition) => string;
    hasErrors: (field: InputFieldDefinition) => boolean;
    getErrorText: (field: InputFieldDefinition) => string;
    getHelpText: (field: InputFieldDefinition) => string;
    fieldVisible: (field: InputFieldDefinition) => boolean;
    getTypeahead: (field: InputFieldDefinition, searchText: string) => string[];
    submit: () => void;
    cancel: () => void;
    showDateTimeField: (event, field: InputFieldDefinition) => void;
}

export = InputFormControllerScope;
