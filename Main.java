import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String userDir = System.getProperty("user.dir");        
        String configFilePath = userDir + "/config.txt";
        Map<String, Router> network = Parser.parseConfigFile(configFilePath);

        // Starting UDP listeners for each router
        network.values().forEach(router -> new Thread(router).start());

        // allow time for all routers to start listening
        try {
            Thread.sleep(1000); // adjust if needed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error: " + e.getMessage());
        }

        // trigger the initial exchange of distance vectors
        network.values().forEach(router -> router.sendDistanceVectorToNeighbors());

        // allow time for exchanges to complete
        try {
            Thread.sleep(5000); // adjust based on network size and speed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Error: " + e.getMessage());
        }

        // print the final state of the network
        System.out.println("\nFinal state of the network:");
        network.values().forEach(router -> {
            System.out.println("\nRouter " + router.routerId + " DV: " + router.distanceVector);
        });
    }

    public static Map<String, Integer> convertToSimpleDistanceVector(Map<String, Router.Route> distanceVector) {
        Map<String, Integer> simpleDistanceVector = new HashMap<>();
        distanceVector.forEach((key, value) -> simpleDistanceVector.put(key, value.getCost()));
        return simpleDistanceVector;
    }
}

