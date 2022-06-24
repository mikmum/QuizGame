package Quiz;

public class Player {
    private String name;
    private int score = 0;
    private int wrongAnswers = 0;
    private int id;
    private boolean answered = true;

    public Player(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void increaseScore() {
        this.score++;
    }

    public int getId() {
        return id;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public boolean isAnswered() {
        return answered;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void increaseWrongAnswers()
    {
        this.wrongAnswers++;
    }

    public void resetWrongAnswers() {
        this.wrongAnswers = 0;
    }

}
