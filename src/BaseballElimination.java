import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.FlowNetwork;
import java.util.HashMap;
import java.util.Map;

public class BaseballElimination {

  private final int teamCount;
  private final Map<String, Integer> teams;
  private final int[] wins;
  private final int[] losses;
  private final int[] remainingGames;
  private final int[][] games;

  public BaseballElimination(String filename) {
    In input = new In(filename);
    teamCount = input.readInt();
    teams = new HashMap<>();
    wins = new int[teamCount];
    losses = new int[teamCount];
    remainingGames = new int[teamCount];
    games = new int[teamCount][teamCount];
    for (int i = 0; i < teamCount; i++) {
      String teamName = input.readString();
      teams.put(teamName, i);
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
    int maxPossibleWins = wins(team) + remaining(team);
    for (String otherTeam : teams()) {
      if (maxPossibleWins < wins(otherTeam)) {
        return true;
      }
    }

    int teamId = teams.get(team);
    int gameCount = ((teamCount - 1) * (teamCount - 2)) / 2;
    int vertexCount = gameCount + teamCount + 1;

    int currentVertex = 0;
    Map<Integer, Integer> teamVertices = new HashMap<>();
    for (String teamName : teams.keySet()) {
      int currentTeamId = teams.get(teamName);
      if (currentTeamId == teamId) {
        continue;
      }
      teamVertices.put(currentTeamId, currentVertex++);
    }

    FlowNetwork flowNetwork = new FlowNetwork(vertexCount);
    int source = vertexCount - 1;
    int target = vertexCount - 2;
    for (int i = 0; i < teamCount; i++) {
      if (teamId == i) {
        continue;
      }
      for (int j = 0; j < teamCount; j++) {
        if (teamId == j || i == j) {
          continue;
        }
        flowNetwork.addEdge(new FlowEdge(source, currentVertex, games[i][j]));
        flowNetwork.addEdge(new FlowEdge(currentVertex, teamVertices.get(i), Double.POSITIVE_INFINITY));
        flowNetwork.addEdge(new FlowEdge(currentVertex, teamVertices.get(j), Double.POSITIVE_INFINITY));
        currentVertex++;
      }
    }

    return false;
  }

  public Iterable<String> certificateOfElimination(String team) {
    validateTeam(team);
    return null;
  }

  private void validateTeam(String team) {
    if (!teams.containsKey(team)) {
      throw new IllegalArgumentException();
    }
  }

  public static void main(String[] args) {
    BaseballElimination division = new BaseballElimination(args[0]);
    division.isEliminated("Detroit");
    for (String team : division.teams()) {

    }
  }
}
