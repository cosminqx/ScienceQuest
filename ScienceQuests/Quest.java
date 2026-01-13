public class Quest {

    private String description;
    private boolean completed;

    public Quest(String description) {
        this.description = description;
        this.completed = false;
    }

    public String getDescription() {
        return completed ? "Quest completat!" : description;
    }

    public void complete() {
        completed = true;
    }
}
