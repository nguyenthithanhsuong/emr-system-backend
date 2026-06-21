package com.eclinic.models;

public class PrescriptionTemplate {
    private long id;
    private Long doctorId; // nullable, so use the boxed type
    private String name;
    private String items; // stored as raw JSON string (jsonb column)
    private String createdAt;

    public PrescriptionTemplate(long id, Long doctorId, String name, String items, String createdAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.name = name;
        this.items = items;
        this.createdAt = createdAt;
    }

    public PrescriptionTemplate() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getItems() { return items; }
    public void setItems(String items) { this.items = items; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String toString() {
        return "PrescriptionTemplate{" +
                "id=" + id +
                ", doctorId=" + doctorId +
                ", name='" + name + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}