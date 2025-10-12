package com.securevault.onepass.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY password_name ASC")
    List<PasswordItem> retrieveRecord();

    @Query("SELECT * FROM passwords WHERE id = :id")
    PasswordItem retrieveRecordById(int id);

    @Query("SELECT encrypted_password FROM passwords WHERE id = :id")
    String retrievePasswordById(int id);

    @Insert
    void insertRecord(PasswordItem passwordItem);

    @Update
    void updateRecord(PasswordItem passwordItem);

    @Query("DELETE FROM passwords WHERE id = :id")
    void deleteRecord(int id);
}