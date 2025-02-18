package org.svuonline.lms.ui.data;


public class WelcomeData {
    private final int image;
    private final String title;

    public WelcomeData(int image, String title) {
        this.image = image;
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }
}
