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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean; // thread-safe boolean flag

public class Router implements Runnable {
    String routerId;
    Map<String, String> directlyConnectedRouters; // Neighbor ID to "IP:port"
    Map<String, Integer> directlyConnectedSubnets; // Subnet to Cost
    Map<String, Route> distanceVector; // Subnet to Route (cost + next hop)
    private int listeningPort; // router's listening port for UDP communication
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean pendingUpdate = false; // flag to track pending updates; reduces network chatter
    // for detecting significant changes and sending triggered updates:
    private Map<String, Integer> lastKnownDirectlyConnectedSubnets = new HashMap<>();
    private Map<String, Route> lastKnownDistanceVector = new HashMap<>();

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

        directlyConnectedRouters.forEach((neighborId, ipPortString) -> {
            // prepare modified DV for each neighbor according to split horizon rule
            Map<String, Route> modifiedDistanceVector = new HashMap<>(distanceVector);

            // remove routes learned from this neighbor before sending DV
            modifiedDistanceVector.entrySet().removeIf(e -> e.getValue().getNextHop().equals(neighborId));

            // convert to simplified DV for debugging, if needed
            Map<String, Integer> simplifiedDistanceVector = Main.convertToSimpleDistanceVector(modifiedDistanceVector);
            System.out.println("Router " + routerId + " DV to " + neighborId + ": " + simplifiedDistanceVector);

            // extracting neighbor IP address and port
            String[] parts = ipPortString.split(":");
            String ipAddress = parts[0];
            int port = Integer.parseInt(parts[1]);

            // send modified distance vector to neighbor
            sendToNeighbor(ipAddress, port, modifiedDistanceVector);
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

    public void updateDistanceVector(String neighborId, Map<String, Router.Route> receivedDistanceVector) {
        AtomicBoolean isUpdated = new AtomicBoolean(false);
        int costToNeighbor = directlyConnectedSubnets.getOrDefault(neighborId, Integer.MAX_VALUE);

        for (Map.Entry<String, Router.Route> entry : receivedDistanceVector.entrySet()) {
            String destination = entry.getKey();
            Router.Route receivedRoute = entry.getValue();

            if (!directlyConnectedRouters.containsKey(neighborId)) {
                continue; // if neighbor isn't directly connected, skip
            }

            int newCost = receivedRoute.getCost() + costToNeighbor;
            Router.Route currentRoute = distanceVector.get(destination);

            if (currentRoute == null || currentRoute.getCost() > newCost) {
                distanceVector.put(destination, new Router.Route(newCost, neighborId));
                isUpdated.set(true);
            }
        }

        new HashMap<>(distanceVector).forEach((dest, route) -> {
            if (route.getNextHop().equals(neighborId)) {
                Router.Route updatedRoute = receivedDistanceVector.get(dest);
                int updatedCost = (updatedRoute != null ? updatedRoute.getCost() : Integer.MAX_VALUE) + costToNeighbor;

                if (updatedCost < Integer.MAX_VALUE && (route.getCost() != updatedCost)) {
                    distanceVector.put(dest, new Router.Route(updatedCost, neighborId));
                    isUpdated.set(true);
                } else if (updatedCost >= Integer.MAX_VALUE) {
                    distanceVector.remove(dest); // invalidate the route if no longer reachable
                    isUpdated.set(true);
                }
            }
        });

        if (isUpdated.get()) {
            System.out.println("\nDV updated for " + routerId);
            sendDistanceVectorToNeighbors(); // immediate update
            pendingUpdate = true; // mark for scheduled update if needed
            detectAndSendTriggeredUpdates(); // check for and handle significant changes
        }
    }

    public void updateLinkCost(String neighborId, int newCost) {
        Integer currentCost = directlyConnectedSubnets.get(neighborId);
        if (currentCost != null && currentCost != newCost) {
            directlyConnectedSubnets.put(neighborId, newCost);
            System.out.println("\nLink cost updated for neighbor " + neighborId + " to " + newCost);
            detectAndSendTriggeredUpdates(); // check for and handle significant changes after updating link cost
        }
    }

    @Override
    public void run() {
        startListening();
        scheduleDistanceVectorUpdates();
    }

    private void scheduleDistanceVectorUpdates() {
        final Runnable updater = () -> {
            if (pendingUpdate) {
                sendDistanceVectorToNeighbors();
                pendingUpdate = false; // reset flag after sending the update
            }
        };
        // schedule the periodic update task; adjust initial delay and period as needed
        scheduler.scheduleAtFixedRate(updater, 0, 10, TimeUnit.SECONDS);
    }

    public void detectAndSendTriggeredUpdates() {
        if (detectSignificantChange()) {
            System.out.println("\nSignificant change detected by " + routerId + "; sending triggered update.");
            sendDistanceVectorToNeighbors();
        }
    }

    public boolean detectSignificantChange() {
        boolean significantChangeDetected = false;

        // check for changes in directly connected link costs
        for (Map.Entry<String, Integer> entry : directlyConnectedSubnets.entrySet()) {
            String neighbor = entry.getKey();
            Integer currentCost = entry.getValue();
            Integer lastKnownCost = lastKnownDirectlyConnectedSubnets.getOrDefault(neighbor, null);

            if (!currentCost.equals(lastKnownCost)) {
                significantChangeDetected = true;
                lastKnownDirectlyConnectedSubnets.put(neighbor, currentCost); // update "snapshot"
            }
        }

        // check for changes in DV--new routes, removed routes, cost changes
        if (!distanceVector.equals(lastKnownDistanceVector)) {
            significantChangeDetected = true;
            lastKnownDistanceVector = new HashMap<>(distanceVector); // update "snapshot"
        }

        return significantChangeDetected;
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

    // graceful shutdown of the scheduler
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
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
