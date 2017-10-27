// This java program implements the branch and bound
// algorithm for TSP problem.
//
import java.util.*;
import java.io.*;

public class assignmentP1 {
  public static void main(String[] args) {
    // the input and output must be provided
    if (args.length != 2) {
      System.out.println("Usage: java TSP <input> <output>");
      return;
    }
    // open the file and solve the TSP for each case
    try {
      int k = 0;
      Scanner in = new Scanner(new File(args[0]));
      PrintStream out = new PrintStream(new FileOutputStream(args[1]));
      while (in.hasNextInt()) {
        k ++; // problem number
        assignmentP1 tsp = new assignmentP1(in, k); // read the city distances
        if (tsp.dist.length > 0) {
          State state = tsp.run();
          // display the state to the output file
          System.out.println(state);
          out.println(state);
        }
      }
      in.close();
      out.close();
    } catch (IOException e) {
      System.out.println("Could not read file " + args[0]);
      System.exit(0);
    }
  }

  private int problem;  // the problem number
  private int[][] dist; // the distance matrix
  private int count;    // total number of nodes in the queue
  private long duration; // running time

  public assignmentP1(Scanner in, int problem) {
    this.problem = problem;
    // read the distances matrix from the input file
    int n = in.nextInt(); // number of cities
    dist = new int[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        // read source/destin and distance of two cities
        int source = in.nextInt();
        int destin = in.nextInt();
        int value = in.nextInt();
        dist[source][destin] = value;
      }
    }
  }

  /**
   * Use branch and bound algorithm to solve the TSP.
   * The State class is used to store the state during
   * the traversal of the graph.
   */
  public State run() {
    long start = System.currentTimeMillis();

    // use a priority queue to store the value
    PriorityQueue<State> q = new PriorityQueue<>();
    int minLength = Integer.MAX_VALUE;
    State minState = null;

    // create the path with only first city (0) as the start
    // state, and add into the priority queue
    int[] pending = new int[dist.length]; // construct the pending city
    for (int i = 0; i < pending.length; i++) {
      pending[i] = i;
    }
    q.add(new State(new int[0], 0, pending));
    count ++;

    // now branch and bound
    while (!q.isEmpty()) {
      State curr = q.remove();
      if (curr.done()) { // already has the full tour
        if (curr.value < minLength) {
          // the current node has a smaller length
          // update.
          minState = curr;
          minLength = curr.value;
        }
      } else {
        // expand from the current node
        for (int next : curr.pending) {
          State newState = new State(curr.visited, next, curr.pending);
          if (newState.bound < minLength) {
            // the new state has the potential to have shorter length
            // add into the queue
            q.add(newState);
            count ++;
          }
        }
      }
    }
    // calculate duration
    duration = System.currentTimeMillis() - start;
    return minState;
  }


  private class State implements Comparable<State> {
    public final int bound; // the bound of the state
    public final int value; // the total distance if this state has all cities (a tour)

    public final int[] visited; // cities already in the tour
    public final int[] pending; // cities that still pending to visit

    // expand the given visited cities with the next city
    // for the next city of the tour.
    public State(int[] arr, int next, int[] more) {
      if (more.length == 1) {
        // next is the last city to visit, add the first city
        // into it
        this.visited = Arrays.copyOf(arr, arr.length + 2);
        this.visited[arr.length] = next;
        this.visited[arr.length + 1] = arr[0];
        this.pending = new int[0];
        // calculate the distance
        value = calculateDist();
        bound = value;
      } else {
        this.visited = Arrays.copyOf(arr, arr.length + 1);
        this.visited[arr.length] = next;
        // calculate the city pending to be visited
        this.pending = new int[more.length - 1];
        int k = 0;
        for (int i = 0; i < more.length; i++) {
          if (more[i] != next) {
            this.pending[k++] = more[i];
          }
        }
        // calculate the bound for the current state
        bound = calculateBound();
        value = bound;
      }
    }

    // whether this state has the full tour
    public boolean done() {
      return pending.length == 0;
    }

    // calculate the distance of the tour.
    private int calculateDist() {
      int sum = 0;
      for (int i = 0; i < visited.length; i++) {
        int j = (i + 1) % visited.length;
        sum += dist[visited[i]][visited[j]];
      }
      return sum;
    }

    // calculate the bound of the current state
    private int calculateBound() {
      int sum = 0;
      // accumulate the total distance in the visited cities
      for (int i = 0; i < visited.length - 1; i++) {
        sum += dist[visited[i]][visited[i + 1]];
      }
      // then, accumulate the potential minimum
      // distance for pending cities
      sum += potential(visited[visited.length - 1]);
      for (int i = 0; i < pending.length; i++) {
        sum += potential(pending[i]);
      }
      return sum;
    }

    // calculate the minimal distance for the given
    // source to visit the next city potentially in the
    // tour. Visited city (except the first one) should
    // be excluded when calculate this.
    private int potential(int source) {
      int min = Integer.MAX_VALUE;
      if (source != visited[0]) {
        min = dist[source][visited[0]]; // to first city
      }
      for (int i = 0; i < pending.length; i++) {
        if (source != pending[i]) {
          if (dist[source][pending[i]] < min) {
            min = dist[source][pending[i]];
          }
        }
      }
      return min;
    }

    @Override
    public int compareTo(State that) {
      return this.value - that.value;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      // save the distance and value to the s tring
      builder.append("Problem: " + problem + ",  ");
      builder.append("Size: " + dist.length + ",  ");
      builder.append("MinCost: " + value + ",  ");
      builder.append("Number Nodes: " + count + ",  ");
      builder.append("Time: " + duration + "  milliseconds\n");
      return builder.toString();
    }
  }

}
