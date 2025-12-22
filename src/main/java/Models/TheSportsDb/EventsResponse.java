package Models.TheSportsDb;

import java.util.List;

public class EventsResponse {
    public List<Event> events;

    @Override
    public String toString() {
        String output = "<b>🏁 Eventi \n\n</b>";

        for (Event e : events)
            output = output.concat(e.toString() + "\n");

        return output;
    }
}

