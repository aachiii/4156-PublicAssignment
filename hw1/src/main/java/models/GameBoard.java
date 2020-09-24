package models;


public class GameBoard {
  /**
   * Represent game state. p1 The first player p2 The second player gameStarted Whether both players
   * have joined turn Which player's turn it is boardState The board representation winner Whether
   * there is a winner isDraw Whether there is a draw
   */
  public GameBoard() {
    super();
    this.gameStarted = false;
    this.turn = 1;
    this.boardState = new char[3][3];
    this.winner = 0;
    this.isDraw = false;
  }

  // public GameBoard() {
  // gameStarted = false;
  // turn = 1;
  // boardState = new char[3][3];
  // winner = 0;
  // isDraw = false;
  // }


  private Player p1;



  private Player p2;

  private boolean gameStarted;

  private int turn;

  private char[][] boardState;

  private int winner;

  private boolean isDraw;



  public Player getP1() {
    return p1;
  }

  public void setP1(Player p1) {
    this.p1 = p1;
  }

  public Player getP2() {
    return p2;
  }

  public void setP2(Player p2) {
    this.p2 = p2;
  }

  public boolean isGameStarted() {
    return gameStarted;
  }

  public void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }

  public int getTurn() {
    return turn;
  }

  public void setTurn(int turn) {
    this.turn = turn;
  }

  public char[][] getBoardState() {
    return boardState;
  }

  public void setBoardState(char[][] boardState) {
    this.boardState = boardState;
  }

  public int getWinner() {
    return winner;
  }

  public void setWinner(int winner) {
    this.winner = winner;
  }

  public boolean isDraw() {
    return isDraw;
  }

  public void setDraw(boolean isDraw) {
    this.isDraw = isDraw;
  }


}
