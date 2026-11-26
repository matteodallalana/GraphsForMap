import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client implements AutoCloseable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean loggedIn;

    public void start() throws IOException {
        start("127.0.0.1", 9000);
    }

    public void start(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                true);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    public DungeonMap login(int personaggio) throws IOException {
        if (personaggio < 0 || personaggio > 2) {
            throw new IllegalArgumentException("Il personaggio deve essere 0, 1 o 2");
        }
        if (socket == null || socket.isClosed()) {
            throw new IllegalStateException("Connessione non avviata");
        }

        out.println("login," + personaggio);

        String risposta = readMessage();
        if (risposta == null) {
            throw new IOException("Connessione chiusa dal server durante il login");
        }

        if (risposta.startsWith("ERROR")) {
            throw new IOException("Errore del server: " + risposta);
        }

        String datiMappa;

        if (risposta.equals("MAP")) {
            datiMappa = readMessage();
            if (datiMappa == null) {
                throw new IOException("Mappa non ricevuta dopo il messaggio MAP");
            }
        } else if (risposta.startsWith("MAP")) {
            datiMappa = risposta.substring(3).trim();

            if (datiMappa.startsWith(",")
                    || datiMappa.startsWith(":")
                    || datiMappa.startsWith(" ")) {
                datiMappa = datiMappa.substring(1).trim();
            }
        } else {
            throw new IOException("Risposta di login inattesa: " + risposta);
        }

        if (datiMappa.isEmpty()) {
            throw new IOException("Il server ha inviato una mappa vuota");
        }

        loggedIn = true;
        return DungeonMap.parse(datiMappa);
    }

    public String sendCommand(String comando) throws IOException {
        if (!loggedIn) {
            throw new IllegalStateException("Effettuare il login prima di inviare comandi");
        }

        out.println(comando);
        String risposta = readMessage();

        if (risposta == null) {
            throw new IOException("Connessione chiusa dal server");
        }
        if (risposta.startsWith("ERROR")) {
            throw new IOException("Errore del server: " + risposta);
        }
        return risposta;
    }

    public String sendMessage(String comando) throws IOException {
        return sendCommand(comando);
    }

    public String readMessage() throws IOException {
        if (in == null) {
            throw new IllegalStateException("Connessione non avviata");
        }

        String risposta = in.readLine();
        return risposta == null ? null : risposta.trim();
    }

    public void stopConnection() throws IOException {
        close();
    }

    @Override
    public void close() throws IOException {
        IOException erroreChiusura = null;

        if (out != null && loggedIn && socket != null && !socket.isClosed()) {
            out.println("logout");
            out.flush();
            loggedIn = false;
        }

        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            erroreChiusura = e;
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            if (erroreChiusura == null) {
                erroreChiusura = e;
            }
        }

        if (erroreChiusura != null) {
            throw erroreChiusura;
        }
    }
}