package be.brigandze.entity;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

// deserialized via hand-rolled JsonbBuilder (not an endpoint type), so native needs explicit registration
@RegisterForReflection
public class TeamEventList {

    private List<Event> results;

    public List<Event> getResults() {
        return results;
    }

    public void setResults(List<Event> results) {
        this.results = results;
    }
}
