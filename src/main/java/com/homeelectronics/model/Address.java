package com.homeelectronics.model;

/**
 * This is a simple POJO (Plain Old Java Object) that maps to the `addresses` table.
 */
public class Address {

    private int id;
    private int userId;
    private String country;
    private String state;
    private String zipCode;
    private String address;
    private boolean isPrimary;

    // Constructors, getters, and setters

    public Address() {
    }

    public Address(int id, int userId, String country, String state, String zipCode, String address, boolean isPrimary) {
        this.id = id;
        this.userId = userId;
        this.country = country;
        this.state = state;
        this.zipCode = zipCode;
        this.address = address;
        this.isPrimary = isPrimary;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
}