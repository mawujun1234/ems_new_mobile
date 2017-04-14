package com.mawujun.updateapp;

/**
 * Created by LuoWen on 2015/12/14.
 */
public class Version {
    private int local;
    private int remote;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String name;
    private String message;

    public Version(int local, int remote) {
        this.local = local;
        this.remote = remote;
    }

    public int getLocal() {
        return local;
    }

    public int getRemote() {
        return remote;
    }
}