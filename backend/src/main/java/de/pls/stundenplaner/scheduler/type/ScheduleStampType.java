package de.pls.stundenplaner.scheduler.type;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public enum ScheduleStampType {

    LESSON,
    BREAK;

    public static List<String> getScheduleStampTypes() {
        return Arrays.stream(ScheduleStampType.values())
                .map(Enum::name)
                .toList();
    }

}
