package com.eclinic;

public enum UserType {
    DOCTOR,
    PATIENT;

    public static UserType from(String rawValue) {
        String normalized = rawValue == null ? "" : rawValue.trim().toUpperCase();
        if ("DOCTOR".equals(normalized)) {
            return DOCTOR;
        }
        if ("PATIENT".equals(normalized)) {
            return PATIENT;
        }
        throw new IllegalArgumentException("Unsupported user type: " + rawValue);
    }
}
