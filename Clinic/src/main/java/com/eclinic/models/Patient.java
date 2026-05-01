package com.eclinic.models;

public class Patient {
    private long id;
    private Long userId;
    private String fullName;
    private String dob;
    private String gender;
    private String phone;
    private String address;
    private String insuranceCode;
    private String createdAt;

    public Patient(long id, Long userId, String fullName, String dob, String gender, String phone, String address, String insuranceCode, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.insuranceCode = insuranceCode;
        this.createdAt = createdAt;
    }

    public Patient() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getInsuranceCode() { return insuranceCode; }
    public void setInsuranceCode(String insuranceCode) { this.insuranceCode = insuranceCode; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", userId=" + userId +
                ", fullName='" + fullName + '\'' +
                ", dob='" + dob + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", insuranceCode='" + insuranceCode + '\'' +
                '}';
    }
}
