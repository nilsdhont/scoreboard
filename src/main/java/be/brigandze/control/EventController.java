package be.brigandze.control;

import static be.brigandze.sporteasy.SportEasyResource.getSportEasyInstance;
import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

import be.brigandze.entity.Event;
import be.brigandze.entity.TeamEventList;
import be.brigandze.entity.Teams;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.jboss.logging.Logger;

public class EventController {

    private static final Logger LOG = Logger.getLogger(EventController.class);

    private static EventController instance;

    public static EventController getInstance() {
        if (instance == null) {
            instance = new EventController();
        }
        return instance;
    }

    private EventController() {
    }


    private Match currentMatch;

    //TODO nu krijg je de eerste match van vandaag wss, aanvullen met controle of match al gedaan is en er een volgende evt is
    //      add category en check op wedstrijd
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

    private Predicate<Event> eventHasOpponents = event -> event.getOpponent_left() != null
        && event.getOpponent_right() != null;

    private Predicate<? super Event> isHomeMatch = event -> Arrays.stream(Teams.values())
        .map(Teams::getId)
        .anyMatch(id -> id == event.getOpponent_left().getId());

    private Predicate<? super Event> eventIsToday = event -> now().atStartOfDay()
        .isBefore(event.getStart_at());
}
