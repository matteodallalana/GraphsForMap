
public class NodeMap {

    private final String id;
    private final int riga;
    private final int colonna;

    public NodeMap(int riga, int colonna) {
        if (riga < 0 || riga > 99 || colonna < 0 || colonna > 99) {
            throw new IllegalArgumentException(
                    "Riga e colonna devono essere tra 0 e 99: riga=" + riga + ", colonna=" + colonna);
        }
        this.riga = riga;
        this.colonna = colonna;
        this.id = buildId(riga, colonna);
    }

    public NodeMap(String id) {
        if (id == null || !id.matches("\\d{4}")) {
            throw new IllegalArgumentException("Id non valido (atteso ccrr, 4 cifre): " + id);
        }
        this.id = id;
        this.colonna = Integer.parseInt(id.substring(0, 2));
        this.riga = Integer.parseInt(id.substring(2, 4));
    }

    public static String buildId(int riga, int colonna) {
        return pad2(colonna) + pad2(riga);
    }

    private static String pad2(int n) {
        if (n < 10) {
            return "0" + n;
        }
        return String.valueOf(n);
    }

    public String getId() {
        return id;
    }

    public int getRiga() {
        return riga;
    }

    public int getColonna() {
        return colonna;
    }

    @Override
    public String toString() {
        return id + "(riga=" + riga + ", col=" + colonna + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeMap)) return false;
        return id.equals(((NodeMap) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}