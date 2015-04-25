interface Language {
    selected: boolean;
    id: string;
    shortName: string;
    name: string;
    isDefault: boolean;
    iconUrl: string;
    translations: {[key: string]: string};
}