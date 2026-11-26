import java.io.IOException;
import java.util.List;

public class MapClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 9000;
    private static final long PAUSA_TRA_MOSSE_MS = 500;

    public static void main(String[] args) throws IOException {
        int personaggio = args.length > 0
                ? Integer.parseInt(args[0])
                : 0;

        if (personaggio < 0 || personaggio > 2) {
            throw new IllegalArgumentException(
                    "Il personaggio deve essere 0, 1 o 2");
        }

        try (Client client = new Client()) {
            client.start(HOST, PORTA);

            DungeonMap mappa = client.login(personaggio);
            String posizione = client.getPosizioneIniziale();
            String target = client.getTargetIniziale();

            System.out.println("Personaggio " + personaggio + " collegato.");
            System.out.println("Posizione iniziale: " + posizione);
            System.out.println("Target iniziale: " + target);
            System.out.println();

            while (true) {
                if (!mappa.containsNode(posizione)) {
                    throw new IllegalStateException(
                            "Posizione fuori dal dungeon: " + posizione);
                }

                if (!mappa.containsNode(target)) {
                    throw new IllegalStateException(
                            "Target fuori dal dungeon: " + target);
                }

                if (posizione.equals(target)) {
                    System.out.println(
                            "Il target coincide già con la posizione "
                                    + posizione
                                    + ". Il server potrebbe non generare "
                                    + "un nuovo target: termino il client.");
                    break;
                }

                List<String> percorso =
                        mappa.shortestPath(posizione, target);

                if (percorso == null || percorso.size() < 2) {
                    throw new IllegalStateException(
                            "Nessun percorso valido da "
                                    + posizione + " verso " + target);
                }

                boolean nuovoTarget = false;

                for (int i = 1; i < percorso.size(); i++) {
                    String idPasso = percorso.get(i);

                    if (!mappa.containsNode(idPasso)) {
                        throw new IllegalStateException(
                                "Il percorso contiene un nodo fuori "
                                        + "dal dungeon: " + idPasso);
                    }

                    NodeMap passo = new NodeMap(idPasso);

                    String comando = "move,"
                            + passo.getColonna()
                            + ","
                            + passo.getRiga();

                    System.out.println("Invio comando: " + comando);

                    String risposta = client.sendCommand(comando);
                    System.out.println(comando + " -> " + risposta);

                    if (risposta.startsWith("OK")) {
                        posizione = client.parseCoordinate(risposta);

                        try {
                            Thread.sleep(PAUSA_TRA_MOSSE_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    } else if (risposta.startsWith("SUCCESS")
                            || risposta.startsWith("FAIL")) {
                        posizione = client.parseCoordinate(risposta);
                        target = client.readTarget();

                        System.out.println("Nuova posizione: " + posizione);
                        System.out.println("Nuovo target: " + target);

                        nuovoTarget = true;
                        break;
                    } else {
                        throw new IOException(
                                "Risposta inattesa del server: " + risposta);
                    }
                }

                if (!nuovoTarget) {
                    System.out.println(
                            "Percorso completato; posizione: " + posizione);
                }
            }
        }
    }
}