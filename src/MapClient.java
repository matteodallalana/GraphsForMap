import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MapClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 9000;

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
            int nPersonaggio = (int)(Math.random() * 3);
            DungeonMap mappa = client.login(nPersonaggio);
            Thread.sleep(1000);

            System.out.println("Personaggio " + personaggio + " collegato.");
            System.out.println("Mappa ricevuta:");
            System.out.println(mappa);

            if (mappa.getNodes().isEmpty()) {
                throw new IllegalStateException("La mappa non contiene nodi");
            }

            String radice = args.length > 1
                    ? args[1]
                    : mappa.getNodes().get(0);

            if (!mappa.containsNode(radice)) {
                throw new IllegalArgumentException(
                        "La radice non è presente nella mappa: " + radice);
            }

            GraphNotOriented<String> mst =
                    mappa.minimumSpanningTreeFrom(radice);

            System.out.println();
            System.out.println("Minimum spanning tree con radice " + radice + ":");
            System.out.println(mst);

            Random random = new Random();
            List<String> nodi = mappa.getNodes();

            String posizione = client.getPosizioneIniziale();
            if (posizione == null) {
                posizione = mappa.containsNode("0000") ? "0000" : radice;
            }

            if (!mappa.containsNode(posizione)) {
                throw new IllegalStateException(
                        "Posizione iniziale non presente nella mappa: " + posizione);
            }

            System.out.println("Posizione iniziale: " + posizione);

            while (true) {
                HashMap<String, Distance<String>> distanze =
                        mappa.distancesFrom(posizione);

                List<String> destinazioniRaggiungibili =
                        new ArrayList<>();

                for (String nodo : nodi) {
                    Distance<String> distanza = distanze.get(nodo);

                    if (!nodo.equals(posizione)
                            && distanza != null
                            && distanza.getDistance()
                            != Float.POSITIVE_INFINITY) {
                        destinazioniRaggiungibili.add(nodo);
                    }
                }

                if (destinazioniRaggiungibili.isEmpty()) {
                    System.out.println(
                            "Nessun altro nodo raggiungibile da "
                                    + posizione + ".");
                    break;
                }

                String destinazione =
                        destinazioniRaggiungibili.get(
                                random.nextInt(
                                        destinazioniRaggiungibili.size()));

                List<String> percorso =
                        mappa.shortestPath(posizione, destinazione);

                if (percorso.size() < 2) {
                    System.out.println("Percorso non valido da "
                            + posizione + " a " + destinazione
                            + "; termino.");
                    break;
                }

                System.out.println(
                        "Destinazione casuale: " + destinazione);

                boolean percorsoCompletato = true;

                for (int i = 1; i < percorso.size(); i++) {
                    NodeMap passo = new NodeMap(percorso.get(i));

                    String comando = "move,"
                            + passo.getColonna() + ","
                            + passo.getRiga();

                    System.out.println("Invio comando: " + comando);

                    try {
                        String risposta = client.sendCommand(comando);
                        System.out.println(comando + " -> " + risposta);

                        // Aggiorna la posizione solo dopo una
                        // risposta positiva del server.
                        posizione = percorso.get(i);

                        Thread.sleep(500);
                    } catch (IOException e) {
                        System.out.println(
                                "Movimento rifiutato: " + e.getMessage());
                        percorsoCompletato = false;
                        break;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                if (!percorsoCompletato) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}