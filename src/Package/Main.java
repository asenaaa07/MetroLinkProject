package Package;

import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Collections;

public class Main {

    static class Edge {
        String to;
        double time;
        String line;

        Edge(String to, double time, String line) {
            this.to = to;
            this.time = time;
            this.line = line;
        }
    }

    static class Node implements Comparable<Node> {
        String station;
        String line;
        double time;
        int changes;
        String mode;

        Node(String station, String line, double time, int changes, String mode) {
            this.station = station;
            this.line = line;
            this.time = time;
            this.changes = changes;
            this.mode = mode;
        }

        public int compareTo(Node other) {
            if (mode.equals("changes")) {
                if (this.changes != other.changes) {
                    return Integer.compare(this.changes, other.changes);
                }
                return Double.compare(this.time, other.time);
            } else {
                return Double.compare(this.time, other.time);
            }
        }
    }

    static class RouteResult {
        List<String> routeKeys;
        double time;
        int changes;

        RouteResult(List<String> routeKeys, double time, int changes) {
            this.routeKeys = routeKeys;
            this.time = time;
            this.changes = changes;
        }
    }

    public static void main(String[] args) {

        Set<String> stations = new HashSet<String>();
        Map<String, List<Edge>> graph = new HashMap<String, List<Edge>>();

        try {
            Scanner fileReader = new Scanner(new File("Metrolink_times_linecolour.csv"));

            String currentLineColour = "unknown";

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length == 1) {
                    currentLineColour = parts[0].trim().toLowerCase();
                    continue;
                }

                if (parts.length >= 3) {
                    try {
                        String from = parts[0].trim().toLowerCase();
                        String to = parts[1].trim().toLowerCase();
                        double time = Double.parseDouble(parts[2].trim());

                        String lineColour = currentLineColour;

                        if (parts.length >= 4) {
                            lineColour = parts[3].trim().toLowerCase();
                        }

                        stations.add(from);
                        stations.add(to);

                        graph.putIfAbsent(from, new ArrayList<Edge>());
                        graph.putIfAbsent(to, new ArrayList<Edge>());

                        graph.get(from).add(new Edge(to, time, lineColour));
                        graph.get(to).add(new Edge(from, time, lineColour));

                    } catch (NumberFormatException e) {
                        // Skips header rows
                    }
                }
            }

            fileReader.close();

            Scanner names = new Scanner(System.in);

            System.out.println("Enter start station:");
            String startStation = names.nextLine().trim().toLowerCase();

            while (!stations.contains(startStation)) {
                System.out.println("Invalid start station. Please enter again:");
                startStation = names.nextLine().trim().toLowerCase();
            }

            System.out.println("Enter end station:");
            String endStation = names.nextLine().trim().toLowerCase();

            while (!stations.contains(endStation) || startStation.equals(endStation)) {
                if (!stations.contains(endStation)) {
                    System.out.println("Invalid end station. Please enter again:");
                } else {
                    System.out.println("Start and end station cannot be the same. Please enter again:");
                }

                endStation = names.nextLine().trim().toLowerCase();
            }

            System.out.println("\nChoose journey option:");
            System.out.println("1 = Shortest time");
            System.out.println("2 = Fewest changes");

            String choice = names.nextLine().trim();

            while (!choice.equals("1") && !choice.equals("2")) {
                System.out.println("Invalid choice. Enter 1 for shortest time or 2 for fewest changes:");
                choice = names.nextLine().trim();
            }

            String mode;
            String title;

            if (choice.equals("1")) {
                mode = "time";
                title = "*** Route with Shortest Time ***";
            } else {
                mode = "changes";
                title = "*** Route with Fewest Changes ***";
            }

            RouteResult result = findRoute(graph, startStation, endStation, mode);

            printRoute(result, title);

            names.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }

    public static RouteResult findRoute(Map<String, List<Edge>> graph, String start, String end, String mode) {

        PriorityQueue<Node> queue = new PriorityQueue<Node>();
        Map<String, Double> bestTime = new HashMap<String, Double>();
        Map<String, Integer> bestChanges = new HashMap<String, Integer>();
        Map<String, String> previous = new HashMap<String, String>();

        String startKey = start + "|";
        bestTime.put(startKey, 0.0);
        bestChanges.put(startKey, 0);

        queue.add(new Node(start, "", 0.0, 0, mode));

        String bestEndKey = null;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            String currentKey = current.station + "|" + current.line;

            if (mode.equals("time")) {
                if (current.time > bestTime.get(currentKey)) {
                    continue;
                }
            } else {
                if (current.changes > bestChanges.get(currentKey)) {
                    continue;
                }
            }

            if (current.station.equals(end)) {
                bestEndKey = currentKey;
                break;
            }

            for (Edge edge : graph.get(current.station)) {
                double newTime = current.time + edge.time;
                int newChanges = current.changes;

                if (!current.line.equals("") && !current.line.equals(edge.line)) {
                    newTime = newTime + 2.0;
                    newChanges++;
                }

                String nextKey = edge.to + "|" + edge.line;

                boolean shouldUpdate = false;

                if (!bestTime.containsKey(nextKey)) {
                    shouldUpdate = true;
                } else if (mode.equals("time") && newTime < bestTime.get(nextKey)) {
                    shouldUpdate = true;
                } else if (mode.equals("changes")) {
                    if (newChanges < bestChanges.get(nextKey)) {
                        shouldUpdate = true;
                    } else if (newChanges == bestChanges.get(nextKey) && newTime < bestTime.get(nextKey)) {
                        shouldUpdate = true;
                    }
                }

                if (shouldUpdate) {
                    bestTime.put(nextKey, newTime);
                    bestChanges.put(nextKey, newChanges);
                    previous.put(nextKey, currentKey);
                    queue.add(new Node(edge.to, edge.line, newTime, newChanges, mode));
                }
            }
        }

        if (bestEndKey == null) {
            return new RouteResult(new ArrayList<String>(), 0.0, 0);
        }

        List<String> routeKeys = new ArrayList<String>();
        String currentKey = bestEndKey;

        while (currentKey != null) {
            routeKeys.add(currentKey);
            currentKey = previous.get(currentKey);
        }

        Collections.reverse(routeKeys);

        return new RouteResult(routeKeys, bestTime.get(bestEndKey), bestChanges.get(bestEndKey));
    }

    public static void printRoute(RouteResult result, String title) {

        if (result.routeKeys.isEmpty()) {
            System.out.println("No route found.");
            return;
        }

        System.out.println("\n" + title);

        if (result.routeKeys.size() > 1) {
            String firstLine = result.routeKeys.get(1).split("\\|")[1];
            String startStation = result.routeKeys.get(0).split("\\|")[0];

            System.out.println(startStation + " on " + firstLine + " line");

            String currentLine = firstLine;

            for (int i = 1; i < result.routeKeys.size(); i++) {
                String[] currentParts = result.routeKeys.get(i).split("\\|");
                String station = currentParts[0];
                String line = currentParts[1];

                if (!line.equals(currentLine)) {
                    String previousStation = result.routeKeys.get(i - 1).split("\\|")[0];

                    System.out.println("** Change Line to " + line + " line ***");
                    System.out.println(previousStation + " on " + line + " line");

                    currentLine = line;
                }

                System.out.println(station + " on " + line + " line");
            }
        }

        System.out.println("Time (mins): " + result.time);
        System.out.println("Total Changes: " + result.changes);
    }
}