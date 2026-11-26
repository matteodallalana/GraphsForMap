import java.util.ArrayList;
import java.util.HashMap;

public class Map extends GraphOriented<String> {

    private final HashMap<String, NodeMap> gridNodes = new HashMap<>();

    @Override
    public void addNode(String id) {
        if (!gridNodes.containsKey(id)) {
            gridNodes.put(id, new NodeMap(id)); // valida il formato
        }
        super.addNode(id);
    }

    public void addNode(int riga, int colonna) {
        addNode(NodeMap.buildId(riga, colonna));
    }

    public NodeMap getGridNode(String id) {
        return gridNodes.get(id);
    }


    @Override
    public void addEdge(String source, String dest, float weight) {
        super.addEdge(source, dest, weight);
        super.addEdge(dest, source, weight);
    }

    public void addEdge(String source, String dest) {
        addEdge(source, dest, 1f);
    }

    public String[] bellmanFord(String source, String dest) {
        return buildPath(bellman_ford(source), source, dest);
    }

    public String[] dijkstra(String source, String dest) {
        return buildPath(dijkstra(source), source, dest);
    }

    private String[] buildPath(HashMap<String, Distance<String>> dist, String source, String dest) {
        if (!nodes.containsKey(dest)) {
            throw new IllegalArgumentException("Nodo destinazione inesistente: " + dest);
        }
        if (dist.get(dest).getDistance() == Float.POSITIVE_INFINITY) {
            return new String[0];
        }
        ArrayList<String> path = new ArrayList<>();
        String cur = dest;
        while (cur != null) {
            path.add(0, cur);
            if (cur.equals(source)) break;
            cur = dist.get(cur).getPrev();
        }
        return path.toArray(new String[0]);
    }
}