package com.securevault.onepass.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(tableName = "passwords")
public class PasswordItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "password_name")
    private String passwordName;
    @ColumnInfo(name = "url")
    private String url;
    @ColumnInfo(name = "username")
    private String username;
    @ColumnInfo(name = "encrypted_password")
    private String encryptedPassword;
    @ColumnInfo(name = "password_length")
    private int passwordLength;
    @ColumnInfo(name = "created_date")
    private LocalDate createdDate;

    public PasswordItem(int id, String passwordName, String url, String username, String encryptedPassword, int passwordLength, LocalDate createdDate) {
        this.id = id;
        this.passwordName = passwordName;
        this.url = url;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.passwordLength = passwordLength;
        this.createdDate = createdDate;
    }

    @Ignore
    public PasswordItem(String passwordName, String url, String username, String encryptedPassword, int passwordLength, LocalDate createdDate) {
        this.passwordName = passwordName;
        this.url = url;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.passwordLength = passwordLength;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public String getPasswordName() {
        return passwordName;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }
}