package be.brigandze.control;

import static java.time.LocalDate.now;

import be.brigandze.entity.Event;
import be.brigandze.entity.Teams;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Predicate;

public class EventPredicates {

    private EventPredicates() {
    }

    public static Predicate<Event> eventHasOpponents = event ->
        event.getOpponent_left() != null && event.getOpponent_right() != null;

    public static Predicate<? super Event> isHomeMatch = event ->
        Arrays.stream(Teams.values())
            .map(Teams::getId)
            .anyMatch(id -> id == event.getOpponent_left().getId());

    public static Predicate<? super Event> eventIsToday = event ->
        now().atStartOfDay().isBefore(event.getStart_at());

    public static Predicate<? super Event> eventNotEnded = event ->
            LocalDateTime.now().isBefore(event.getEnd_at());
}
