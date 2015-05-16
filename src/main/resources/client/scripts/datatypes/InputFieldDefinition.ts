interface InputFieldDefinition {
    type: string;
    text: string;
    selectValues: SelectValueDefinition[];
    readOnly: boolean;
    id: string;
    required: boolean;
    width?: number;
    maxLength?: number;
    minLength?: number;
    readOnlyCondition: string;
    visibleCondition: string;
    property: string;
    helpText: string;
    defaultValue: any;
    visibleFunction: Function;
    readOnlyFunction: Function;
    dynamicSource: DynamicInputFieldDefinition;
    isOpen: boolean;
    inlineWithPrevious: boolean;
    children: InputFieldDefinition[];
    isGroup: boolean;
    reference: string;
    position: string;
}
