package com.lessmarkup.userinterface.model.structure;

public class MenuItemModel {
    private final String url;
    private final String title;
    private final String imageUrl;
    private final boolean selected;
    private final int level;
    
    public MenuItemModel(String url, String title, String imageUrl, boolean selected, int level) {
        this.url = url;
        this.title = title;
        this.imageUrl = imageUrl;
        this.selected = selected;
        this.level = level;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }
    
    
}
