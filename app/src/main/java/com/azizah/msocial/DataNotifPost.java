package com.azizah.msocial;

public class DataNotifPost {
    private String pId, title, deskt, notificationType,notificationTopic;

    public DataNotifPost() {
    }

    public DataNotifPost(String pId, String title, String deskt, String notificationType, String notificationTopic) {
        this.pId = pId;
        this.title = title;
        this.deskt = deskt;
        this.notificationType = notificationType;
        this.notificationTopic = notificationTopic;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeskt() {
        return deskt;
    }

    public void setDeskt(String deskt) {
        this.deskt = deskt;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationTopic() {
        return notificationTopic;
    }

    public void setNotificationTopic(String notificationTopic) {
        this.notificationTopic = notificationTopic;
    }
}
