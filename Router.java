import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Router implements Runnable {
    String routerId;
    Map<String, String> directlyConnectedRouters; // Neighbor ID to "IP:port"
    Map<String, Integer> directlyConnectedSubnets; // Subnet to Cost
    Map<String, Route> distanceVector; // Subnet to Route (cost + next hop)
    private int listeningPort; // router's listening port for UDP communication

    public Router(String routerId, int listeningPort) {
        this.routerId = routerId;
        this.listeningPort = listeningPort;
        this.directlyConnectedRouters = new HashMap<>();
        this.directlyConnectedSubnets = new HashMap<>();
        this.distanceVector = new HashMap<>();
    }

    public void connectToRouter(String routerId, String interfaceId) {
        directlyConnectedRouters.put(routerId, interfaceId);
    }

    public void connectToSubnet(String subnet, int cost) {
        directlyConnectedSubnets.put(subnet, cost);
        distanceVector.put(subnet, new Route(cost, "Directly connected"));
    }

    public void sendDistanceVectorToNeighbors() {
        // convert and log the simplified distance vector for debugging
        Map<String, Integer> simplifiedDistanceVector = Main.convertToSimpleDistanceVector(this.distanceVector);
        System.out.println("Router " + routerId + " DV: " + simplifiedDistanceVector);

        directlyConnectedRouters.forEach((neighborId, ipPortString) -> {
            String[] parts = ipPortString.split(":");
            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);
            sendToNeighbor(ipAddress, port, new HashMap<>(distanceVector));
        });
    }

    private void sendToNeighbor(String ipAddress, int port, Map<String, Route> distanceVector) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(ipAddress);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutput out = new ObjectOutputStream(baos)) {
                out.writeObject(distanceVector);
                byte[] data = baos.toByteArray();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDistanceVector(String neighborId, Map<String, Route> receivedDistanceVector) {
        boolean isUpdated = false;
        for (Map.Entry<String, Route> entry : receivedDistanceVector.entrySet()) {
            String subnet = entry.getKey();
            Route receivedRoute = entry.getValue();
            // add 1 to the received cost to account for the distance to the neighbor
            int newCost = receivedRoute.getCost() + 1;
            if (!distanceVector.containsKey(subnet) || distanceVector.get(subnet).getCost() > newCost) {
                distanceVector.put(subnet, new Route(newCost, neighborId));
                isUpdated = true;
            }
        }
        if (isUpdated) {
            sendDistanceVectorToNeighbors();
        }
    }

    @Override
    public void run() {
        startListening();
    }

    public void startListening() {
        try (DatagramSocket socket = new DatagramSocket(listeningPort)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                try (ObjectInputStream ois = new ObjectInputStream(
                        new ByteArrayInputStream(packet.getData(), 0, packet.getLength()))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Route> receivedDistanceVector = (Map<String, Route>) ois.readObject();
                    // use sender's IP address as the neighborId identifier
                    String neighborId = packet.getAddress().getHostAddress();
                    updateDistanceVector(neighborId, receivedDistanceVector);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // inner class to represent a route
    public static class Route implements java.io.Serializable {
        private int cost;
        private String nextHop;

        public Route(int cost, String nextHop) {
            this.cost = cost;
            this.nextHop = nextHop;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        public String getNextHop() {
            return nextHop;
        }

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
