import java.util.Map;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        String userDir = System.getProperty("user.dir");
        String configFilePath = userDir + "\\DistanceVector\\config.txt";
        Map<String, Router> network = Parser.parseConfigFile(configFilePath);

        // Initially, print the distance vectors of all routers
        System.out.println("Initial state of the network:");
        for (Router router : network.values()) {
            System.out.println("Router " + router.routerId + ": " + router.distanceVector);
        }

        // Simulate the exchange of distance vectors       
        simulateDistanceVectorExchange(network);

        // After updates, print the updated distance vectors
        System.out.println("\nAfter distance vector exchange:");
        for (Router router : network.values()) {
            System.out.println("Router " + router.routerId + ": " + router.distanceVector);
        }
    }

    private static void simulateDistanceVectorExchange(Map<String, Router> network) {
        // each router sends its distance vector to all neighbors
        for (Router sender : network.values()) {
            for (String neighborId : sender.directlyConnectedRouters.keySet()) {
                Router receiver = network.get(neighborId);
                if (receiver != null) {
                    receiver.updateDistanceVector(sender.routerId,
                            convertToSimpleDistanceVector(sender.distanceVector));
                }
            }
        }
        // Note: This simplistic approach does not handle iterative updates or
        // convergence checking.
    }

    // Helper method to convert complex Route objects to simple distance vectors for
    // easy handling
    private static Map<String, Integer> convertToSimpleDistanceVector(Map<String, Router> distanceVector) {
        Map<String, Integer> simpleDistanceVector = new HashMap<>();
        for (Map.Entry<String, Router> entry : distanceVector.entrySet()) {
            simpleDistanceVector.put(entry.getKey(), entry.getValue().cost);
        }
        return simpleDistanceVector;
    }
}
