package com.example.SunbrelloSimpleServer;


public class SignalDto {
    private int signal;
    private String msg;

    public SignalDto() {
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
