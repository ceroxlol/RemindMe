package Data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by Ceroxlol on 13.12.2017.
 */

@DatabaseTable(tableName = "USERS")
public class User implements Serializable {
    @DatabaseField(generatedId = true, columnName = "UserID")
    private int id;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField
    private String password;

    //default ctor
    public User()
    {
        // ORMLite needs a no-arg constructor
    }


    public User(int id, String name, String password)
    {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    //getter
    public User getUser()
    {
        return this;
    }
    public String getName(){return this.name;}
    public String getPassword(){return this.password;}
    //setter
    public void setName(String name){this.name = name;}
    public void setPassword(String password){this.password = password;}
}
