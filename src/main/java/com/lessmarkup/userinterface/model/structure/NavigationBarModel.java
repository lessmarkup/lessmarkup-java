package com.lessmarkup.userinterface.model.structure;

import java.util.ArrayList;
import java.util.List;

public class NavigationBarModel extends MenuItemModel {
    private final List<NavigationBarModel> children = new ArrayList<>();

    public NavigationBarModel(String url, String title, String imageUrl, boolean selected, int level) {
        super(url, title, imageUrl, selected, level);
    }

    public List<NavigationBarModel> getChildren() { return children; }
}
