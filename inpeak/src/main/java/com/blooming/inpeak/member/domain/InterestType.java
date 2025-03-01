package com.blooming.inpeak.member.domain;

public enum InterestType {
    REACT,
    SPRING,
    DATABASE;

    public String toFormattedString() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
