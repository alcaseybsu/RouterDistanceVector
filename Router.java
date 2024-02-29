// routers can see their direct neighbors at startup, but not beyond, 
// which is why they need to run the distanceVector protocol to learn about the rest of the network.
// each router runs the distanceVector protocol. It has a list of all the routers in the network, 
// and it sends its distance vector to all of its neighbors. 
// It also receives distance vectors from its neighbors and updates its own distance vector accordingly. 
// It also has a list of all the subnets in the network, and it uses the distance vector to determine the 
// best path to each subnet. It also has a list of all the hosts in the network, and it uses the distance 
//vector to determine the best path to each host.
// DistanceVector class in here?? Or just a function that returns the distance vector?
// map: key: subnet, value: overall distance (total of costs)
// count only subnets, not routers in the "cost" (don't count the dest. subnet)
// R1 to N4 = 1, R1 to N1, N2, and N3 = 0
// enable multiple router instances

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
    

public class Router {
    String routerId;    
    Map<String, String> directlyConnectedRouters = new HashMap<>(); // This should map neighbor ID to "IP:port"
    Map<String, Integer> directlyConnectedSubnets = new HashMap<>(); // Subnet and Cost
    Map<String, Route> distanceVector = new HashMap<>(); // Subnet and Route (cost + next hop)

    public Router(String routerId) {
        this.routerId = routerId;
    }

    public void connectToRouter(String routerId, String interfaceId) {
        directlyConnectedRouters.put(routerId, interfaceId);
    }

    public void connectToSubnet(String subnet, int cost) {
        directlyConnectedSubnets.put(subnet, cost);
        distanceVector.put(subnet, new Route(cost, routerId)); // Directly connected subnets have a cost and the router itself as the next hop
    }

    public void updateDistanceVector(String neighborId, Map<String, Integer> receivedDistanceVector) {
        boolean isUpdated = false;
    
        for (Map.Entry<String, Integer> entry : receivedDistanceVector.entrySet()) {
            String subnet = entry.getKey();
            Integer costToSubnetThroughNeighbor = entry.getValue() + directlyConnectedSubnets.get(neighborId); // Add the cost to reach the neighbor
    
            // If the subnet is not in the distance vector, or if a shorter path through this neighbor is found
            if (!distanceVector.containsKey(subnet) || distanceVector.get(subnet).cost > costToSubnetThroughNeighbor) {
                distanceVector.put(subnet, new Route(costToSubnetThroughNeighbor, neighborId));
                isUpdated = true;
            }
        }
    
        // If the distance vector was updated, propagate the change to neighbors (not shown here)
        if (isUpdated) {
            sendDistanceVectorToNeighbors();
        }
    }
    
    private void sendDistanceVectorToNeighbors() {
        for (String neighbor : directlyConnectedRouters.keySet()) {
            // Create a copy of the distance vector for modification
            Map<String, Route> modifiedDistanceVector = new HashMap<>(distanceVector);

            // Iterate over the distance vector to apply the split horizon rule
            modifiedDistanceVector.entrySet().removeIf(entry -> entry.getValue().nextHop.equals(neighbor));

            // Send the modified distance vector to the neighbor
            sendToNeighbor(neighbor, modifiedDistanceVector);
        }
    }

    private void sendToNeighbor(String neighbor, Map<String, Route> distanceVector) {
        // Extracting neighbor IP address and port
        String[] parts = directlyConnectedRouters.get(neighbor).split(":");
        String ipAddress = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(ipAddress);

            // Serialize the distance vector map to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutput out = new ObjectOutputStream(baos)) {
                out.writeObject(distanceVector);
                out.flush();
                byte[] data = baos.toByteArray();

                // Send the byte array over UDP to the neighbor
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    // inner class to represent a route
    public class Route {
        private int cost;
        private String nextHop;
    
        // Constructor
        public Route(int cost, String nextHop) {
            this.cost = cost;
            this.nextHop = nextHop;
        }
    
        // Getter for cost
        public int getCost() {
            return cost;
        }
    
        // Setter for cost
        public void setCost(int cost) {
            this.cost = cost;
        }
    
        // Getter for nextHop
        public String getNextHop() {
            return nextHop;
        }
    
        // Setter for nextHop
        public void setNextHop(String nextHop) {
            this.nextHop = nextHop;
        }
    
        @Override
        public String toString() {
            return "Route{" +
                    "cost=" + cost +
                    ", nextHop='" + nextHop + '\'' +
                    '}';
        }
    }
}
    
