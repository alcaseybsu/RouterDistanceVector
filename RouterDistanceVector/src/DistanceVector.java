//enables the router to calculate the
// distance between itself and every subnet
import java.util.HashMap;
import java.util.Map;

//TODO: Make this universal, utilizing the Parser class, not hard-coded, so that it works with other config files.

public static class DistanceVector {

    private String name;
    private Map<String, Integer> directlyConnectedSubnets;
    private Map<String, Integer> distanceVector;

    public Router(String name, Map<String, Integer> directlyConnectedSubnets) {
        this.name = name;
        this.directlyConnectedSubnets = directlyConnectedSubnets;
        this.distanceVector = new HashMap<>(directlyConnectedSubnets);
    }

    public void exchangeDistanceVectors(Map<Router, Map<String, Integer>> neighborsDistanceVectors) {
        for (Map.Entry<Router, Map<String, Integer>> entry : neighborsDistanceVectors.entrySet()) {
            Router neighborRouter = entry.getKey();
            Map<String, Integer> neighborDistanceVector = entry.getValue();
            updateDistanceVector(neighborRouter, neighborDistanceVector);
        }
    }
    private void updateDistanceVector(Router neighborRouter, Map<String, Integer> neighborDistanceVector) {
        for (Map.Entry<String, Integer> entry : neighborDistanceVector.entrySet()) {
            String subnet = entry.getKey();
            int cost = entry.getValue();
            if (!distanceVector.containsKey(subnet) || distanceVector.get(subnet) > cost + 1) {
                distanceVector.put(subnet, cost + 1);
            }
        }
    }

    public void printDistanceVector() {
        System.out.println(name + "'s Distance Vector:");
        for (Map.Entry<String, Integer> entry : distanceVector.entrySet()) {
            System.out.println("To " + entry.getKey() + ": Cost " + entry.getValue());
        }
        System.out.println();
    }

    public static void main(String[] args) {
        // Initial setup
        Map<String, Integer> R1DirectlyConnected = new HashMap<>();
        R1DirectlyConnected.put("N1", 1);
        R1DirectlyConnected.put("N2", 1);
        R1DirectlyConnected.put("N3", 1);
        Router R1 = new Router("R1", R1DirectlyConnected);

        Map<String, Integer> R2DirectlyConnected = new HashMap<>();
        R2DirectlyConnected.put("N3", 1);
        R2DirectlyConnected.put("N4", 1);
        Router R2 = new Router("R2", R2DirectlyConnected);

        Map<String, Integer> R3DirectlyConnected = new HashMap<>();
        R3DirectlyConnected.put("N4", 1);
        R3DirectlyConnected.put("N5", 1);
        R3DirectlyConnected.put("N6", 1);
        Router R3 = new Router("R3", R3DirectlyConnected);

        // After DV exchange
        Map<Router, Map<String, Integer>> neighborsDistanceVectorsR1 = new HashMap<>();
        neighborsDistanceVectorsR1.put(R2, Map.of("N3", 1, "N4", 2, "N5", 3, "N6", 3));
        neighborsDistanceVectorsR1.put(R3, Map.of("N4", 2, "N5", 3, "N6", 3));
        R1.exchangeDistanceVectors(neighborsDistanceVectorsR1);

        Map<Router, Map<String, Integer>> neighborsDistanceVectorsR2 = new HashMap<>();
        neighborsDistanceVectorsR2.put(R1, Map.of("N1", 2, "N2", 2, "N4", 1, "N5", 2, "N6", 2));
        neighborsDistanceVectorsR2.put(R3, Map.of("N4", 1, "N5", 2, "N6", 2));
        R2.exchangeDistanceVectors(neighborsDistanceVectorsR2);

        Map<Router, Map<String, Integer>> neighborsDistanceVectorsR3 = new HashMap<>();
        neighborsDistanceVectorsR3.put(R1, Map.of("N1", 3, "N2", 3, "N3", 2, "N5", 1, "N6", 1));
        neighborsDistanceVectorsR3.put(R2, Map.of("N1", 3, "N2", 3, "N3", 2, "N5", 1, "N6", 1));
        R3.exchangeDistanceVectors(neighborsDistanceVectorsR3);

        // Print updated distance vectors
        R1.printDistanceVector();
        R2.printDistanceVector();
        R3.printDistanceVector();
    }
}
