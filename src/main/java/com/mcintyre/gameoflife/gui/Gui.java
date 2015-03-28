package com.mcintyre.gameoflife.gui;


import com.mcintyre.gameoflife.game.Game;
import com.mcintyre.gameoflife.tools.FileHandling;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Gui extends JFrame {
  private static final long serialVersionUID = 1L;
  
  public static final String savedGrids = FileHandling.rootFilePath + "savedGrids";
  private static final Border border = BorderFactory.createLineBorder(new Color(237, 237, 237), 1);

  private final JPanel mainPanel = new JPanel();
  private final JToolBar toolBar = new JToolBar();
  private final JButton startButton = new JButton("Start!");
  private final JButton stopButton = new JButton("Stop");
  private final JButton resetButton = new JButton("Reset");
  private final JButton clearButton = new JButton("Clear");
  private final JToggleButton wrapButton = new JToggleButton("Wrap");
  private final JSlider slider = new JSlider(0, 300, 50);
  private final JLabel generations = new JLabel("Generations: ");
  private final Counter generationsCounter = new Counter();
  private final JTextField savedNameField = new JTextField();
  private final JButton saveButton = new JButton("Save");
  private final JComboBox loadGrid = new JComboBox();

  private final Square[][] squares = new Square[Game.GRID_HEIGHT][Game.GRID_WIDTH];

  private Map<String, boolean[][]> savedGridsMap;

  private final ExecutorService ex = Executors.newFixedThreadPool(5);

  private final Game game;

  public Gui(Game game) {
    this.game = game;
  }

  public void initialize() {
    initializeMainPanel();
    initializeFrame();
    initializeToolBar();
    loadSavedGrids();
    updateLoadableGrids();
    launch();
    repaint();
  }

  private void updateLoadableGrids() {
    loadGrid.removeAllItems();
    for (String s : savedGridsMap.keySet()) {
      loadGrid.addItem(s);
    }
  }

  private void updateLoadableGrids(String defaultStr) {
    updateLoadableGrids();
    loadGrid.setSelectedItem(defaultStr);
  }

  @SuppressWarnings("unchecked")
  private void loadSavedGrids() {
    if (FileHandling.exists(savedGrids)) {
      savedGridsMap = (Map<String, boolean[][]>) FileHandling.Deserialize(savedGrids);
    } else {
      savedGridsMap = new HashMap<String, boolean[][]>();
    }
  }

  public void synchGuiToGameGrid() {
    for (Square[] row : squares) {
      for (Square square : row) {
        if (square.alive ^ game.getGridAt(square.row, square.col)) square.toggle();
      }
    }
  }

  private void initializeToolBar() {
    startButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        ex.execute(new Runnable() {
          @Override
          public void run() {
            game.start();
          }
        });
      }
    });
    toolBar.add(startButton);

    stopButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!game.hasStarted()) return;
        ex.execute(new Runnable() {
          @Override
          public void run() {
            game.requestStop();
          }
        });
      }
    });
    toolBar.add(stopButton);

    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        game.reset();
        resetGui();
      }
    });
    toolBar.add(resetButton);
    
    clearButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        game.clear();
        resetGui();
      }
    });
    toolBar.add(clearButton);

    wrapButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        ex.execute(new Runnable() {
          @Override
          public void run() {
           game.toggleWrap(); 
          }
        });
      }
    });
    toolBar.add(wrapButton);
    
    slider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        ex.execute(new Runnable() {
          @Override
          public void run() {
            game.setSleepInterval(slider.getValue());
          }
        });
      }
    });
    toolBar.add(slider);

    toolBar.add(generations);
    toolBar.add(generationsCounter);

    toolBar.add(savedNameField);

    saveButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        ex.execute(new Runnable() {
          @Override
          public void run() {
            synchronized (savedGridsMap) {
              String name = savedNameField.getText();
              savedGridsMap.put(name, game.getGrid());
              FileHandling.Serialize(savedGridsMap, savedGrids);
              updateLoadableGrids(name);
            }
          }
        });
      }
    });
    toolBar.add(saveButton);

    loadGrid.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ex.execute(new Runnable() {
          @Override
          public void run() {
            String gridName = (String) loadGrid.getSelectedItem();
            // This is here because clearing the comboBox triggers an action
            // event.
            // This then causes it to load a null grid, which causes
            // nullPointerExceptions in the resetGui method.
            if (gridName == null) return;
            game.requestStop();
            while (game.hasStarted())
              ; // Busy waiting - this is shitty!
            game.setGrid(savedGridsMap.get(gridName));
            resetGui();
          }
        });
      }
    });
    toolBar.add(loadGrid);
    getContentPane().add(toolBar, BorderLayout.NORTH);

  }

  private void initializeMainPanel() {
    mainPanel.setLayout(new GridLayout(Game.GRID_HEIGHT, Game.GRID_WIDTH));
    mainPanel.setOpaque(true);
    for (int row = 0; row < Game.GRID_HEIGHT; row++) {
      for (int col = 0; col < Game.GRID_WIDTH; col++) {
        Square square = new Square(row, col);
        squares[row][col] = square;
        mainPanel.add(square);
      }
    }
  }

  private void resetGui() {
    generationsCounter.reset();
    synchGuiToGameGrid();
  }

  private void initializeFrame() {
    setTitle("Game of Life");
    setBounds(0, 0, 1200, 900);
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(mainPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  private void launch() {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public void toggleSquareAt(int row, int col) {
    squares[row][col].toggle();
  }

  private class Square extends JPanel {
    private static final long serialVersionUID = 1L;
    
    private final int row;
    private final int col;

    private boolean alive = false;

    public Square(int row, int col) {
      this.row = row;
      this.col = col;
      setBorder(border);
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          if (game.hasStarted()) return;
          alive = !alive;
          game.toggleGridAt(Square.this.row, Square.this.col);
          repaint();
        }
      });
    }

    @Override
    public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g;

      setBackground(alive ? Color.BLUE : Color.WHITE);
      super.paintComponent(g2);
    }

    public void toggle() {
      alive = !alive;
      repaint();
    }

  }

  private static class Counter extends JLabel {
    private static final long serialVersionUID = 1L;
    
    private int count = 0;

    public Counter() {
      update();
    }

    public void increment() {
      count++;
      update();
    }

    public void reset() {
      count = 0;
      update();
    }

    public void update() {
      setText(Integer.toString(count));
    }

  }

  public void updateOnGeneration() {
    generationsCounter.increment();
  }

}
