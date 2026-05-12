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
    //Edge is a object orianted class 
    static class Edge {

        //stores the values that have been given
        //object variables
        String to;
        double time;
        String line;

        //Edge is a constructor. Helps create objects
        // A constructor name has to be same with class name.
        Edge(String to, double time, String line) {

            //"this.to" in java means variable inside the object
            //"to" paramater coming into constructor
            this.to = to;
            this.time = time;
            this.line = line;
        }
    }

    //it is a object orianted class
    //"implements Camparable..." is an interface implementation for abstract data types
    static class MetroJourney implements Comparable<MetroJourney> {

        //instance variables belonging to MetroJourney object
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

        //this is a method. a function inside a class
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

    //class that keeps the last answer
    static class FinalDirection {

        //instance variables that belong to FinalDirection object
        List<String> routeKeys;
        double time;
        int changes;

        FinalDirection(List<String> routeKeys, double time, int changes) {
            this.routeKeys = routeKeys;
            this.time = time;
            this.changes = changes;
        }
    }

    //where the program starts running and this is a method
    //"String[] args" is a parameter
    public static void main(String[] args) {

        //Keeps all the station names and does not allow duplicates
        //"stations" is a variable. "Set<String>" is a interface type. "HashSet<String>()" is a object.
        Set<String> stations = new HashSet<String>();

        //Creats the graph structure
        //variable and a map collection
        Map<String, List<Edge>> graph = new HashMap<String, List<Edge>>();

        //using try because the file i put in might not open
        try {

            //opens up the file i need to use
            //object
            Scanner fileReader = new Scanner(new File("Metrolink_times_linecolour.csv"));

            String currentLineColour = "unknown";

            //loop that keeps on going if the condition is true
            //will keep on reading  the file line by line until it ends
            while (fileReader.hasNextLine()) {
                String line = fileReader.nextLine().trim();

                //conditional statment that makes decisions
                //checks for empty lines in the file and skips them
                if (line.isEmpty()) {
                    continue;
                }

                //array variable that stores multiple strings
                //"line.split(",")" is a method that belongs to string class
                String[] parts = line.split(",");


                //this part is saying if there is one line in the folder it is a colour 
                if (parts.length == 1) {
                    currentLineColour = parts[0].trim().toLowerCase();
                    continue;
                }

                //makes sure the file has enough lines before continuing
                if (parts.length >= 3) {
                    try {
                        String from = parts[0].trim().toLowerCase();
                        String to = parts[1].trim().toLowerCase();

                        //static method that turns text into number
                        double time = Double.parseDouble(parts[2].trim());

                        String lineColour = currentLineColour;

                        //nested if statment that checks if a line colour exist in the row
                        if (parts.length >= 4) {
                            lineColour = parts[3].trim().toLowerCase();
                        }

                        //a method thet adds an item into collection
                        stations.add(from);
                        stations.add(to);

                        //method that adds something only if it doesn't exist
                        graph.putIfAbsent(from, new ArrayList<Edge>());
                        graph.putIfAbsent(to, new ArrayList<Edge>());

                        //"new Edge" object creation that creates an Edge object
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

            //will keep on asking the user to enter a valid station
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

            //if the condition is true it will do the first block if not do the second block
            if (choice.equals("1")) {
                mode = "time";
                title = "*** Route with Shortest Time ***";
            } else {
                mode = "changes";
                title = "*** Route with Fewest Changes ***";
            }

            //calls the findRoute method
            FinalDirection result = findRoute(graph, startStation, endStation, mode);

            showRoute(result, title);

            names.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
    }

    public static FinalDirection findRoute(Map<String, List<Edge>> graph, String start, String end, String mode) {

        //will scan through then will find the best journey
        //and it is a collection class
        PriorityQueue<MetroJourney> queue = new PriorityQueue<MetroJourney>();
        Map<String, Double> bestTime = new HashMap<String, Double>();
        Map<String, Integer> bestChanges = new HashMap<String, Integer>();
        Map<String, String> previous = new HashMap<String, String>();

        String startKey = start + "|";
        bestTime.put(startKey, 0.0);
        bestChanges.put(startKey, 0);

        queue.add(new MetroJourney(start, "", 0.0, 0, mode));

        String bestEndKey = null;

        //repeatedly processes the best avalible journey from the priorty queue until the destination is found 
        //or the no routes remain
        while (!queue.isEmpty()) {

            //"queue.poll()" is a method the removes and runs the best value that is in the queue
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
            //and it is also a enhanced for loop. loops through all connected edges
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

                    //checks if the time is correct and if the route is faster than the previously stored route
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

        //static method that reverses the list order
        Collections.reverse(routeKeys);

        return new FinalDirection(routeKeys, bestTime.get(bestEndKey), bestChanges.get(bestEndKey));
    }

    //"ShowRoute" is a method call that calls the method that prints the route 
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

            //it iterates through the final route list and prints each station in order
            //it goes through the route list one station at a time
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
