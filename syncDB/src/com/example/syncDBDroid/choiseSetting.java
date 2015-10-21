package com.example.syncDBDroid;

/**
 * Created with IntelliJ IDEA.
 * User: n.bilan
 * Date: 26.12.13
 * Time: 11:52
 * To change this template use File | Settings | File Templates.
 */
public class choiseSetting {
   private int serverPort;// = 1023; // здесь обязательно нужно указать порт к которому привязывается сервер.
    private String address;// = "192.168.1.2"; // это IP-адрес компьютера, где исполняется наша серверная программа.
    private String db_name;
    private int vDB;
    private boolean isSD;

    public int getServerPort() {
        return serverPort;
    }

    public int getvDB() {
        return vDB;
    }

    public void setvDB(int vDB) {
        this.vDB = vDB;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
    }

    public boolean isSD() {
        return isSD;
    }

    public void setSD(boolean SD) {
        isSD = SD;
    }

    choiseSetting(){
        serverPort= 1023; // здесь обязательно нужно указать порт к которому привязывается сервер.
        address = "192.168.1.2";
        db_name = "anyDB";
        vDB=1;
        isSD =false;
    }

}
