package com.fezzee.messaging;

public class FavoriteItem {
private int imageId;
private String jid;  //WO Device!
private String name; //friendly name
private int presence;
private String status;

public FavoriteItem(String jid, int imageId, String name, String status, int availId) {
    this.imageId = imageId;
    this.name = name;
    this.status = status;
    this.presence = availId;
    this.jid = jid;
}
public int getImageId() {
    return imageId;
}
public void setImageId(int imageId) {
    this.imageId = imageId;
}
public String getJID() {
    return jid;
}
public void setJID(String jid) {
    this.jid = jid;
}
public int getPresence() {
    return presence;
}
public void setPresence(int presence) {
    this.presence = presence;
}
public String getName() {
    return name;
}
public void setName(String name) {
    this.name = name;
}
public String getStatus() {
    return status;
}
public void setStatus(String status) {
    this.status = status;
}
@Override
public String toString() {
    return name + " : " + status;
}
}
