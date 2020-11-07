package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import controllers.PlayGame;
import java.util.concurrent.TimeUnit;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import models.GameBoard;
import models.Message;
import models.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;




@TestMethodOrder(OrderAnnotation.class)
public class GameTest {

  /**
   * Runs only once before the testing starts.
   */
  @BeforeAll
  public static void init() {
    // Start Server
    PlayGame.main(null);
    System.out.println("Before All");
  }

  /**
   * This method starts a new game before every test run. It will run every time before a test.
   */
  @BeforeEach
  public void startNewGame() {
    // Test if server is running. You need to have an endpoint /
    // If you do not wish to have this end point, it is okay to not have anything in this method.
    HttpResponse<String> response = Unirest.get("http://localhost:8080/").asString();
    int restStatus = response.getStatus();

    System.out.println("Before Each");
  }

  /**
   * This is a test case to evaluate the newgame endpoint.
   */
  @Test
  @Order(1)
  public void newGameTest() {

    // Create HTTP request and get response
    HttpResponse<String> response = Unirest.get("http://localhost:8080/newgame").asString();
    int restStatus = response.getStatus();

    // Check assert statement (New Game has started)
    assertEquals(restStatus, 200);
    System.out.println("Test New Game");
  }

  /**
   * This is a test case to evaluate the startgame endpoint.
   */
  @Test
  @Order(2)
  public void startGameTest() {

    System.out.println("----------------Test Start Game----------------");

    // Create a POST request to startgame endpoint and get the body
    // Remember to use asString() only once for an endpoint call.
    // Every time you call asString(), a new request will be sent to the endpoint.
    // Call it once and then use the data in the object.
    HttpResponse<String> response =
        Unirest.post("http://localhost:8080/startgame").body("type=O").asString();
    String responseBody = response.getBody();

    // --------------------------- JSONObject Parsing ----------------------------------

    System.out.println("Start Game Response: " + responseBody);

    // Parse the response to JSON object
    JSONObject jsonObject = new JSONObject(responseBody);

    // Check if game started after player 1 joins: Game should not start at this point
    assertEquals(false, jsonObject.get("gameStarted"));

    // ---------------------------- GSON Parsing -------------------------

    // GSON use to parse data to object
    Gson gson = new Gson();
    GameBoard gameBoard = gson.fromJson(jsonObject.toString(), GameBoard.class);
    Player player1 = gameBoard.getP1();

    // Check if player type is correct
    assertEquals('O', player1.getType());


  }


  /**
   * This is to evaluate joingame end point.
   */
  @Test
  @Order(3)
  public void joinGameTest() {
    System.out.println("----------------Test Join Game----------------");
    // Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    HttpResponse<String> response = Unirest.get("http://localhost:8080/joingame").asString();

    // Check the status of response of calling joingame endpoint
    assertEquals(200, response.getStatus());
  }

  /**
   * this is a test case to verify that player1 cannot make a movement before player2 join in.
   */
  @Test
  @Order(4)
  public void moveBeforePlayerTwoJoinTest() {

    System.out.println("----------------Test Case1: Move Before P2 Join Game----------------");

    // Create a POST request to start endpoint
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();

    // P1 make a movement at (0,0) before P2 join the game
    HttpResponse<String> response =
        Unirest.post("http://localhost:8080/move/1").body("x=0&&y=0").asString();
    String responseBody = response.getBody();
    System.out.println("Move Before P2 Join: " + responseBody);

    // Parse the message
    JSONObject jsonObject = new JSONObject(responseBody);
    Gson gson = new Gson();
    Message message = gson.fromJson(jsonObject.toString(), Message.class);

    // Check if the movement is valid according to the message of response
    // Move validity should be false
    assertEquals(false, message.isMoveValidity());
  }

  /**
   * this is to verify that player1 always make a movement first.
   */
  @Test
  @Order(5)
  public void playerOneMoveFisrtTest() {

    System.out.println("----------------Test Case2: Player1 Must Move First----------------");

    // Create a POST request to start endpoint
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    // Player2 joins the game
    Unirest.get("http://localhost:8080/joingame").asString();


    // P2 try make a movement first, but will fail
    HttpResponse<String> response =
        Unirest.post("http://localhost:8080/move/2").body("x=0&&y=0").asString();
    String responseBody = response.getBody();
    System.out.println("Player2 tries to move first: " + responseBody);

    // parse the message
    JSONObject jsonObject = new JSONObject(responseBody);
    Gson gson = new Gson();
    Message message = gson.fromJson(jsonObject.toString(), Message.class);

    // check if the movement is valid according to the message of response
    // move validity should be false
    assertEquals(false, message.isMoveValidity());
  }

  /**
   * This verifies that a player cannot make two moves in their turn.
   */
  @Test
  @Order(6)
  public void playerCannotMoveTwiceTest() {

    System.out.println("----------------Test Case3: One Player cannot Move Twice Each Turn-------");

    // Create a POST request to start endpoint
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    // Player2 joins the game
    Unirest.get("http://localhost:8080/joingame").asString();

    // ----------------Case1, Player1 tries to move twice in one turn----------------
    // P1 tries to move twice
    Unirest.post("http://localhost:8080/move/1").body("x=0&&y=0").asString();
    HttpResponse<String> response =
        Unirest.post("http://localhost:8080/move/1").body("x=0&&y=1").asString();
    String responseBody = response.getBody();
    System.out.println("Player1 tries to move twice: " + responseBody);

    // parse the message
    JSONObject jsonObject = new JSONObject(responseBody);
    Gson gson = new Gson();
    Message message = gson.fromJson(jsonObject.toString(), Message.class);

    // check if the movement is valid according to the message of response
    // move validity should be false
    assertEquals(false, message.isMoveValidity());

    // ----------------Case2, Player2 tries to move twice in one turn----------------
    // P2 tries to move twice
    Unirest.post("http://localhost:8080/move/2").body("x=0&&y=1").asString();
    HttpResponse<String> response2 =
        Unirest.post("http://localhost:8080/move/2").body("x=0&&y=2").asString();
    String responseBody2 = response2.getBody();
    System.out.println("Player2 tries to move twice: " + responseBody2);

    // parse the message
    JSONObject jsonObject2 = new JSONObject(responseBody2);
    Message message2 = gson.fromJson(jsonObject2.toString(), Message.class);

    // check if the movement is valid according to the message of response
    // move validity should be false
    assertEquals(false, message2.isMoveValidity());

    // ----------------Case3, not valid move----------------
    // P2 tries to move twice
    Unirest.post("http://localhost:8080/move/1").body("x=1&&y=1").asString();
    HttpResponse<String> response3 =
        Unirest.post("http://localhost:8080/move/2").body("x=1&&y=1").asString();
    String responseBody3 = response3.getBody();
    System.out.println("Move is not valid " + responseBody3);

    // parse the message
    JSONObject jsonObject3 = new JSONObject(responseBody3);
    Message message3 = gson.fromJson(jsonObject3.toString(), Message.class);

    // check if the movement is valid according to the message of response
    // move validity should be false
    assertEquals(false, message3.isMoveValidity());
  }

  /**
   * This is to test that a player will win.
   * @throws InterruptedException a. 
   */
  @Test
  @Order(7)
  public void onePlayWinTest() throws InterruptedException {

    System.out.println("----------------Test Case4: One Player Wins----------------");

    // Create a POST request to start endpoint
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    // Player2 joins the game
    Unirest.get("http://localhost:8080/joingame").asString();

    // ----------------Player1 wins, making a row----------------
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=2").asString();
    HttpResponse<String> response = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody = response.getBody();
    System.out.println("Player1 wins, making a row" + responseBody);
    JSONObject jsonObject = new JSONObject(responseBody);

    assertEquals(1, jsonObject.get("winner"));

    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    HttpResponse<String> response2 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody2 = response2.getBody();
    System.out.println("Player1 wins, making a row" + responseBody2);
    JSONObject jsonObject2 = new JSONObject(responseBody2);
    assertEquals(1, jsonObject2.get("winner"));

    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=2").asString();
    HttpResponse<String> response3 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody3 = response3.getBody();
    System.out.println("Player1 wins, making a row" + responseBody3);
    JSONObject jsonObject3 = new JSONObject(responseBody3);
    assertEquals(1, jsonObject3.get("winner"));

    // ----------------Player1 wins, making a column, test crush---------------
    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    
    PlayGame.stop();
    TimeUnit.SECONDS.sleep(2);
    PlayGame.main(null);
    
    Unirest.get("http://localhost:8080/joingame").asString();
    
    PlayGame.stop();
    TimeUnit.SECONDS.sleep(2);
    PlayGame.main(null);
    
    
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    //
    PlayGame.stop();
    TimeUnit.SECONDS.sleep(2);
    PlayGame.main(null);
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    System.out.println("reboot success ");
    //

    HttpResponse<String> response4 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody4 = response4.getBody();
    System.out.println("Player1 wins, making a column" + responseBody4);
    JSONObject jsonObject4 = new JSONObject(responseBody4);
    assertEquals(1, jsonObject4.get("winner"));

    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=1").asString();
    HttpResponse<String> response5 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody5 = response5.getBody();
    System.out.println("Player1 wins, making a column" + responseBody5);
    JSONObject jsonObject5 = new JSONObject(responseBody5);
    assertEquals(1, jsonObject5.get("winner"));

    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=2").asString();
    HttpResponse<String> response6 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody6 = response6.getBody();
    System.out.println("Player1 wins, making a column" + responseBody6);
    JSONObject jsonObject6 = new JSONObject(responseBody6);
    assertEquals(1, jsonObject6.get("winner"));

    // ----------------Player2 wins, making a cross----------------
    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=2").asString();
    HttpResponse<String> response7 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody7 = response7.getBody();
    System.out.println("Player2 wins, making a cross" + responseBody7);
    JSONObject jsonObject7 = new JSONObject(responseBody7);
    assertEquals(2, jsonObject7.get("winner"));

    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=0").asString();
    HttpResponse<String> response8 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody8 = response8.getBody();
    System.out.println("Player2 wins, making a cross" + responseBody8);
    JSONObject jsonObject8 = new JSONObject(responseBody8);
    assertEquals(2, jsonObject8.get("winner"));

    // ----------------Player1 wins, making a column type=O----------------
    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=O").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=0").asString();
    HttpResponse<String> response9 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody9 = response9.getBody();
    System.out.println("Player1 wins, making a column" + responseBody9);
    JSONObject jsonObject9 = new JSONObject(responseBody9);
    assertEquals(1, jsonObject9.get("winner"));

    // ----------------Player1 wins, making a row type=O----------------
    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=O").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=2").asString();
    HttpResponse<String> response10 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody10 = response10.getBody();
    System.out.println("Player1 wins, making a row" + responseBody10);
    JSONObject jsonObject10 = new JSONObject(responseBody10);

    assertEquals(1, jsonObject10.get("winner"));

    // ----------------Player2 wins, making a cross----------------
    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=O").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=0").asString();
    HttpResponse<String> response11 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody11 = response11.getBody();
    System.out.println("Player2 wins, making a cross" + responseBody11);
    JSONObject jsonObject11 = new JSONObject(responseBody11);
    assertEquals(2, jsonObject11.get("winner"));

  }


  /**
   * This verifies if the board is full and nobody wins, it is a draw.
   */

  @Test
  @Order(8)
  public void drawTest() {

    System.out.println("----------------Test Case5: Draw----------------");


    // Create a POST request to start endpoint
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    // Player2 joins the game
    Unirest.get("http://localhost:8080/joingame").asString();

    // Make a draw case
    Unirest.post("http://localhost:8080/move/1").body("x=0&&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=2").asString();
    HttpResponse<String> response = Unirest.get("http://localhost:8080/getboard").asString();

    String responseBody = response.getBody();
    System.out.println("Player2 tries to move first: " + responseBody);

    // parse the message
    JSONObject jsonObject = new JSONObject(responseBody);

    // check if the movement is valid according to the message of response
    // move validity should be false
    assertEquals(true, jsonObject.get("isDraw"));
  }

  /**
   * This verifies if the board is full and nobody wins, it is a draw.
   * @throws InterruptedException a.
   */

  @Test
  @Order(9)
  public void overTest() throws InterruptedException {

    System.out.println("----------------Test Case6: Game is Over----------------");

    // ----------------Draw game is over----------------
    // Create a POST request to start endpoint
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    // Player2 joins the game
    Unirest.get("http://localhost:8080/joingame").asString();

    // Make a draw case
    Unirest.post("http://localhost:8080/move/1").body("x=0&&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=2").asString();
    
    PlayGame.stop();
    TimeUnit.SECONDS.sleep(2);
    PlayGame.main(null);
    
    HttpResponse<String> response = Unirest.get("http://localhost:8080/getboard").asString();

    String responseBody = response.getBody();
    System.out.println("Game is Over" + responseBody);

    // parse the message
    JSONObject jsonObject = new JSONObject(responseBody);

    // check if the movement is valid according to the message of response
    // move validity should be false

    assertEquals(true, jsonObject.get("isDraw"));

    // ----------------winner----------------
    Unirest.post("http://localhost:8080/").asString();
    Unirest.post("http://localhost:8080/startgame").body("type=O").asString();
    Unirest.get("http://localhost:8080/joingame").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&y=2").asString();
    HttpResponse<String> response10 = Unirest.get("http://localhost:8080/getboard").asString();
    String responseBody10 = response10.getBody();
    System.out.println("Game is Over" + responseBody10);
    JSONObject jsonObject10 = new JSONObject(responseBody10);

    assertEquals(1, jsonObject10.get("winner"));
  }

  
  /**
   * This verifies the getboard api.
   */
  @Test
  @Order(10)
  public void getboardTest() {

    System.out.println("----------------Test getboard api----------------");

    // ----------------Draw game is over----------------
    // Create a POST request to start endpoint
    Unirest.post("http://localhost:8080/startgame").body("type=X").asString();
    // Player2 joins the game
    Unirest.get("http://localhost:8080/joingame").asString();

    // Make a draw case
    Unirest.post("http://localhost:8080/move/1").body("x=0&&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=0&&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=0&&y=2").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&&y=1").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=1&&y=0").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=1&&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=1").asString();
    Unirest.post("http://localhost:8080/move/2").body("x=2&&y=0").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=2").asString();
    Unirest.post("http://localhost:8080/move/1").body("x=2&&y=2").asString();
    HttpResponse<String> response = Unirest.get("http://localhost:8080/getboard").asString();

    String responseBody = response.getBody();
    System.out.println("getboard api test" + responseBody);

    // parse the message
    JSONObject jsonObject = new JSONObject(responseBody);

    assertEquals(true, jsonObject.get("isDraw"));
    assertEquals(0, jsonObject.get("winner"));
    assertEquals(false, jsonObject.get("gameStarted"));
    assertEquals(2, jsonObject.get("turn"));

  }


  /**
   * This will run every time after a test has finished.
   */
  @AfterEach
  public void finishGame() {
    System.out.println("After Each");
  }

  /**
   * This method runs only once after all the test cases have been executed.
   */
  @AfterAll
  public static void close() {
    // Stop Server
    PlayGame.stop();
    System.out.println("After All");
  }
}
