package controllers;

import com.google.gson.Gson;
import io.javalin.Javalin;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;
import models.GameBoard;
import models.Message;
import models.Player;
import org.eclipse.jetty.websocket.api.Session;
import utils.DatabaseJdbc;


/**
 * PlayGame class.
 * 
 * @author ccc
 *
 */
public class PlayGame {

  private static final int PORT_NUMBER = 8080;

  private static Javalin app;

  private static GameBoard gameboard = new GameBoard();

  /**
   * determine if it is a draw. 
   * 
   * @param boardState boardState
   * @return isDraw boolean
   */
  public static boolean gameIsDraw(char[][] boardState) {
    for (int x = 0; x < boardState.length; x++) {
      for (int y = 0; y < boardState[x].length; y++) {
        if (boardState[x][y] == '\u0000') {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * update board status.
   * 
   * @param boardState boardState
   * @param x x
   * @param y y
   * @return
   */
  public static int getBoardStatus(char[][] boardState, int x, int y) {
    for (int row = 0; row < 3; row++) {
      if (boardState[row][0] == boardState[row][1] && boardState[row][1] == boardState[row][2]) {
        if (boardState[row][0] == 'X') {
          return x;
        } else if (boardState[row][0] == 'O') {
          return y;
        }
      }
    }
    for (int col = 0; col < 3; col++) {
      if (boardState[0][col] == boardState[1][col] && boardState[1][col] == boardState[2][col]) {
        if (boardState[0][col] == 'X') {
          return x;
        } else if (boardState[0][col] == 'O') {
          return y;
        }
      }
    }
    if (boardState[0][0] == boardState[1][1] && boardState[1][1] == boardState[2][2]) {
      if (boardState[0][0] == 'X') {
        return x;
      } else if (boardState[0][0] == 'O') {
        return y;
      }
    } else if (boardState[0][2] == boardState[1][1] && boardState[1][1] == boardState[2][0]) {
      if (boardState[0][2] == 'X') {
        return x;
      } else if (boardState[0][2] == 'O') {
        return y;
      }
    } else if (gameIsDraw(boardState)) {
      return -1;
    }
    return 0;
  }

  /**
   * main function.
   * 
   * @param args args.
   * @return
   */
  public static void main(final String[] args) {

    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);



    // Redirect to new game page
    app.get("/newgame", ctx -> {
      ctx.redirect("/tictactoe.html");
    });

    // Start a new game and initialize the gameBoard
    app.post("/startgame", ctx -> {
      String requestBody = ctx.body();
      System.out.print(requestBody);
      String[] tokens = requestBody.split("=");
      // System.out.println(tokens);



      Player player1 = new Player();
      player1.setId(1);
      player1.setType(tokens[1].charAt(0));

      gameboard.setP1(player1);
      char[][] board = {
          {'\u0000', '\u0000', '\u0000'},
          {'\u0000', '\u0000', '\u0000'},
          {'\u0000', '\u0000', '\u0000'}
      };
      gameboard.setBoardState(board);
      gameboard.setWinner(0);
      gameboard.setDraw(false);
      gameboard.setTurn(1);
      gameboard.setGameStarted(false);
      
      DatabaseJdbc db = new DatabaseJdbc();
      Connection con = db.createConnection();
      boolean tableCreated = db.createTable(con, "ASE_I3_MOVE");
      System.out.println("tableCreated: " + tableCreated);
      
      boolean boardAdded = db.addMoveData(con, gameboard.getBoardState(),
          0, player1.getType(), gameboard.isGameStarted());
      System.out.println("boardAdded: " + boardAdded);
      
      
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      
      Gson gsonLib = new Gson();
      String jsonGameboard = gsonLib.toJson(gameboard);

      
      

      ctx.result(jsonGameboard);

      // sendGameBoardToAllPlayers()
    });

    // Player2 join the game and update gameBoard
    app.get("/joingame", ctx -> {
      
      DatabaseJdbc db = new DatabaseJdbc();
      Connection con = db.createConnection();
      
      gameboard.setBoardState(db.getLastBoard(con, "ASE_I3_MOVE"));
      Player player1 = new Player();
      player1.setId(1);
      player1.setType(db.getLastType(con, "ASE_I3_MOVE"));
      gameboard.setP1(player1);;
      
      Player player2 = new Player();
      player2.setId(2);
      char p1Type = gameboard.getP1().getType();

      if (p1Type == 'X') {
        player2.setType('O');
      } else {
        player2.setType('X');
      }

      gameboard.setP2(player2);
      gameboard.setGameStarted(true);
      boolean boardAdded = db.addMoveData(con, gameboard.getBoardState(),
          2, player2.getType(), gameboard.isGameStarted());
      
      try {
        con.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
      
      ctx.redirect("/tictactoe.html?p=2");
      Gson gsonLib = new Gson();
      sendGameBoardToAllPlayers(gsonLib.toJson(gameboard));
    });

    // Respond to movements. Update gameBoard. Throw exceptions when there is a not valid movement.
    app.post("/move/:playerId", ctx -> {
      System.out.println("-----------------------");
      System.out.println("I have a new MOVE!!!!!!");
      String playerId = ctx.pathParam("playerId");
      int x = Integer.parseInt(ctx.formParam("x"));
      int y = Integer.parseInt(ctx.formParam("y"));
      int xp;
      int yp;
      
      DatabaseJdbc db = new DatabaseJdbc();
      Connection con = db.createConnection();
      
      char lastType = db.getLastType(con, "ASE_I3_MOVE");
      
      int lastPlayer = db.getLastPlayer(con, "ASE_I3_MOVE");

      boolean started = db.getStarted(con, "ASE_I3_MOVE");
      gameboard.setBoardState(db.getLastBoard(con, "ASE_I3_MOVE"));
      gameboard.setGameStarted(started);
      Player ppLast = new Player(lastType, lastPlayer);
      char blastType;
      int blastPlayer;
      
      if (lastPlayer == 1) {
        blastPlayer = 2; 
      } else {
        blastPlayer = 1;
      }
      if (lastType == 'X') {
        blastType = 'O';
      } else {
        blastType = 'X';
      }
      Player p2Last = new Player(blastType, blastPlayer);
      
      
      
      
      if (lastPlayer == 1) {
        gameboard.setP1(ppLast);
        gameboard.setP2(p2Last);
        if (lastType == 'X') {
          xp = 1;
          yp = 2;
        } else {
          xp = 2;
          yp = 1;
        }
      } else {
        gameboard.setP2(ppLast);
        gameboard.setP1(p2Last);
        if (lastType == 'X') {
          xp = 2;
          yp = 1;
        } else {
          xp = 1;
          yp = 2;
        }
      }

      
      int status = getBoardStatus(gameboard.getBoardState(), xp, yp);

      if (status == -1) {
        gameboard.setDraw(true);
      } else if (status > 0) {
        gameboard.setWinner(status);
      }
      System.out.println("This is gameboard: " + gameboard.getP1().getType() + gameboard.getTurn());

      

      boolean moveValidity;
      String message;
      int code;
      
      

      try {
        if (!gameboard.isGameStarted()) {
          throw new IOException("Both players must have joined");
        } else if (gameboard.isDraw() || gameboard.getWinner() > 0) {
          gameboard.setGameStarted(false);
          throw new IOException("Game is already over");
        }

        char[][] boardstate = gameboard.getBoardState();
        char type;

        if (gameboard.getTurn() == 1 && playerId.equals("2")) {
          throw new IOException("Player 1 did not move first");
        } else if ((gameboard.getTurn() % 2 == 0 && playerId.equals("1"))
            || (gameboard.getTurn() % 2 != 0 && playerId.equals("2"))) {
          throw new IOException("Player cannot make two moves in their turn");
        } else if (boardstate[x][y] != '\u0000') {
          throw new IOException("Please make a legal move");
        }

        if (playerId.equals("1")) {
          type = gameboard.getP1().getType();
        } else {
          type = gameboard.getP2().getType();
        }

        boardstate[x][y] = type;
        gameboard.setBoardState(boardstate);

        int status2 = getBoardStatus(boardstate, xp, yp);

        if (status2 == -1) {
          gameboard.setDraw(true);
        } else if (status2 > 0) {
          gameboard.setWinner(status2);
        }
        moveValidity = true;
        code = 100;
        message = "";
        gameboard.setTurn(gameboard.getTurn() == 1 ? 2 : 1);
        char t;
        if (lastType == 'X') {
          t = 'O';
        } else {
          t = 'X';
        }
        
        
        db.addMoveData(con, boardstate, gameboard.getTurn() == 1 ? 2 : 1, 
            t, gameboard.isGameStarted());
        try {
          con.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
        
        
        Gson gsonLib = new Gson();
        ctx.result(gsonLib.toJson(new Message(moveValidity, code, message)));
      } catch (IOException e) {
        moveValidity = false;
        code = 200;
        message = e.getMessage();
        Gson gsonLib = new Gson();
        ctx.result(gsonLib.toJson(new Message(moveValidity, code, message)));
      }
      Gson gsonLib = new Gson();
      sendGameBoardToAllPlayers(gsonLib.toJson(gameboard));



    });

    
    // Move Endpoint
    // 1- player
    // 2- Move is valid
    // 3- Game winner
    // 4- Game draw

    // get board status
    app.get("/getboard", ctx -> {
      Gson gsonLib = new Gson();
      ctx.result(gsonLib.toJson(gameboard));
    });


    // Web sockets - DO NOT DELETE or CHANGE
    app.ws("/gameboard", new UiWebSocket());
  }

  /**
   * Send message to all players.
   * 
   * @param gameBoardJson Gameboard JSON
   * @throws IOException Websocket message send IO Exception
   */
  private static void sendGameBoardToAllPlayers(final String gameBoardJson) {
    Queue<Session> sessions = UiWebSocket.getSessions();
    for (Session sessionPlayer : sessions) {
      try {
        sessionPlayer.getRemote().sendString(gameBoardJson);
      } catch (IOException e) {
        // Add logger here
      }
    }
  }

  public static void stop() {
    app.stop();
  }
}
