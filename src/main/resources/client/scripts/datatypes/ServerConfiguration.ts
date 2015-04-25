class ServerConfiguration {
    hasLogin: boolean;
    hasSearch: boolean;
    configurationPath: string;
    rootPath: string;
    rootTitle: string;
    profilePath: string;
    forgotPasswordPath: string;
    collections: CollectionDefinition[];
    recaptchaPublicKey: string;
    maximumFileSize: number;
    smiles: Smile[];
    smilesBase: string;
    useGoogleAnalytics: boolean;
    loginModelId: string;
    pageSize: number;
    navigationTree: MenuItem[];
    topMenu: TopMenuItem[];
}