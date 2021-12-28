package Data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import DatabaseServices.LocationPersister;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Ceroxlol on 27.02.2018.
 */
@Getter
@Setter
@NoArgsConstructor
@DatabaseTable
public class VisitedLocation {
    @DatabaseField(generatedId = true, columnName = "LocationID")
    private int id;

    @DatabaseField(canBeNull = false, persisterClass = LocationPersister.class)
    private android.location.Location location;
}
