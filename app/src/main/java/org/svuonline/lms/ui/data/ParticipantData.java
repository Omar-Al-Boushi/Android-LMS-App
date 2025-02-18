package org.svuonline.lms.ui.data;

import androidx.annotation.StringRes;

public class ParticipantData {
    private @StringRes int nameResourceId;
    private @StringRes int roleResourceId;
    private @StringRes int descriptionResourceId;
    private int imageResource;

    public ParticipantData(@StringRes int nameResourceId, @StringRes int roleResourceId, @StringRes int descriptionResourceId, int imageResource) {
        this.nameResourceId = nameResourceId;
        this.roleResourceId = roleResourceId;
        this.descriptionResourceId = descriptionResourceId;
        this.imageResource = imageResource;
    }

    public int getNameResourceId() {
        return nameResourceId;
    }

    public void setNameResourceId(int nameResourceId) {
        this.nameResourceId = nameResourceId;
    }

    public int getRoleResourceId() {
        return roleResourceId;
    }

    public void setRoleResourceId(int roleResourceId) {
        this.roleResourceId = roleResourceId;
    }

    public int getDescriptionResourceId() {
        return descriptionResourceId;
    }

    public void setDescriptionResourceId(int descriptionResourceId) {
        this.descriptionResourceId = descriptionResourceId;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
