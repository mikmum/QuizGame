package Quiz;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Question {
    private final String questionText;
    private final String[] answers = new String[4];
    private final String correctAnswer;
    private boolean gotAnswer = false;

    public Question(String questionText, String answer1, String answer2, String answer3, String answer4,
                    String correctAnswer ){
        this.questionText = questionText;
        this.answers[0] = answer1;
        this.answers[1] = answer2;
        this.answers[2] = answer3;
        this.answers[3] = answer4;
        this.correctAnswer = correctAnswer;
    }

    public String[] getAnswers() {
        return answers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public Boolean isCorrect( String givenAnswer ){
        return givenAnswer.equals( getCorrectAnswer() );
    }

    public void setGotAnswer(boolean gotAnswer) {
        this.gotAnswer = gotAnswer;
    }

    public boolean isGotAnswer() {
        return gotAnswer;
    }

    public void writeOutQuestion(){
        System.out.println(questionText);
        Server.broadcast(questionText);
        System.out.println("1." + answers[0] + " 2." + answers[1] + " 3." + answers[2] + " 4." + answers[3] + "\n");
        Server.broadcast("1." + answers[0] + " 2." + answers[1] + " 3." + answers[2] + " 4." + answers[3] + "\n");
    }

    public void handleQuestion(List<Player> players) throws InterruptedException{

        TimeUnit.SECONDS.sleep(2);

        for(Player player: players){
            if (player.getWrongAnswers() == 3){
                Server.broadcast("UWAGA! Gracz " + player.getName() + " przez 3 bledne odpowiedzi pauzuje w tej turze\n");
                player.resetWrongAnswers();
                player.setAnswered(true);
            }
            else { player.setAnswered(false); }
        }

        setGotAnswer(false);

        this.writeOutQuestion();

        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < 10 * 1000) {
            if (isGotAnswer()){
                break;
            }
            else { TimeUnit.SECONDS.sleep(1); }
        }

        if (!isGotAnswer()) {
            System.out.println("Czas minal\n");
            Server.broadcast("Czas minal\n");
        }

        for(Player player: players){
            player.setAnswered(true);
        }

        TimeUnit.SECONDS.sleep(2);
    }

    public String handleAnswer(String answer, Player answeringPlayer, List<Player> players){

        if(answeringPlayer.isAnswered()){ return "Nie mozesz teraz odpowiadac"; }

        answeringPlayer.setAnswered(true);

        if (this.isCorrect( answer ) ){
            for(Player player: players){
                player.setAnswered(true);
            }
            answeringPlayer.increaseScore();
            Server.broadcast("Gracz " + answeringPlayer.getName() + " jako pierwszy odpowiedzial poprawnie!");
            this.setGotAnswer(true);
            return "Prawidlowa odpowiedz " + answeringPlayer.getName() + " ! Twoj obecny wynik: " +
                    answeringPlayer.getScore() + "\n";
        }
        else {
            answeringPlayer.increaseWrongAnswers();
            return "Zla odpowiedz :(\n";
        }
    }

}
