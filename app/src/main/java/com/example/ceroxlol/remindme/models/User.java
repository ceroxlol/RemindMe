package com.example.ceroxlol.remindme.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Ceroxlol on 13.12.2017.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DatabaseTable(tableName = "USERS")
public class User implements Serializable {
    @DatabaseField(generatedId = true, columnName = "UserID")
    private int id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String password;
}
