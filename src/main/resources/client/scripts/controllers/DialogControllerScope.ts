import InputFormControllerScope = require('./InputFormControllerScope');

interface DialogControllerScope extends InputFormControllerScope {
    submitSuccess: string;
    applyCaption: string;
    changesApplied: boolean;
    configuration: DialogControllerConfiguration;
    hasChanges: boolean;
    openForm(form: ng.IFormController);
}

export = DialogControllerScope;