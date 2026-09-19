package be.brigandze.entity;

import static be.brigandze.util.Utils.isNotNullString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.json.bind.annotation.JsonbDateFormat;

public class Event {

    private int id;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime start_at;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime end_at;

    private Opponent opponent_left;
    private Opponent opponent_right;

    private Links _links;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getStart_at() {
        return start_at;
    }

    public void setStart_at(LocalDateTime start_at) {
        this.start_at = start_at;
    }

    public LocalDateTime getEnd_at() {
        return end_at;
    }

    public void setEnd_at(Object end_at) {
        if (end_at != null && isNotNullString(end_at)) {
            this.end_at = LocalDateTime.parse((String) end_at,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX][X]"));
        }
    }

    public void setEnd_atFromLocalDateTime(LocalDateTime end_at){
        this.end_at = end_at;
    }

    public Opponent getOpponent_left() {
        return opponent_left;
    }

    public void setOpponent_left(Opponent opponent_left) {
        this.opponent_left = opponent_left;
    }

    public Opponent getOpponent_right() {
        return opponent_right;
    }

    public void setOpponent_right(Opponent opponent_right) {
        this.opponent_right = opponent_right;
    }

    public Links get_links() {
        return _links;
    }

    public void set_links(Links _links) {
        this._links = _links;
    }

}
