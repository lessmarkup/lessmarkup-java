package com.lessmarkup.interfaces.system;

import com.lessmarkup.interfaces.cache.CacheHandler;

public interface ResourceCache extends CacheHandler {
    boolean resourceExists(String path);
    byte[] readBytes(String path);
    String readText(String path);
    String parseText(String path);
}
