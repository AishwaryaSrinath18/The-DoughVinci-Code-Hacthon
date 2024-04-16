import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class WeightedGraph {
    private Map<String, Map<String, Integer>> graph;

    public WeightedGraph() {
        this.graph = new HashMap<>();
    }

    public void addVertex(String vertex) {
        if (!graph.containsKey(vertex)) {
            graph.put(vertex, new HashMap<>());
        }
    }

    public void addEdge(String source, String destination, int weight) {
        graph.get(source).put(destination, weight);
        graph.get(destination).put(source, weight);
    }

    public int getWeight(String source, String destination) {
        return graph.get(source).get(destination);
    }

    // Method to calculate distance between two locations (kitchens or customers)
    public int calculateDistance(String source, String destination) {
        // Replace this with actual distance calculation logic based on coordinates
        return Math.abs(source.hashCode() - destination.hashCode());
    }
}

class Order {
    String orderId;
    String kitchenId;
    String customerId;
    int pickupTime; // Order ready time

    public Order(String orderId, String kitchenId, String customerId, int pickupTime) {
        this.orderId = orderId;
        this.kitchenId = kitchenId;
        this.customerId = customerId;
        this.pickupTime = pickupTime;
    }
}

class Rider {
    String riderId;
    List<Order> orders;

    public Rider(String riderId) {
        this.riderId = riderId;
        this.orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        orders.add(order);
    }
}

public class DeliveryOptimizer {
    private WeightedGraph cityGraph;
    private List<Order> orders;

    public DeliveryOptimizer() {
        this.cityGraph = new WeightedGraph();
        this.orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void addLocation(String location) {
        cityGraph.addVertex(location);
    }

    public void addEdge(String source, String destination, int distance) {
        cityGraph.addEdge(source, destination, distance);
    }

    public List<Rider> optimizeDelivery() {
        List<Rider> riders = new ArrayList<>();
        for (Order order : orders) {
            boolean assigned = false;
            for (Rider rider : riders) {
                if (rider.orders.isEmpty()) {
                    rider.addOrder(order);
                    assigned = true;
                    break;
                }
                Order lastOrder = rider.orders.get(rider.orders.size() - 1);

                // Rule #1
                if (order.kitchenId.equals(lastOrder.kitchenId) &&
                        order.customerId.equals(lastOrder.customerId) &&
                        Math.abs(order.pickupTime - lastOrder.pickupTime) <= 10) {
                    rider.addOrder(order);
                    assigned = true;
                    break;
                }

                // Rule #2
                else if (!order.kitchenId.equals(lastOrder.kitchenId) &&
                        order.customerId.equals(lastOrder.customerId) &&
                        Math.abs(order.pickupTime - lastOrder.pickupTime) <= 10 &&
                        cityGraph.calculateDistance(order.kitchenId, lastOrder.kitchenId) <= 1) {
                    rider.addOrder(order);
                    assigned = true;
                    break;
                }

                // Rule #3
                else if (order.kitchenId.equals(lastOrder.kitchenId) &&
                        !order.customerId.equals(lastOrder.customerId) &&
                        Math.abs(order.pickupTime - lastOrder.pickupTime) <= 10 &&
                        cityGraph.calculateDistance(order.customerId, lastOrder.customerId) <= 1) {
                    rider.addOrder(order);
                    assigned = true;
                    break;
                }

                // Rule #4: Two orders to the same customer, 2nd kitchen's pickup on the way
                else if (order.customerId.equals(lastOrder.customerId) &&
                        cityGraph.calculateDistance(lastOrder.kitchenId, order.kitchenId) +
                                Math.abs(lastOrder.pickupTime - order.pickupTime) <= 10) {
                    rider.addOrder(order);
                    assigned = true;
                    break;
                }

                // Rule #5: Two orders, 2nd customer's drop on the way to the 1st customer
                else if (cityGraph.calculateDistance(lastOrder.customerId, order.customerId) <= 1 &&
                        cityGraph.calculateDistance(lastOrder.kitchenId, order.kitchenId) +
                                Math.abs(lastOrder.pickupTime - order.pickupTime) <= 10) {
                    rider.addOrder(order);
                    assigned = true;
                    break;
                }

                // Rule #6: Two orders from the same kitchen, 2nd customer's drop on the way to the 1st
                else if (order.kitchenId.equals(lastOrder.kitchenId) &&
                        cityGraph.calculateDistance(lastOrder.customerId, order.customerId) <= 1 &&
                        Math.abs(order.pickupTime - lastOrder.pickupTime) <= 10) {
                    rider.addOrder(order);
                    assigned = true;
                    break;
                }
            }

            if (!assigned) {
                Rider newRider = new Rider("Rider" + (riders.size() + 1));
                newRider.addOrder(order);
                riders.add(newRider);
            }
        }
        return riders;
    }

    public static void main(String[] args) {
        DeliveryOptimizer optimizer = new DeliveryOptimizer();

        // Add locations (kitchens and customers)
        optimizer.addLocation("KitchenA");
        optimizer.addLocation("KitchenB");
        optimizer.addLocation("KitchenC");
        optimizer.addLocation("CustomerA");
        optimizer.addLocation("CustomerB");
        optimizer.addLocation("CustomerC");

        // Add edges with distances (1 km = 1000 units for simplicity)
        optimizer.addEdge("KitchenA", "CustomerA", 500); // Example distances
        optimizer.addEdge("KitchenA", "CustomerB", 800);
        optimizer.addEdge("KitchenA", "CustomerC", 700);
        optimizer.addEdge("KitchenB", "CustomerA", 600);
        optimizer.addEdge("KitchenB", "CustomerB", 400);
        optimizer.addEdge("KitchenB", "CustomerC", 900);
        optimizer.addEdge("KitchenC", "CustomerA", 300);
        optimizer.addEdge("KitchenC", "CustomerB", 200);
        optimizer.addEdge("KitchenC", "CustomerC", 500);

        // Add orders based on the given rules
        optimizer.addOrder(new Order("1", "KitchenA", "CustomerA", 10));
        optimizer.addOrder(new Order("2", "KitchenA", "CustomerA", 20));
        optimizer.addOrder(new Order("3", "KitchenB", "CustomerB", 10));
        optimizer.addOrder(new Order("4", "KitchenA", "CustomerB", 10));
        optimizer.addOrder(new Order("5", "KitchenC", "CustomerC", 10));
        optimizer.addOrder(new Order("6", "KitchenC", "CustomerC", 20));

        List<Rider> optimizedDeliveries = optimizer.optimizeDelivery();
        for (Rider rider : optimizedDeliveries) {
            System.out.println("Rider: " + rider.riderId);
            for (Order order : rider.orders) {
                System.out.println("Order: " + order.orderId);
            }
        }
    }
}
