package com.android.emu.cenv.socket;

public class SocketInfo {

//    # https://github.com/torvalds/linux/blob/master/include/linux/socket.h
    public static final long AF_UNIX = 1;

//    # http://students.mimuw.edu.pl/SO/Linux/Kod/include/linux/socket.h.html
    public static final long SOCK_STREAM = 1;

    public long domain;
    public long type;
    public long protocol;

    public String addr;
}
