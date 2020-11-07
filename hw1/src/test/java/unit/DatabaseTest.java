package unit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import utils.DatabaseJdbc;

@TestMethodOrder(OrderAnnotation.class)
public class DatabaseTest {
 
  /**
   * This is to test gameIsDraw() method of DatabaseJdbc.
   */
  @Test
  @Order(1)
  public void testConnection() throws SQLException {
    System.out.println("----------------Test Connection----------------");
    DatabaseJdbc jdbc = new DatabaseJdbc();
    Connection con = jdbc.createConnection();
    boolean reachable = con.isValid(10); //10 sec
    assertEquals(true, reachable);
    
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
  }
  
  /**
   * This is to test createTable() method of DatabaseJdbc.
   * @throws SQLException a
   */
  @Test
  @Order(2)
  public void testCreateTable() throws SQLException {
    System.out.println("----------------Test Create table----------------");
    DatabaseJdbc jdbc = new DatabaseJdbc();
    Connection con = jdbc.createConnection();
    String tablename = "ASE_I3_MOVE";
    boolean tableCreated = jdbc.createTable(con, tablename);
    assertEquals(true, tableCreated);
    
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    
  }

  /**
   * This is to test addMoveData() method of DatabaseJdbc.
   * @throws SQLException a
   */
  @Test
  @Order(3)
  public void testAddMoveData() throws SQLException {
    System.out.println("----------------Test add data----------------");
    DatabaseJdbc jdbc = new DatabaseJdbc();
    Connection con = jdbc.createConnection();
    String tablename = "ASE_I3_MOVE";
    boolean tableCreated = jdbc.createTable(con, tablename);
    char[][] board = {
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'}
    };
    boolean tupleAdded = jdbc.addMoveData(con, board, 2, 'X', true);
    
    assertEquals(true, tupleAdded);
    
    
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
    
  }
  
  /**
   * This is to test getLastBoard() method of DatabaseJdbc.
   * @throws SQLException a
   */
  @Test
  @Order(4)
  public void testGetLastBoard() throws SQLException {
    System.out.println("----------------Test get board----------------");
    DatabaseJdbc jdbc = new DatabaseJdbc();
    Connection con = jdbc.createConnection();
    String tablename = "ASE_I3_MOVE";
    boolean tableCreated = jdbc.createTable(con, tablename);
    char[][] board = {
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'}
    };
    boolean tupleAdded = jdbc.addMoveData(con, board, 2, 'X', true);
    char[][] getBoard = jdbc.getLastBoard(con, tablename);
    
    
    assertEquals(board[0][0], getBoard[0][0]);
    
    
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    
  }
  
  
  /**
   * This is to test getLastPlayer() method of DatabaseJdbc.
   * @throws SQLException a
   */
  @Test
  @Order(5)
  public void testGetLastPlayer() throws SQLException {
    System.out.println("----------------Test get last player----------------");
    DatabaseJdbc jdbc = new DatabaseJdbc();
    Connection con = jdbc.createConnection();
    String tablename = "ASE_I3_MOVE";
    boolean tableCreated = jdbc.createTable(con, tablename);
    char[][] board = {
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'}
    };
    boolean tupleAdded = jdbc.addMoveData(con, board, 2, 'X', true);
    int player = jdbc.getLastPlayer(con, tablename);
    
    
    assertEquals(2, player);
    
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * This is to test getLastType() method of DatabaseJdbc.
   * @throws SQLException a
   */
  @Test
  @Order(6)
  public void testGetLastType() throws SQLException {
    System.out.println("----------------Test get last type----------------");
    DatabaseJdbc jdbc = new DatabaseJdbc();
    Connection con = jdbc.createConnection();
    String tablename = "ASE_I3_MOVE";
    boolean tableCreated = jdbc.createTable(con, tablename);
    char[][] board = {
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'}
    };
    boolean tupleAdded = jdbc.addMoveData(con, board, 2, 'X', true);
    char type = jdbc.getLastType(con, tablename);
    
    assertEquals('X', type);
    
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * This is to test getStarted() method of DatabaseJdbc.
   * @throws SQLException  a
   */
  @Test
  @Order(7)
  public void testGetStarted() throws SQLException {
    System.out.println("----------------Test get started----------------");
    DatabaseJdbc jdbc = new DatabaseJdbc();
    Connection con = jdbc.createConnection();
    String tablename = "ASE_I3_MOVE";
    boolean tableCreated = jdbc.createTable(con, tablename);
    char[][] board = {
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'},
        {'\u0000', '\u0000', '\u0000'}
    };
    boolean tupleAdded = jdbc.addMoveData(con, board, 2, 'X', true);
    boolean started = jdbc.getStarted(con, tablename);
    
    
    assertEquals(true, started);
    
    try {
      con.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  
  
}