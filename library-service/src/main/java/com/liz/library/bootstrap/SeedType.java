package com.liz.library.bootstrap;

public enum SeedType {
    ROLE(0),
    USER(1),
    BOOK(2),
    ALL(999);

    private final int order;

    SeedType(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
