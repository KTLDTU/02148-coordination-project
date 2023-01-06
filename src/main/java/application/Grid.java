package application;

import javafx.scene.Scene;
import javafx.util.Pair;

import java.util.*;

public class Grid {
    public final int ROWS = 9, COLS = 9;
    private final int WALLS_TO_REMOVE = 5;
    private boolean[][] visited = new boolean[ROWS][COLS];
    public HashSet<Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> connected = new HashSet<>();

    public Grid(Scene gameScene) {
        generateGrid();
    }

    private void generateGrid() {
        dfsBacktracker(0, 0);
        removeWalls();
    }

    private void removeWalls() {
        // TODO
    }

    private void dfsBacktracker(int curRow, int curCol) {
        visited[curRow][curCol] = true;
        var curP = new Pair<>(curRow, curCol);

        int[][] dir = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        Collections.shuffle(Arrays.asList(dir)); // randomize directions

        for (int i = 0; i < 4; i++) {
            int newRow = curRow + dir[i][0];
            int newCol = curCol + dir[i][1];

            if (inBounds(newRow, newCol) && !visited[newRow][newCol]) {
                connected.add(new Pair<>(curP, new Pair<>(newRow, newCol)));
                dfsBacktracker(newRow, newCol);
            }
        }


    }

    private boolean inBounds(int row, int col) {
        return 0 <= row && row < ROWS && 0 <= col && col < COLS;
    }
}
