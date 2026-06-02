package com.eclinic.models;

public class MedicineCategory {
    private long id;
    private String name;
    private String nameVi;
    private String description;
    private int displayOrder;
    private String createdAt;

    public MedicineCategory(long id, String name, String nameVi, String description, int displayOrder, String createdAt) {
        this.id = id;
        this.name = name;
        this.nameVi = nameVi;
        this.description = description;
        this.displayOrder = displayOrder;
        this.createdAt = createdAt;
    }

    public MedicineCategory() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNameVi() { return nameVi; }
    public void setNameVi(String nameVi) { this.nameVi = nameVi; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
