package application;

import javafx.scene.Scene;
import javafx.util.Pair;

import java.util.*;

public class Grid {
    public final int ROWS = 9, COLS = 9;
    private final int WALLS_TO_REMOVE = 5;
    private final boolean[][] visited = new boolean[ROWS][COLS];

    // iff two squares are connected, there is no wall between them
    public HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> connected = new HashSet<>();

    public Grid(Scene gameScene) {
        generateGrid();
    }

    private void generateGrid() {
        dfsBacktracker(0, 0);
        removeSomeWalls();
    }

    private void dfsBacktracker(int curRow, int curCol) {
        visited[curRow][curCol] = true;
        var curSquare = new Pair<>(curRow, curCol);

        int[][] dir = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        Collections.shuffle(Arrays.asList(dir)); // randomize directions

        for (int i = 0; i < 4; i++) {
            int newRow = curRow + dir[i][0];
            int newCol = curCol + dir[i][1];

            if (inBounds(newRow, newCol) && !visited[newRow][newCol]) {
                connected.add(new Pair<>(curSquare, new Pair<>(newRow, newCol)));
                dfsBacktracker(newRow, newCol);
            }
        }


    }

    private boolean inBounds(int row, int col) {
        return 0 <= row && row < ROWS && 0 <= col && col < COLS;
    }

    private void removeSomeWalls() {
        Random random = new Random();
        int removed = 0;

        while (removed < WALLS_TO_REMOVE) {
            // choose random square and random direction array
            int curRow = random.nextInt(ROWS);
            int curCol = random.nextInt(COLS);
            var curSquare = new Pair<>(curRow, curCol);

            int[][] dir = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            Collections.shuffle(Arrays.asList(dir));

            // for the first direction where squares are not connected: connect them
            for (int i = 0; i < 4; i++) {
                int newRow = curRow + dir[i][0];
                int newCol = curCol + dir[i][1];
                var newSquare = new Pair<>(newRow, newCol);

                if (inBounds(newRow, newCol) && notConnected(curSquare, newSquare)) {
                    connected.add(new Pair<>(curSquare, newSquare));
                    removed++;
                    break;
                }
            }
        }
    }

    public boolean notConnected(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
        return !connected.contains(new Pair<>(p1, p2)) && !connected.contains(new Pair<>(p2, p1));
    }
}
