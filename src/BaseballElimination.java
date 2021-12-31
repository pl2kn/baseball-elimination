import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.StdOut;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseballElimination {

  private final int teamCount;
  private final Map<String, Integer> teams = new HashMap<>();
  private final Map<Integer, String> teamIds = new HashMap<>();
  private final int[] wins;
  private final int[] losses;
  private final int[] remainingGames;
  private final int[][] games;
  private List<String> certificateOfElimination = new ArrayList<>();

  public BaseballElimination(String filename) {
    In input = new In(filename);
    teamCount = input.readInt();
    wins = new int[teamCount];
    losses = new int[teamCount];
    remainingGames = new int[teamCount];
    games = new int[teamCount][teamCount];
    for (int i = 0; i < teamCount; i++) {
      String teamName = input.readString();
      teams.put(teamName, i);
      teamIds.put(i, teamName);
      wins[i] = input.readInt();
      losses[i] = input.readInt();
      remainingGames[i] = input.readInt();
      for (int j = 0; j < teamCount; j++) {
        games[i][j] = input.readInt();
      }
    }
  }

  public int numberOfTeams() {
    return teamCount;
  }

  public Iterable<String> teams() {
    return teams.keySet();
  }

  public int wins(String team) {
    validateTeam(team);
    return wins[teams.get(team)];
  }

  public int losses(String team) {
    validateTeam(team);
    return losses[teams.get(team)];
  }

  public int remaining(String team) {
    validateTeam(team);
    return remainingGames[teams.get(team)];
  }

  public int against(String team1, String team2) {
    validateTeam(team1);
    validateTeam(team2);
    return games[teams.get(team1)][teams.get(team2)];
  }

  public boolean isEliminated(String team) {
    validateTeam(team);
    certificateOfElimination = new ArrayList<>();
    int maxPossibleWins = wins(team) + remaining(team);
    boolean isEliminated = false;
    for (String otherTeam : teams()) {
      if (maxPossibleWins < wins(otherTeam)) {
        certificateOfElimination.add(otherTeam);
        isEliminated = true;
      }
    }
    if (isEliminated) {
      return true;
    }
    int teamId = teams.get(team);
    int gameCount = ((teamCount - 1) * (teamCount - 2)) / 2;
    int currentVertex = 0;
    int source = currentVertex++;
    int target = currentVertex++;
    Map<Integer, Integer> teamVertices = new HashMap<>();
    for (String teamName : teams.keySet()) {
      int currentTeamId = teams.get(teamName);
      if (currentTeamId == teamId) {
        continue;
      }
      teamVertices.put(currentTeamId, currentVertex++);
    }
    FlowNetwork flowNetwork = new FlowNetwork(gameCount + teamCount + 1);
    for (int i = 0; i < teamCount; i++) {
      if (teamId == i) {
        continue;
      }
      for (int j = i + 1; j < teamCount; j++) {
        if (teamId == j) {
          continue;
        }
        flowNetwork.addEdge(new FlowEdge(source, currentVertex, games[i][j]));
        flowNetwork.addEdge(
            new FlowEdge(currentVertex, teamVertices.get(i), Double.POSITIVE_INFINITY));
        flowNetwork.addEdge(
            new FlowEdge(currentVertex, teamVertices.get(j), Double.POSITIVE_INFINITY));
        currentVertex++;
      }
    }
    for (int i = 0; i < teamCount; i++) {
      if (teamId == i) {
        continue;
      }
      flowNetwork.addEdge(new FlowEdge(teamVertices.get(i), target,
          Math.max(0, wins[teamId] + remainingGames[teamId] - wins[i])));
    }
    FordFulkerson fordFulkerson = new FordFulkerson(flowNetwork, source, target);
    for (FlowEdge edge : flowNetwork.adj(source)) {
      if (edge.flow() != edge.capacity()) {
        isEliminated = true;
        break;
      }
    }
    if (isEliminated) {
      for (int i = 0; i < teamCount; i++) {
        if (teamId == i) {
          continue;
        }
        if (fordFulkerson.inCut(teamVertices.get(i))) {
          certificateOfElimination.add(teamIds.get(i));
        }
      }
      return true;
    }
    return false;
  }

  public Iterable<String> certificateOfElimination(String team) {
    if (isEliminated(team)) {
      return certificateOfElimination;
    }
    return null;
  }

  private void validateTeam(String team) {
    if (!teams.containsKey(team)) {
      throw new IllegalArgumentException();
    }
  }

  public static void main(String[] args) {
    BaseballElimination division = new BaseballElimination(args[0]);
    for (String team : division.teams()) {
      if (division.isEliminated(team)) {
        StdOut.print(team + " is eliminated by the subset R = { ");
        for (String t : division.certificateOfElimination(team)) {
          StdOut.print(t + " ");
        }
        StdOut.println("}");
      } else {
        StdOut.println(team + " is not eliminated");
      }
    }
  }
}
