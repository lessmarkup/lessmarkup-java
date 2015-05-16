package com.lessmarkup.userinterface.model.structure;

import com.lessmarkup.framework.system.RequestContextHolder;
import com.lessmarkup.interfaces.cache.DataCache;
import com.lessmarkup.interfaces.system.RequestContext;
import com.lessmarkup.interfaces.system.ResourceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Scope("prototype")
public class ResourceModel {
    
    private final DataCache dataCache;
    private String contentType;
    private String path;
    private String extension;

    @Autowired
    public ResourceModel(DataCache dataCache) {
        this.dataCache = dataCache;
    }
    
    public boolean initialize(String path) {
        if (path == null || path.length() == 0) {
            return false;
        }
        
        ResourceCache resourceCache = dataCache.get(ResourceCache.class);
        
        if (!resourceCache.resourceExists(path)) {
            return false;
        }
        
        int lastDotPoint = path.lastIndexOf('.');
        
        if (lastDotPoint <= 0) {
            return false;
        }
        
        this.extension = path.substring(lastDotPoint+1).toLowerCase();
        
        switch (extension) {
            case "html":
                this.contentType = "text/html";
                break;
            case "txt":
                this.contentType = "text/plain";
                break;
            case "js":
                this.contentType = "text/javascript";
                break;
            case "css":
                this.contentType = "text/css";
                break;
            case "jpeg":
            case "jpg":
                this.contentType = "image/jpeg";
                break;
            case "gif":
                this.contentType = "image/gif";
                break;
            case "png":
                this.contentType = "image/png";
                break;
            case "eot":
                this.contentType = "application/vnd.ms-fontobject";
                break;
            case "otf":
                this.contentType = "application/x-font-opentype";
                break;
            case "svg":
                this.contentType = "image/svg+xml";
                break;
            case "ttf":
                this.contentType = "application/font-sfnt";
                break;
            case "woff":
            case "woff2":
                this.contentType = "application/font-woff";
                break;
            case "map":
                this.contentType = "text/map";
                break;
            case "ts":
                this.contentType = "text/typescript";
                break;
            default:
                return false;
        }
        
        this.path = path;
        
        return true;
    }
    
    public void handleRequest() throws IOException {
        RequestContext requestContext = RequestContextHolder.getContext();
        requestContext.addHeader("Cache-Control", "public, max-age=3600");
        requestContext.addHeader("Content-Type", this.contentType);
        ResourceCache resourceCache = this.dataCache.get(ResourceCache.class);
        
        byte[] resourceBytes;
        
        switch (this.extension) {
            case "html":
                resourceBytes = resourceCache.parseText(this.path).getBytes(StandardCharsets.UTF_8);
                break;
            default:
                resourceBytes = resourceCache.readBytes(this.path);
                break;
        }
        
        requestContext.getOutputStream().write(resourceBytes, 0, resourceBytes.length);
    }
}
