package Quiz;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server implements Runnable{

    private static ArrayList<ConnectionHandler> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;
    private boolean gameStarted = false;
    private Game game;
    private int i = 0;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        }catch(Exception e){
            shutdown();
        }
    }


    public static void broadcast(String message) {
        for(ConnectionHandler ch : connections) {
            if(ch != null) {
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e){
            // ignore
        }
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isGameStarted() {
        return this.gameStarted;
    }

    public Game getGame() {
        return game;
    }

    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private Player player;
        int id;

        public ConnectionHandler(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            try {
                i+= 1;
                if (i < 5){
                    broadcast("Laczenie nowego gracza...");
                    out = new PrintWriter(client.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    out.println("Podaj nick gracza: ");
                    id = i;
                    player = new Player(in.readLine(), id);
                    System.out.println(player.getName() + " polaczyl sie");
                    broadcast(player.getName() + " dolaczyl do gry");

                    if (id == 1) {
                        out.println("Jestes graczem nr 1. Jesli chcesz wystartowac gre, wpisz /start");
                    }
                    else {
                        out.println("Oczekuj na rozpoczecie rozgrywki przez gracza nr 1");
                    }

                    out.println("W oczekiwaniu na gre mozesz poczatowac z innymi graczami :) Po prostu wpisz wiadomosc" +
                            " w konsoli\n");

                    out.println("Gracze obecni teraz w lobby:");
                    for (ConnectionHandler ch: connections){
                        if (ch.getPlayer() != null) { out.println(ch.getPlayer().getName());}
                    }

                    String message;
                    while ( (message = in.readLine()) != null) {
                        if (message.startsWith("/quit")) {
                            broadcast(player.getName() + " opuscila gre");
                            System.out.println(player.getName() + " rozlaczono");
                            shutdown();
                        }
                        else if (!isGameStarted() && message.startsWith("/start") && player.getId() == 1){
                            if (connections.size() < 2) {
                                broadcast("Potrzeba co najmniej 2 graczy by wystartowac");
                            }
                            else {
                                startGame();
                            }
                        }
                        else if (!isGameStarted() && message.startsWith("/start")){
                            out.println("Tylko gracz 1 (" + connections.get(0).getPlayer().getName() + ") moze" +
                                    " rozpoczac gre");
                        }
                        else if ( !isGameStarted() ){
                            broadcast(player.getName() + ": " + message);
                        }
                        else {
                            if(message.matches("([1-9]|10)\\.[1-4]")) {
                                if ( (String.valueOf(message.charAt(0)).equals(
                                        String.valueOf(getGame().getQuestionNumber() + 1) )) ||
                                        ( (getGame().getQuestionNumber()+1 == 10) && message.matches("10\\.[1-4]"))
                                ){
                                    String answer = message.substring(message.length() - 1);
                                    out.println(getGame().getCurrentQuestion().handleAnswer(answer,
                                            player, game.getPlayers()));
                                }
                                else {
                                    out.println("Zly numer pytania. Masz wysoki ping, albo sie pomyliles");
                                }
                            }
                            else { out.println("Podaj odpowiedz w formacie: NrPytania.Odpowiedz  (np. 2.2)"); }
                        }
                    }
                }

            } catch (IOException e) {
                shutdown();
            }
        }

        public Player getPlayer() {
            return player;
        }

        public void sendMessage(String message) {
            if (out != null) { out.println(message); }
        }

        public void shutdown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

        public void startGame() {

            i = 5;

            broadcast("Gra rozpocznie sie za 5 sekund");
            broadcast("Udzielaj odpowiedzi w formacie: NrPytania.Odpowiedz  (np. 2.2)");
            setGameStarted(true);

            List<Question> questions = new ArrayList<>();

            questions.add(new Question("W ktorym roku wybuchla II WS:", "1914", "1918",
                    "1939", "1945", "3"));
            questions.add(new Question("Imie kota z Toma i Jerrego to:", "Tom", "Jerry",
                    "Spike", "Butch", "1"));
            questions.add(new Question("Wzor chemiczny dwutlenku wegla to:", "H2O", "CO2",
                    "C4", "CO", "2"));
            questions.add(new Question("Tytanica wyrezyserowal:", "Steven Spielberg",
                    "Robert B Weide", "Patryk Vega", "James Cameron", "4"));
            questions.add(new Question("Ktory z tych krajow nie jest sasiadem Polski:", "Rosja",
                    "Niemcy", "Wegry", "Czechy", "3"));
            questions.add(new Question("Ktore z nastepujacych nie jest warzywem:", "Pomidor",
                    "Rzodkiew", "Burak", "Pomarancza", "4"));
            questions.add(new Question("Ktory z tych tenisistow nigdy nie wygral Wimbledonu:",
                    "Novak Djokovic", "Roger Federer", "Andy Murray", "Jerzy Janowicz",
                    "4"));
            questions.add(new Question("Club Brugge to druzyna z:", "Francji", "Belgii",
                    "Hiszpanii", "Holandii", "2"));
            questions.add(new Question("Ile jest planet w Ukladzie Slonecznym? ", "Siedem",
                    "Osiem", "Dziewiec", "Dziesiec", "2"));
            questions.add(new Question("O jakiej dzielnicy spiewal Bonus BGC, ze 'nie jest kolorowo'?:",
                    "Lazarz", "Wilda", "Piatkowo", "Jezyce", "1"));
            questions.add(new Question("Czego potrzebuja rosliny do fotosyntezy?",
                    "Tlenku wegla", "Dwutlenku wegla", "Fosforu", "Chlorowodoru",
                    "2"));
            questions.add(new Question("W serii 'Harry Potter' NIE wystepuje postac o imieniu:",
                    "Hagrid", "Minerva", "Lucjusz", "Amadeusz", "4"));
            questions.add(new Question("Ktore z nastepujacych kolorow NIE jest takze gatunkiem herbaty?",
                    "biala", "turkusowa", "rubinowa", "czerwona", "3"));
            questions.add(new Question("Ktory z podanych nizej owadow gryzie?",
                    "Bak", "Osa", "Pszczola", "Trzmiel", "1"));
            questions.add(new Question("Jak nalezy zapisac implikacje w jezyku logiki?",
                    "~", "<->", "->", "v", "3"));
            questions.add(new Question("Czy pizza z ananasem ma prawo istniec? ",
                    "Nie", "Nie", "Nie", "Tak", "4"));
            questions.add(new Question("Jak w jezyku niemieckim mozna powiedziec 'prosze'?",
                    "hund", "bitte", "warum", "entschuldigung", "2"));
            questions.add(new Question("Jakich dzwiekow NIE potrafi wydawac lis?",
                    "Szczekanie", "Krzyk", "Pisk", "Warczenie", "4"));
            questions.add(new Question("Kto wygral licytacje kamienicy w powiesci 'Lalka' B.Prusa'?:",
                    "Stanislaw Wokulski", "Ignacy Rzecki", "Izabela Lecka",
                    "Baronowa Krzeszowska", "1"));
            questions.add(new Question("Ktory z podanych ptakow jest najwiekszy?:",
                    "Wrona", "Gawron", "Kruk", "Sroka", "3"));


            Collections.shuffle(questions);

            List<Question> nQuestions = questions.stream().limit(10).collect(Collectors.toList());

            List<Player> players = new ArrayList<>();

            for(ConnectionHandler ch: connections){
                if(ch != null) {
                    if(ch.getPlayer() != null) {
                        players.add(ch.getPlayer());
                    }
                }
            }

            game = new Game(nQuestions, players);

            pool.execute(game);
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

}
