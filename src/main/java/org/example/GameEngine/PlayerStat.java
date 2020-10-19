package org.example.GameEngine;

public enum PlayerStat {
    ANOMALY("ANOMALY"),
    NORMAL("NORMAL");

    private String value;
    PlayerStat(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
