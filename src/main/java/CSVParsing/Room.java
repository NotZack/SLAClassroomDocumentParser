package CSVParsing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class Room {
    private String subject;
    private String description;
    private String startDate;
    private String endDate;
    private String name;
    private String facilityName;
    private String mtgStart;
    private String mtgEnd;
    private Boolean[] dayAvailability;
    private String instructor;
    private String instructorId;

    @Contract(pure = true)
    Room(@NotNull String[] rawCSVLine) {
        subject = rawCSVLine[0];
        description = rawCSVLine[2];
        startDate = rawCSVLine[3];
        endDate = rawCSVLine[4];
        name = rawCSVLine[5];
        facilityName = rawCSVLine[6];
        mtgStart = rawCSVLine[7];
        mtgEnd = rawCSVLine[8];
        dayAvailability = parseDayAvailability(Arrays.copyOfRange(rawCSVLine, 9, 14));
        instructor = rawCSVLine[15];
        instructorId = rawCSVLine[16];

        System.out.println(name + " " + facilityName);

    }

    @NotNull
    private Boolean[] parseDayAvailability(@NotNull String[] dayAvailability) {
        Boolean[] avail = new Boolean[dayAvailability.length];
        for (int i = 0; i < dayAvailability.length; i++) {
            avail[i] = dayAvailability[i].equals("Y") || dayAvailability[i].equals("y");
        }
        return avail;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getName() {
        return name;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public String getMtgStart() {
        return mtgStart;
    }

    public String getMtgEnd() {
        return mtgEnd;
    }

    public Boolean[] getDayAvailability() {
        return dayAvailability;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getInstructorId() {
        return instructorId;
    }
}
