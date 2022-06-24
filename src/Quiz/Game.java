package Quiz;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Game implements Runnable{
    private int questionNumber = 0;
    private List<Question> questions;
    private List<Player> players;

    public Game(List<Question> questions, List<Player> players){
        this.questions = questions;
        this.players = players;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public Question getCurrentQuestion() {
        return questions.get(questionNumber);
    }

    public void playGame() throws InterruptedException, IOException {

        TimeUnit.SECONDS.sleep(5);

        System.out.println("Po wyświetleniu każdego pytania masz 10 sekund na odpowiedź\n");
        Server.broadcast("Po wyswietleniu kazdego pytania masz 10 sekund na odpowiedz\n");

        for (Question currentQuestion : questions){

            System.out.println("Pytanie nr " + (getQuestionNumber()+1) +":\n");
            Server.broadcast("Pytanie nr " + (getQuestionNumber()+1) +":\n");

            currentQuestion.handleQuestion(players);
            questionNumber++;
        }
        System.out.println("Koniec gry. Ostateczna punktacja:\n");
        Server.broadcast("Koniec gry. Ostateczna punktacja:\n");
        getPlayers().sort(Comparator.comparing(Player::getScore).reversed());
        int miejsce = 1;
        for(Player player: getPlayers()){
                System.out.println(miejsce + ". " + player.getName() + ", Wynik: " + player.getScore());
                Server.broadcast(miejsce + ". " + player.getName() + ", Wynik: " + player.getScore());
                miejsce++;
        }
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void run() {
        try {
            playGame();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

}
