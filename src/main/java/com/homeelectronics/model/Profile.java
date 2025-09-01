package com.homeelectronics.model;



import java.sql.Date;

public class Profile {
    private int id;
    private int userId;
    private String firstName;
    private String lastName;
    private Date dob;
    private String phoneNumber;

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Date getDob() { return dob; }
    public void setDob(Date dob) { this.dob = dob; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
