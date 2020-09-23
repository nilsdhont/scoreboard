package be.brigandze.control;

import be.brigandze.entity.Event;
import be.brigandze.entity.TeamEventList;
import be.brigandze.entity.Teams;
import be.brigandze.sporteasy.SportEasyResource;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;

public class EventController {

    private static EventController instance;

    SportEasyResource sportEasyResource = SportEasyResource.getInstance();

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
                .map(sportEasyResource::getEvents)
                .filter(Objects::nonNull)
                .map(TeamEventList::getResults)
                .flatMap(List::stream)
                .filter(e -> e.getOpponent_left() != null && e.getOpponent_right() != null)
                .filter(e -> now().atStartOfDay().isBefore(e.getStart_at()))
                .sorted(comparing(Event::getStart_at))
                .findFirst();
        if (event.isPresent()) {
            this.currentMatch = new Match(event.get().getOpponent_left().getId(), event.get().getId());
        }
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }
}
