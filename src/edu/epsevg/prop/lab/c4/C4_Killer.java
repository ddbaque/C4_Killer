package edu.epsevg.prop.lab.c4;

import java.util.ArrayList;

/**
 * C4_Killer is a class that implements the "Jugador" and "IAuto" interfaces. It represents a
 * Connect 4 AI player that makes moves using a minimax algorithm with alpha-beta pruning. The AI
 * also records the time and selected column for each move during the game.
 */
public class C4_Killer implements Jugador, IAuto {

  // Matrix of weights used to evaluate the positions on the Connect 4 board.
  private int[][] weights = {
    {6, 8, 10, 14, 14, 10, 8, 6},
    {8, 12, 16, 22, 22, 16, 12, 8},
    {10, 16, 22, 26, 26, 22, 16, 10},
    {10, 16, 22, 26, 26, 22, 16, 10},
    {10, 16, 22, 26, 26, 22, 16, 10},
    {10, 16, 22, 26, 26, 22, 16, 10},
    {8, 12, 16, 14, 14, 16, 12, 8},
    {6, 8, 10, 14, 14, 10, 8, 6}
  };

  private ArrayList<Integer> selectedMoves =
      new ArrayList<>(); // List to store selected columns for each move.
  private ArrayList<Double> movementTimes = new ArrayList<>(); // List to store time for each move.

  private static int INFINITE =
      Integer.MAX_VALUE; // Constant for the infinite value (used in minimax).
  private static int MINUS_INFINITE =
      Integer.MIN_VALUE; // Constant for negative infinity (used in minimax).

  private int boardsExplored;
  private int gameBoards;
  private boolean isPruning; // Boolean to check if alpha-beta pruning is enabled.
  private int depth; // The initial depth for the minimax algorithm.
  private String name; // Name of the AI player.

  /**
   * Constructor to initialize the C4_Killer object.
   *
   * @param depth The initial depth of the minimax algorithm.
   * @param isPruning Boolean indicating whether alpha-beta pruning is used.
   * @throws Exception If an error occurs during initialization.
   */
  public C4_Killer(int depth, boolean isPruning) throws Exception {
    this.name = "C4_Killer"; // Name of the player.
    this.boardsExplored = 0;
    this.gameBoards = 0;
    this.isPruning = isPruning; // Set whether pruning is enabled.
    this.depth = depth; // Set the initial depth for the search.
  }

  @Override
  public String nom() {
    return name; // Return the name of the AI player.
  }

  /**
   * This method calculates the best move for the AI using the minimax algorithm with alpha-beta
   * pruning. It iterates through all possible moves and evaluates them.
   *
   * @param t The current board state.
   * @param color The color of the AI player (either 1 or -1).
   * @return The selected column for the next move.
   */
  @Override
  public int moviment(Tauler t, int color) {
    boardsExplored = 0; // Reset the number of explored boards.
    long startTime = System.nanoTime(); // Start the timer with nanoTime for better precision.

    int bestColumn = -1,
        bestValue = MINUS_INFINITE; // Initial best column and value (negative infinity).

    // Loop through all columns to find the best move.
    for (int movement = 0; movement < t.getMida(); ++movement) {
      if (t.movpossible(movement)) { // Check if the move is possible in the current column.
        Tauler auxiliarBoard = new Tauler(t); // Create a new board for the simulation.
        auxiliarBoard.afegeix(movement, color); // Simulate the move.

        MinimaxContext context =
            new MinimaxContext(auxiliarBoard, depth - 1, movement, color, MINUS_INFINITE, INFINITE);
        int value = minimaxMinMovement(context); // Pass the context instead of the six parameters.

        // If the move is better than the previous one, update the best column and value.
        if (value > bestValue) {
          bestValue = value;
          bestColumn = movement;
        }
      }
    }

    long endTime = System.nanoTime(); // End the timer for the move.
    double tiempoMovimiento =
        (endTime - startTime) / 1_000_000_000.0; // Convert nanoseconds to seconds.

    // Store the time and the selected column for the current move.
    movementTimes.add(tiempoMovimiento);
    selectedMoves.add(bestColumn);

    showStatistics(); // Display the table of move times after the move.

    return bestColumn; // Return the best column to be played.
  }

  /**
   * This method checks the last color placed in a specific column.
   *
   * @param t The current board state.
   * @param column The column to check.
   * @return The color of the last piece in the specified column.
   */
  private int getTopPiece(Tauler t, int column) {
    for (int i = t.getMida() - 1; i >= 0; --i)
      if (t.getColor(i, column) != 0) return t.getColor(i, column);
    return 0;
  }

  /** Displays a table of the move times and selected columns after the move. */
  public void showStatistics() {
    // Print the table header.
    System.out.println("\n================= Table of Statistics =================");
    System.out.printf("%-10s | %-17s | %-20s\n", "Move", "Column", "Time (seconds)");
    System.out.println("-------------------------------------------------------"); // Line divider

    // Print each move's data in the table.
    for (int i = 0; i < movementTimes.size(); i++) {
      System.out.printf(
          "%-10d | %-17d | %-20.4f\n",
          i + 1,
          selectedMoves.get(i), // Selected column
          movementTimes.get(i)); // Time in seconds
    }
  }

  /**
   * Maximizing function for the minimax algorithm. It tries to maximize the AI's score.
   *
   * @param context The current context containing the board, depth, color, and alpha-beta bounds.
   * @return The best value for the AI's move.
   */
  private int minimaxMaxMovement(MinimaxContext context) {
    int bestValue = this.MINUS_INFINITE, c = getTopPiece(context.t, context.column);
    if (context.t.solucio(context.column, c) || context.depth == 0 || !context.t.espotmoure()) {
      return context.t.solucio(context.column, c)
          ? bestValue
          : calculateConsecutiveHeuristic(context.t, context.actualColumn);
    }

    for (int movement = 0; movement < context.t.getMida(); ++movement) {
      if (context.t.movpossible(movement)) {
        Tauler auxiliarBoard = new Tauler(context.t);
        auxiliarBoard.afegeix(movement, context.actualColumn);
        bestValue =
            Math.max(
                bestValue,
                minimaxMinMovement(
                    new MinimaxContext(
                        auxiliarBoard,
                        context.depth - 1,
                        movement,
                        context.actualColumn,
                        context.alpha,
                        context.beta)));
        if (isPruning && bestValue >= context.beta) break;
        context.alpha = Math.max(bestValue, context.alpha);
      }
    }
    return bestValue;
  }

  /**
   * Minimizing function for the minimax algorithm. It tries to minimize the opponent's score.
   *
   * @param context The current context containing the board, depth, color, and alpha-beta bounds.
   * @return The best value for the opponent's move.
   */
  private int minimaxMinMovement(MinimaxContext context) {
    int c = getTopPiece(context.t, context.column);
    boardsExplored++;
    gameBoards++;
    int bestValue = this.INFINITE;

    if (context.t.solucio(context.column, c) || context.depth == 0 || !context.t.espotmoure()) {
      return context.t.solucio(context.column, c)
          ? bestValue
          : calculateConsecutiveHeuristic(context.t, context.actualColumn);
    }

    for (int movement = 0; movement < context.t.getMida(); ++movement) {
      if (context.t.movpossible(movement)) {
        Tauler auxiliarBoard = new Tauler(context.t);
        auxiliarBoard.afegeix(movement, -context.actualColumn);
        bestValue =
            Math.min(
                bestValue,
                minimaxMaxMovement(
                    new MinimaxContext(
                        auxiliarBoard,
                        context.depth - 1,
                        movement,
                        context.actualColumn,
                        context.alpha,
                        context.beta)));
        if (isPruning && bestValue <= context.alpha) break;
        context.beta = Math.min(bestValue, context.beta);
      }
    }
    return bestValue;
  }

  /**
   * Heuristic evaluation function for the current board state based on consecutive pieces.
   *
   * @param t The current board state.
   * @param color The color of the player being evaluated.
   * @return The heuristic value based on consecutive pieces.
   */
  public int calculateConsecutiveHeuristic(Tauler t, int color) {
    int h = 0, size = t.getMida();
    for (int c = 0; c < size; ++c) {
      for (int f = 0; f < size; ++f) {
        int col = t.getColor(f, c);
        if (col == 0) continue;
        int signe = (col == color) ? 1 : -1;
        for (int i = 0; i < 4; ++i) {
          if (f + i < size && c + i < size) {
            int piece = t.getColor(f + i, c + i);
            h += (piece == color) ? signe : (piece == 0 ? signe * 10 : 0);
          }
        }
      }
    }
    return h;
  }
}

/**
 * MinimaxContext class to group the parameters passed between min and max functions. It contains
 * the current board state, column being evaluated, search depth, current player's color, and
 * alpha-beta bounds.
 */
class MinimaxContext {
  Tauler t; // The current board state.
  int depth; // The remaining depth for the search.
  int column; // The column currently being evaluated.
  int actualColumn; // The current player's color.
  int alpha; // The alpha value for pruning.
  int beta; // The beta value for pruning.

  /**
   * Constructor to initialize the MinimaxContext.
   *
   * @param t The current board state.
   * @param depth The remaining depth for the search.
   * @param column The column currently being evaluated.
   * @param actualColumn The color of the current player.
   * @param alpha The alpha value for pruning.
   * @param beta The beta value for pruning.
   */
  public MinimaxContext(Tauler t, int depth, int column, int actualColumn, int alpha, int beta) {
    this.t = t;
    this.depth = depth;
    this.column = column;
    this.actualColumn = actualColumn;
    this.alpha = alpha;
    this.beta = beta;
  }
}
