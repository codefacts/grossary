package com.imslbd.grossary.model;

/**
 * Created by shahadat on 12/30/15.
 */
public class Contact {
    private String name;
    private String phone;
    private String ageGroup;
    private byte[] signature;
    private String grocery;
    private String location;
    private String posNo;
    private String address;
    private MyDate date;

    public static class MyDate {
        private String $date;
    }
}
