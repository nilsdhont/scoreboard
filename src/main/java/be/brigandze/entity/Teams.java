package be.brigandze.entity;

public enum Teams {
    BRIGANDZE(594671),
    BRIGANDZELLES(605596);

    private final int id;

    Teams(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
