import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Parser {

    public static Map<String, Router> parseConfigFile(String filename) {
        Map<String, Router> routers = new HashMap<>();
        Map<String, String> routerIPPortMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue; // skip comments
                else if (line.contains(",")) { // process IP:Port for routers
                    String[] parts = line.split(",");
                    String routerId = parts[0];
                    String[] ipPort = parts[1].split(":");
                    int port = Integer.parseInt(ipPort[1]);
                    routers.putIfAbsent(routerId, new Router(routerId, port));
                    routerIPPortMap.put(routerId, parts[1]); // map routerId to "IP:port"
                } else if (line.contains(":")) { // process connections and subnets
                    String[] parts = line.split(":");
                    String routerId = parts[0];
                    if (parts[1].contains(".")) { // router to subnet mapping
                        String[] subnets = parts[1].split("\\.");
                        for (String subnet : subnets) {
                            routers.get(routerId).connectToSubnet(subnet, 1); // default cost of 1
                        }
                    } else { // router to router connection
                        String neighborId = parts[1];
                        String interfaceId = routerIPPortMap.get(neighborId); // get "IP:port" for neighbor
                        if (interfaceId != null) {
                            routers.get(routerId).connectToRouter(neighborId, interfaceId);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return routers;
    }
}

