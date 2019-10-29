import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

public class CSVDataInteraction {

    private ArrayList<Room> csvRooms;

    @Contract(pure = true)
    CSVDataInteraction(ArrayList<Room> csvRooms) {
        this.csvRooms = csvRooms;
    }

    @Contract(pure = true)
    public Room getRoomByName(String roomNumber) {
        return null;
    }
}
