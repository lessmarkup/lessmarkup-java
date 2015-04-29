interface IApplication extends ng.IModule {
    initialize: (initialData: InitialData, serverConfiguration: ServerConfiguration, languages: Language[]) => void;
}
