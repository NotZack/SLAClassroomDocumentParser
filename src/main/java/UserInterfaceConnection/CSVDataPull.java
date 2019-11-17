package UserInterfaceConnection;

import CSVParsing.Room;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;

class CSVDataPull {

    private ArrayList<Room> csvRooms;

    @Contract(pure = true)
    CSVDataPull(ArrayList<Room> csvRooms) {
        this.csvRooms = csvRooms;
    }

    @Contract(pure = true)
    Room getRoomByName(String roomName) {
        for (Room room : csvRooms) {
            if (room.getName().equals(roomName) || room.getFacilityName().equals(roomName)) {
                return room;
            }
        }
        System.out.println("Room not found");
        return null;
    }
}