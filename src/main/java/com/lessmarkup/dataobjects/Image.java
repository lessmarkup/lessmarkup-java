package com.lessmarkup.dataobjects;

import com.lessmarkup.interfaces.data.AbstractDataObject;
import com.lessmarkup.interfaces.recordmodel.RequiredField;

import java.time.OffsetDateTime;
import java.util.OptionalLong;

public class Image extends AbstractDataObject {
    private String contentType;
    private byte[] data;
    private String thumbnailContentType;
    private byte[] thumbnail;
    private OffsetDateTime created;
    private OffsetDateTime updated;
    private OptionalLong userId;
    private String fileName;
    private int height;
    private int width;

    @RequiredField
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @RequiredField
    public byte[] getData() {
        return data;
    }
    public void setData(byte[] data) {
        this.data = data;
    }

    public String getThumbnailContentType() {
        return thumbnailContentType;
    }
    public void setThumbnailContentType(String thumbnailContentType) { this.thumbnailContentType = thumbnailContentType; }

    public byte[] getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    @RequiredField
    public OffsetDateTime getCreated() {
        return created;
    }
    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }
    public void setUpdated(OffsetDateTime updated) {
        this.updated = updated;
    }

    public OptionalLong getUserId() {
        return userId;
    }
    public void setUserId(OptionalLong userId) {
        this.userId = userId;
    }

    @RequiredField
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }
    public void setWidth(int width) {
        this.width = width;
    }
}
