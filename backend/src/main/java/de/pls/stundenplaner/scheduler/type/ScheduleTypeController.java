package de.pls.stundenplaner.scheduler.type;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scheduleTypes")
public class ScheduleTypeController {

    @GetMapping
    public List<String> getTypes() {
        return ScheduleStampType.getScheduleStampTypes();
    }

}
