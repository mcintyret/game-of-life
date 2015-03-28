package com.mcintyre.gameoflife.game;

import com.mcintyre.gameoflife.gui.Gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Game {
    public static final int GRID_HEIGHT = 80;
    public static final int GRID_WIDTH = 120;

    private final List<RowCalculator> jobList = new LinkedList<>();

    private int sleepInterval = 200;

    private boolean[][] grid = new boolean[GRID_HEIGHT][GRID_WIDTH];
    private boolean[][] gridSnapshot;

    private boolean hasStarted = false;
    private boolean stopRequested = false;
    private boolean wrap = false;
    private final Gui gui;

    private ExecutorService ex = Executors.newCachedThreadPool();

    public boolean[][] getGrid() {
        return grid;
    }

    public void setGrid(boolean[][] grid) {
        this.grid = grid;
    }

    public Game() {
        gui = new Gui(this);
        gui.initialize();
    }

    public void toggleGridAt(int row, int col) {
        grid[row][col] = !grid[row][col];
    }

    public boolean getGridAt(int row, int col) {
        return grid[row][col];
    }

    public void clear() {
        grid = new boolean[GRID_HEIGHT][GRID_WIDTH];
    }

    public void reset() {
        grid = gridSnapshot;
    }

    private void print(boolean[][] grid) {
        for (boolean[] row : grid) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println("\n");
    }

    public void start() {
        System.out.println("STARTING");
        gridSnapshot = snapShot(grid);
        stopRequested = false;
        hasStarted = true;
        while (doGeneration()) {
            ex.execute(gui::updateOnGeneration);
            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (stopRequested) break;
        }
        if (stopRequested) {
            System.out.println("STOPPED - stop requested");
        } else {
            System.out.println("STOPPED - stabilized");
        }
        hasStarted = false;
    }

    private boolean[][] snapShot(boolean[][] input) {
        if (input == null || input.length == 0) return null;
        boolean[][] output = new boolean[input.length][input[0].length];
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                output[i][j] = input[i][j];
            }
        }
        return output;
    }

    private boolean doGeneration() {
        jobList.clear();
        boolean hasChanged = false;
        boolean[][] newGrid = new boolean[GRID_HEIGHT][GRID_WIDTH];

        for (int i = 0; i < grid.length; i++) {
            jobList.add(new RowCalculator(i, newGrid));
        }

        try {
            for (Future<Boolean> future : ex.invokeAll(jobList)) {
                hasChanged |= future.get();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }

        grid = newGrid;
        return hasChanged;
    }

    private int getNum(int row, int col) {
        int total = 0;
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i == row && j == col) continue;
                try {
                    if (grid[i][j]) total++;
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (!wrap) continue;
                    int i2 = i;
                    int j2 = j;
                    if (i == -1) i2 = GRID_HEIGHT - 1;
                    else if (i == GRID_HEIGHT) i2 = 0;
                    if (j == -1) j2 = GRID_WIDTH - 1;
                    else if (j == GRID_WIDTH) j2 = 0;
                    if (grid[i2][j2]) total++;
                }
            }
        }
        return total;

    }

    public boolean hasStarted() {
        return hasStarted;
    }

    public void requestStop() {
        stopRequested = true;
    }

    public synchronized void setSleepInterval(int sleepInterval) {
        this.sleepInterval = sleepInterval;
    }

    public class RowCalculator implements Callable<Boolean> {

        private final int row;
        private final boolean[][] newGrid;

        public RowCalculator(int row, boolean[][] newGrid) {
            this.row = row;
            this.newGrid = newGrid;
        }

        @Override
        public Boolean call() {
            boolean hasChanged = false;
            for (int col = 0; col < grid[row].length; col++) {
                int num = getNum(row, col);
                switch (num) {
                    case 2:
                        newGrid[row][col] = grid[row][col];
                        break;
                    case 3:
                        newGrid[row][col] = true;
                        break;
                    default:
                        newGrid[row][col] = false;
                }
                boolean changed = newGrid[row][col] ^ grid[row][col];
                if (changed) gui.toggleSquareAt(row, col);
                hasChanged |= changed;
            }
            return hasChanged;
        }

    }

    public void toggleWrap() {
        wrap = !wrap;
    }

}
