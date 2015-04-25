///<amd-dependency path="../types/Autolinker.d.ts" />

class FriendlyFormattingService {

    smilesBase: string;
    smiles: { [code: string]: string } = {};
    smilesString = "";
    smilesExpression: RegExp;
    autoLinker: Autolinker;

    constructor(serverConfiguration: ServerConfiguration) {
        this.smilesBase = serverConfiguration.smilesBase;
        this.autoLinker = new Autolinker();
        this.autoLinker.newWindow = false;
        this.autoLinker.truncate = 30;

        for (var i = 0; i < serverConfiguration.smiles.length; i++) {
            var smile = serverConfiguration.smiles[i];
            this.smiles[smile.code] = smile.id;
            if (this.smilesString.length > 0) {
                this.smilesString += "|";
            }
            for (var j = 0; j < smile.code.length; j++) {
                switch (smile.code[j]) {
                    case '(':
                    case ')':
                    case '[':
                    case ']':
                    case '-':
                    case '?':
                    case '|':
                        this.smilesString += '\\';
                        break;
                }
                this.smilesString += smile.code[j];
            }
        }

        if (this.smilesString.length > 0) {
            this.smilesExpression = new RegExp(this.smilesString, "g");
        } else {
            this.smilesExpression = null;
        }

    }

    public getSmilesExpression(): RegExp {
        return this.smilesExpression;
    }

    private getSmileUrl(code: string) {
        if (!code.length || !this.smilesBase) {
            return "";
        }
        return "<img alt=\"" + code + "\" src=\"" + this.smilesBase + this.smiles[code] + "\" title=\"" + code + "\" />";
    }

    public smilesToImg(text: string): string {
        if (this.smilesExpression != null) {
            text = text.replace(/([^<>]*)(<[^<>]*>)/gi, function (match, left, tag) {
                if (!left || left.length == 0) {
                    return match;
                }
                left = left.replace(this.smilesExpression, this.getSmileUrl);
                return tag ? left + tag : left;
            });
        }
        return text;
    }

    public getFriendlyHtml(text: string): string {

        if (text == null || text.length == 0) {
            return text;
        }

        text = this.smilesToImg(text);

        return this.autoLinker.link(text);
    }
}

import servicesModule = require('./module');
servicesModule.service('friendlyFormatting', [
    'serverConfiguration',
    FriendlyFormattingService]);

export = FriendlyFormattingService;