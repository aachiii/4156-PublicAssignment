package controllers;

import java.io.IOException;
import java.util.Queue;
import org.eclipse.jetty.websocket.api.Session;
import com.google.gson.Gson;
import io.javalin.Javalin;
import models.GameBoard;
import models.Message;
import models.Player;



class PlayGame {

  private static final int PORT_NUMBER = 8080;

  private static Javalin app;

  private static GameBoard gameboard = new GameBoard();

  // Decide if it is a draw
  private static boolean isDraw(char[][] boardState) {
    for (int x = 0; x < boardState.length; x++) {
      for (int y = 0; y < boardState[x].length; y++) {
        if (boardState[x][y] == '\u0000') {
          return false;
        }
      }
    }
    return true;
  }

  // Decide if there is a winner of draw
  private static int getBoardStatus(char[][] boardState, int x, int y) {
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
    } else if (isDraw(boardState)) {
      return -1;
    }
    return 0;
  }

  /**
   * Main method of the application.
   * 
   * @param args Command line arguments
   */

  public static void main(final String[] args) {

    app = Javalin.create(config -> {
      config.addStaticFiles("/public");
    }).start(PORT_NUMBER);

    // Test Echo Server
    app.post("/echo", ctx -> {
      ctx.result(ctx.body());
    });

    app.get("/hello", ctx -> {
      ctx.result("Hello, world!");
    });

    // Redirect to new game page
    app.get("/newgame", ctx -> {
      ctx.redirect("/tictactoe.html");
    });

    // Start a new game and initialize the gameBoard
    app.post("/startgame", ctx -> {
      String requestBody = ctx.body();
      System.out.print(requestBody);
      String[] tokens = requestBody.split("=");
      System.out.println(tokens);



      Player player1 = new Player();
      player1.setId(1);
      player1.setType(tokens[1].charAt(0));

      gameboard.setP1(player1);

      gameboard.setBoardState(new char[3][3]);
      gameboard.setWinner(0);
      gameboard.setDraw(false);
      gameboard.setTurn(1);

      Gson gsonLib = new Gson();
      String jsonGameboard = gsonLib.toJson(gameboard);

      System.out.println(jsonGameboard);

      ctx.result(jsonGameboard);

      // sendGameBoardToAllPlayers()
    });

    // Player2 join the game and update gameBoard
    app.get("/joingame", ctx -> {

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
      ctx.redirect("/tictactoe.html?p=2");
      Gson gsonLib = new Gson();
      sendGameBoardToAllPlayers(gsonLib.toJson(gameboard));
    });

    // Respond to movements. Update gameBoard. Throw exceptions when there is a not valid movement.
    app.post("/move/:playerId", ctx -> {
      String playerId = ctx.pathParam("playerId");
      int x = Integer.parseInt(ctx.formParam("x"));
      int y = Integer.parseInt(ctx.formParam("y"));
      int xp;
      int yp;

      boolean moveValidity;
      String message;
      int code;

      if (gameboard.getP1().getType() == 'X') {
        xp = 1;
        yp = 2;
      } else {
        xp = 2;
        yp = 1;
      }

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
            || (gameboard.getTurn() % 2 == 1 && playerId.equals("2"))) {
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

        int status = getBoardStatus(boardstate, xp, yp);

        if (status == -1) {
          gameboard.setDraw(true);
        } else if (status > 0) {
          gameboard.setWinner(status);
        }
        moveValidity = true;
        code = 100;
        message = "";
        gameboard.setTurn(gameboard.getTurn() == 1 ? 2 : 1);
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
