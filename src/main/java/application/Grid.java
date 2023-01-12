package application;

import datatypes.HashSetIntArray;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public class Grid {
    public final int ROWS = 9, COLS = 9;
    private final int WALLS_TO_REMOVE = 5;
    private final boolean[][] visited = new boolean[ROWS][COLS];
    public ArrayList<Rectangle> horizontalWalls = new ArrayList<>();
    public ArrayList<Rectangle> verticalWalls = new ArrayList<>();

    // iff two squares are connected, there is no wall between them
    public HashSetIntArray connectedSquares = new HashSetIntArray(); // {row1, col1, row2, col2}

    public Grid(Pane gamePane) {
        dfsBacktracker(0, 0);
        removeSomeWalls();
        generateWallRectangles(gamePane);
    }

    public Grid(Pane gamePane, HashSetIntArray connectedSquares) {
        this.connectedSquares = connectedSquares;
        generateWallRectangles(gamePane);
    }

    private void dfsBacktracker(int curRow, int curCol) {
        visited[curRow][curCol] = true;
        Integer[] curSquare = {curRow, curCol};

        int[][] dir = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        Collections.shuffle(Arrays.asList(dir)); // randomize directions

        for (int i = 0; i < 4; i++) {
            int newRow = curRow + dir[i][0];
            int newCol = curCol + dir[i][1];

            if (inBounds(newRow, newCol) && !visited[newRow][newCol]) {
                Integer[] newConnection = ArrayUtils.addAll(curSquare, newRow, newCol);
                connectedSquares.add(newConnection);
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
            Integer[] curSquare = {curRow, curCol};

            int[][] dir = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            Collections.shuffle(Arrays.asList(dir));

            // for the first direction where squares are not connected: connect them
            for (int i = 0; i < 4; i++) {
                int newRow = curRow + dir[i][0];
                int newCol = curCol + dir[i][1];
                Integer[] newSquare = {newRow, newCol};

                if (inBounds(newRow, newCol) && notConnected(curSquare, newSquare)) {
                    Integer[] newConnection = ArrayUtils.addAll(curSquare, newSquare);
                    connectedSquares.add(newConnection);
                    removed++;
                    break;
                }
            }
        }
    }

    public boolean notConnected(Integer[] p1, Integer[] p2) {
        Integer[] conn1 = ArrayUtils.addAll(p1, p2);
        Integer[] conn2 = ArrayUtils.addAll(p2, p1);

        for (Integer[] element : connectedSquares) {
            if (Arrays.equals(element, conn1) || Arrays.equals(element, conn2))
                return false;
        }

        return true;
    }

    private void generateWallRectangles(Pane gamePane) {
        double paneWidth = gamePane.getWidth();
        double paneHeight = gamePane.getHeight();
        double wallThickness = 2.0;
        double wallWidth = paneWidth / COLS + wallThickness;
        double wallHeight = paneHeight / ROWS + wallThickness;

        // inner walls
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Integer[] p1, p2;

                // add wall below
                if (row < ROWS - 1) {
                    p1 = new Integer[]{row, col};
                    p2 = new Integer[]{row + 1, col};

                    if (notConnected(p1, p2)) {
                        double x = paneWidth * col / COLS;
                        double y = paneHeight * (row + 1) / ROWS;
                        addInnerWall(new Rectangle(x, y, wallWidth, wallThickness), "horizontal");
                    }
                }

                // add wall to the right
                if (col < COLS - 1) {
                    p1 = new Integer[]{row, col};
                    p2 = new Integer[]{row, col + 1};

                    if (notConnected(p1, p2)) {
                        double x = paneWidth * (col + 1) / COLS;
                        double y = paneHeight * row / ROWS;
                        addInnerWall(new Rectangle(x, y, wallThickness, wallHeight), "vertical");
                    }
                }
            }
        }

        // outer walls
        horizontalWalls.add(new Rectangle(0, 0, paneWidth, wallThickness));
        horizontalWalls.add(new Rectangle(0, paneHeight, paneWidth + wallThickness, wallThickness));
        verticalWalls.add(new Rectangle(0, 0, wallThickness, paneHeight));
        verticalWalls.add(new Rectangle(paneWidth, 0, wallThickness, paneHeight + wallThickness));
    }

    private void addInnerWall(Rectangle wall, String orientation) {
        wall.setFill(Color.DIMGRAY);

        if (orientation.equals("horizontal"))
            horizontalWalls.add(wall);
        else
            verticalWalls.add(wall);
    }
}
