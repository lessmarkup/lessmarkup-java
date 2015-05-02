class UserState {

    constructor() {
        this.notVerified = true;
        this.progress = false;
        this.email = "";
        this.password = "";
        this.remember = false;
        this.loggedIn = false;
        this.name = null;
        this.error = null;
        this.showConfiguration = false;
    }

    email: string;
    password: string;
    remember: boolean;
    loggedIn: boolean;
    notVerified: boolean;
    name: string;
    error: string;
    progress: boolean;
    showConfiguration: boolean;
}

export = UserState;