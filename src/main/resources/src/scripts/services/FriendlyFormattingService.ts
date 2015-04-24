import Autolinker = require('Autolinker');

class FriendlyFormattingService {

    smilesBase: string;
    smiles: { [code: string]: string } = {};
    smilesStr = "";
    smilesExpr: RegExp;
    autoLinker: Autolinker;

    constructor(serverConfiguration: ServerConfiguration) {
        this.smilesBase = serverConfiguration.smilesBase;
        this.autoLinker = new Autolinker({ newWindow: false, truncate: 30});

        for (var i = 0; i < serverConfiguration.smiles.length; i++) {
            var smile = serverConfiguration.smiles[i];
            this.smiles[smile.code] = smile.id;
            if (this.smilesStr.length > 0) {
                this.smilesStr += "|";
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
                        this.smilesStr += '\\';
                        break;
                }
                this.smilesStr += smile.code[j];
            }
        }

        if (this.smilesStr.length > 0) {
            this.smilesExpr = new RegExp(this.smilesStr, "g");
        } else {
            this.smilesExpr = null;
        }

    }

    private getSmileUrl(code: string) {
        if (!code.length || !this.smilesBase) {
            return "";
        }
        return "<img alt=\"" + code + "\" src=\"" + this.smilesBase + this.smiles[code] + "\" title=\"" + code + "\" />";
    }

    private smilesToImg(text: string) {
        if (this.smilesExpr != null) {
            text = text.replace(/([^<>]*)(<[^<>]*>)/gi, function (match, left, tag) {
                if (!left || left.length == 0) {
                    return match;
                }
                left = left.replace(this.smilesExpr, this.getSmileUrl);
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

        return Autolinker.link(text);
    }
}

import servicesModule = require('./module');
servicesModule.service('friendlyFormatting', [
    'serverConfiguration',
    FriendlyFormattingService]);
