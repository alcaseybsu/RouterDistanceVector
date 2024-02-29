import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    public static Map<String, Router> parseConfigFile(String filename) {
        Map<String, Router> routers = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming a simple format: RouterID, NeighborID:IP:Port, Subnet:Cost
                // Adjust parsing logic based on the actual format of your config file
                if (line.startsWith("#")) {
                    continue; // Comment line, ignore
                } else if (line.contains("Router")) {
                    String[] parts = line.split(",");
                    String routerId = parts[0];
                    routers.putIfAbsent(routerId, new Router(routerId));
                } else if (line.contains("Connection")) {
                    String[] parts = line.split(",");
                    String routerId = parts[0];
                    String neighborId = parts[1];
                    String interfaceId = parts[2]; // IP:Port
                    routers.get(routerId).connectToRouter(neighborId, interfaceId);
                } else if (line.contains("Subnet")) {
                    String[] parts = line.split(",");
                    String routerId = parts[0];
                    String subnet = parts[1];
                    int cost = Integer.parseInt(parts[2]);
                    routers.get(routerId).connectToSubnet(subnet, cost);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return routers;
    }
}
