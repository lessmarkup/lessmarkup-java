interface InputFieldDefinition {
    type: string;
    text: string;
    selectValues: SelectValueDefinition[];
    readOnly: boolean;
    id: string;
    required: boolean;
    width?: number;
    readOnlyCondition: string;
    visibleCondition: string;
    property: string;
    helpText: string;
    defaultValue: any;
    visibleFunction: (object: any) => boolean;
    readOnlyFunction: (object: any) => boolean;
    dynamicSource: DynamicInputFieldDefinition;
    isOpen: boolean;
}
