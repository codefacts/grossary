package com.imslbd.grossary.model;

/**
 * Created by shahadat on 1/30/16.
 */
public enum UserType {
    ADMIN(1), CALL_CENTER_SUPERVISOR(2);
    private final long id;

    UserType(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }
}
