package be.brigandze.entity;

import static be.brigandze.util.Utils.isNumeric;

import io.quarkus.runtime.annotations.RegisterForReflection;

// deserialized via hand-rolled JsonbBuilder (not an endpoint type), so native needs explicit registration
@RegisterForReflection
public class Opponent {

    private String full_name;
    private String short_name;
    private int score;
    private int id;

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(Object score) {
        if (score instanceof String) {
            String scoreString = (String) score;
            this.score = isNumeric(scoreString) ? Integer.parseInt(scoreString) : 0;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return short_name != null && !short_name.isEmpty() ? short_name : full_name;
    }
}
