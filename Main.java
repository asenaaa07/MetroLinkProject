package Package;

//java libraries
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

//where the program runs
public class Main {

    //shows the two stations have a cannection between 
    static class Edge {

        //stores the values that have been given
        String to;
        double time;
        String line;

        //Edge is a constructor.
        // A constructor name has to be same with class name.
        Edge(String to, double time, String line) {

            //"this" in java means that it saves the values inside the object.
            //yani yukarda olana yaziyorlar programda asagiya kaydediyor
            this.to = to;
            this.time = time;
            this.line = line;
        }
    }

    static class MetroJourney implements Comparable<MetroJourney> {
        String station;
        String line;
        double time;
        int changes;
        String mode;

        //MetroJourney is an object
        MetroJourney(String station, String line, double time, int changes, String mode) {

            //Keeps the input the user wrote inside the MetroJourney aka object
            this.station = station;
            this.line = line;
            this.time = time;
            this.changes = changes;
            this.mode = mode;
        }

        public int compareTo(MetroJourney other) {
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

    //object that keeps the last answer
    static class FinalDirection {
        List<String> routeKeys;
        double time;
        int changes;

        FinalDirection(List<String> routeKeys, double time, int changes) {
            this.routeKeys = routeKeys;
            this.time = time;
            this.changes = changes;
        }
    }

    public static void main(String[] args) {

        //Keeps all the station names and does not allow duplicates
        Set<String> stations = new HashSet<String>();

        //Creats the graph
        Map<String, List<Edge>> graph = new HashMap<String, List<Edge>>();

        //using try because the file i put in might not open
        try {

            //opens up the file i need to use
            Scanner fileReader = new Scanner(new File("Metrolink_times_linecolour.csv"));

            String currentLineColour = "unknown";

            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine().trim();

                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");


                //this part is saying if there is one line in the folder it is a colour 
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

            //input
            Scanner names = new Scanner(System.in);

            System.out.println("Enter start station:");
            String startStation = names.nextLine().trim().toLowerCase();

            while (!stations.contains(startStation)) {
                System.out.println("Invalid start station. Please enter again:");
                startStation = names.nextLine().trim().toLowerCase();
            }

            System.out.println("Enter end station:");
            String endStation = names.nextLine().trim().toLowerCase();

            //if the end station is enterd wrong  or they enter start station to end
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

            //registers what the user picked
            String choice = names.nextLine().trim();

            //if they enter numbers that are not 1 or 2 the sytem will ask again
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

            //uses the findRoute method
            FinalDirection result = findRoute(graph, startStation, endStation, mode);

            showRoute(result, title);

            names.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }

    public static FinalDirection findRoute(Map<String, List<Edge>> graph, String start, String end, String mode) {

        //will scan through then will find the best journey
        PriorityQueue<MetroJourney> queue = new PriorityQueue<MetroJourney>();
        Map<String, Double> bestTime = new HashMap<String, Double>();
        Map<String, Integer> bestChanges = new HashMap<String, Integer>();
        Map<String, String> previous = new HashMap<String, String>();

        String startKey = start + "|";
        bestTime.put(startKey, 0.0);
        bestChanges.put(startKey, 0);

        queue.add(new MetroJourney(start, "", 0.0, 0, mode));

        String bestEndKey = null;

        while (!queue.isEmpty()) {
            MetroJourney current = queue.poll();
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

            //checks all the stations that are connected to given input
            for (Edge edge : graph.get(current.station)) {
                double newTime = current.time + edge.time;
                int newChanges = current.changes;

                //if there is already a line and the edge uses a different line we do the maths
                if (!current.line.equals("") && !current.line.equals(edge.line)) {
                    newTime = newTime + 2.0;
                    newChanges++;
                }

                String nextKey = edge.to + "|" + edge.line;

                //helps decide if the new way is better or not
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
                    queue.add(new MetroJourney(edge.to, edge.line, newTime, newChanges, mode));
                }
            }
        }

        if (bestEndKey == null) {
            return new FinalDirection(new ArrayList<String>(), 0.0, 0);
        }

        List<String> routeKeys = new ArrayList<String>();
        String currentKey = bestEndKey;

        while (currentKey != null) {
            routeKeys.add(currentKey);
            currentKey = previous.get(currentKey);
        }

        Collections.reverse(routeKeys);

        return new FinalDirection(routeKeys, bestTime.get(bestEndKey), bestChanges.get(bestEndKey));
    }

    public static void showRoute(FinalDirection result, String title) {

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

                //gets station name and the colour of it
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