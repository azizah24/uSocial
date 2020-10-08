package com.azizah.msocial;



public class Pengirim {

    private Datanotif data;
    private String to;
    private Notifikasi notification;

    public Pengirim() {
    }

    public Pengirim(Datanotif data, String to, Notifikasi notification) {
        this.data = data;
        this.to = to;
        this.notification = notification;
    }

    public Datanotif getData() {
        return data;
    }

    public void setData(Datanotif data) {
        this.data = data;
    }

    public Notifikasi getNotification() {
        return notification;
    }

    public void setNotification(Notifikasi notification) {
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}