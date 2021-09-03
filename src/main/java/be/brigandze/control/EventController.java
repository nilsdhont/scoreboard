package be.brigandze.control;

import static be.brigandze.control.EventPredicates.eventHasOpponents;
import static be.brigandze.control.EventPredicates.eventIsToday;
import static be.brigandze.control.EventPredicates.eventNotEnded;
import static be.brigandze.control.EventPredicates.isHomeMatch;
import static be.brigandze.sporteasy.SportEasyResource.getSportEasyInstance;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

import be.brigandze.entity.Event;
import be.brigandze.entity.TeamEventList;
import be.brigandze.entity.Teams;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jboss.logging.Logger;

public class EventController {

    private static final Logger LOG = Logger.getLogger(EventController.class);

    private static EventController instance;

    private Match currentMatch;

    private EventController() {
    }

    public static EventController getInstance() {
        if (instance == null) {
            instance = new EventController();
        }
        return instance;
    }

    //TODO add category en check op wedstrijd ==> is al deels via #eventHasOpponents
    void updateCurrentMatch() {
        Optional<Event> event = stream(Teams.values())
            .map(Teams::getId)
            .map(getSportEasyInstance()::getEvents)
            .filter(Objects::nonNull)
            .map(TeamEventList::getResults)
            .flatMap(List::stream)
            .filter(eventHasOpponents)
            .filter(isHomeMatch)
            .filter(eventIsToday)
            .filter(eventNotEnded)
            .min(comparing(Event::getStart_at));
        if (event.isPresent()) {
            Match newMatch = Match.builder()
                .id(event.get().getId())
                .teamId(event.get().getOpponent_left().getId())
                .nameBrigandZe(event.get().getOpponent_left().getName())
                .nameVisitors(event.get().getOpponent_right().getName())
                .build();
            if (currentMatch == null || !currentMatch.equals(newMatch)) {
                this.currentMatch = newMatch;
                currentMatch.printScore();
            }
        } else {
            LOG.info("NO MATCH AVAILABLE");
        }
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }
}
