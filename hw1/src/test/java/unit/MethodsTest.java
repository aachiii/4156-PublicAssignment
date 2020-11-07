package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import controllers.PlayGame;
import models.GameBoard;
import org.junit.jupiter.api.Test;



public class MethodsTest {
  PlayGame playgame = new PlayGame();

  /**
   * This is to test createConnection() method of PlayGame.
   */
  @Test
  public void testCheckDraw() {
    // initialize the GameBoard
    GameBoard gameBoard = new GameBoard();

    char[][] board = gameBoard.getBoardState();

    gameBoard.setBoardState(new char[3][3]);
    board = gameBoard.getBoardState();
    gameBoard.setWinner(0);
    gameBoard.setDraw(false);

    // -------------------1st case, there is a draw----------------
    board[0][0] = board[0][2] = board[1][0] = board[2][1] = board[2][2] = 'X';
    board[0][1] = board[1][1] = board[1][2] = board[2][0] = 'O';

    assertEquals(true, PlayGame.gameIsDraw(gameBoard.getBoardState()));

    // -------------------2nd case, there is not a draw--------------
    gameBoard.setBoardState(new char[3][3]);
    board[0][0] = board[0][1] = board[1][1] = board[2][1] = board[2][2] = 'X';
    board[0][2] = board[1][0] = board[1][2] = board[2][0] = 'O';

    assertEquals(false, PlayGame.gameIsDraw(gameBoard.getBoardState()));

  }


  /**
   * This is to test case 1 getBoardStatus() method of PlayGame.
   */
  @Test
  public void checkGetBoardStatus1() {
    // initialize the GameBoard
    GameBoard gameBoard = new GameBoard();

    char[][] board = gameBoard.getBoardState();


    // -------------------1st case, col----------------
    board[0][0] = board[0][1] = board[0][2] = 'O';
    assertEquals(2, PlayGame.getBoardStatus(board, 1, 2));

  }


  /**
   * This is to test case 2 getBoardStatus() method of PlayGame.
   */
  @Test
  public void checkGetBoardStatus2() {
    // initialize the GameBoard
    GameBoard gameBoard = new GameBoard();

    char[][] board = gameBoard.getBoardState();

    // -------------------2nd case, row--------------
    gameBoard.setBoardState(new char[3][3]);

    board[0][0] = board[1][0] = board[2][0] = 'O';

    assertEquals(2, playgame.getBoardStatus(board, 1, 2));
  }


  /**
   * This is to test case 3 getBoardStatus() method of PlayGame.
   */
  @Test
  public void checkGetBoardStatus3() {
    // initialize the GameBoard
    GameBoard gameBoard = new GameBoard();

    char[][] board = gameBoard.getBoardState();
    gameBoard.setBoardState(new char[3][3]);

    // -------------------3rd case, cross1--------------
    gameBoard.setBoardState(new char[3][3]);

    board[0][0] = board[1][1] = board[2][2] = 'X';

    assertEquals(1, PlayGame.getBoardStatus(board, 1, 2));
  }


  /**
   * This is to test case 4 getBoardStatus() method of PlayGame.
   */
  @Test
  public void checkGetBoardStatus4() {
    // initialize the GameBoard
    GameBoard gameBoard = new GameBoard();

    char[][] board = gameBoard.getBoardState();
    gameBoard.setBoardState(new char[3][3]);

    // -------------------4th case, cross2--------------
    gameBoard.setBoardState(new char[3][3]);

    board[0][2] = board[1][1] = board[2][0] = 'X';

    assertEquals(1, PlayGame.getBoardStatus(board, 1, 2));

  }

}
