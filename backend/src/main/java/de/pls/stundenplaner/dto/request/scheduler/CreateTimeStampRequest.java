package de.pls.stundenplaner.dto.request.scheduler;

import de.pls.stundenplaner.scheduler.type.ScheduleStampType;

public record CreateTimeStampRequest(
        ScheduleStampType type
) {

}
