package be.brigandze.control;

import static be.brigandze.sporteasy.SportEasyResource.getSportEasyInstance;
import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

import be.brigandze.entity.Event;
import be.brigandze.entity.TeamEventList;
import be.brigandze.entity.Teams;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EventController {

    private static EventController instance;

    private Match currentMatch;

    public static EventController getInstance() {
        if (instance == null) {
            instance = new EventController();
        }
        return instance;
    }

    private EventController() {
    }

    void updateCurrentMatch() {
        //TODO nu krijg je de eerste match van vandaag wss, aanvullen met controle of match al gedaan is en er een volgende evt is
        //      add category en check op wedstrijd
        Optional<Event> event = stream(Teams.values())
            .map(Teams::getId)
            .map(getSportEasyInstance()::getEvents)
            .filter(Objects::nonNull)
            .map(TeamEventList::getResults)
            .flatMap(List::stream)
            .filter(e -> e.getOpponent_left() != null && e.getOpponent_right() != null)
            .filter(e -> now().atStartOfDay().isBefore(e.getStart_at()))
            .sorted(comparing(Event::getStart_at))
            .findFirst();
        if (event.isPresent()) {
            Match newMatch = Match.builder()
                .id(event.get().getId())
                .teamId(event.get().getOpponent_left().getId())
                .nameBrigandZe(event.get().getOpponent_left().getName())
                .nameVisitors(event.get().getOpponent_right().getName())
                .build();
//            new Match(event.get().getOpponent_left().getId(),
//                event.get().getId());
            if (currentMatch == null || !currentMatch.equals(newMatch)) {
                this.currentMatch = newMatch;
                currentMatch.printScore();
            }
        }
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }
}
