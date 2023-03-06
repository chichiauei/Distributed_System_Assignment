package org.client2;

public class RequestCounter {

    private int count = 0;

    synchronized public  void inc() {
        count++;
    }

    public int getVal() {
        return this.count;
    }
}
