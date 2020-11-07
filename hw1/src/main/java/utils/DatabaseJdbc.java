package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class DatabaseJdbc {
  
 
  
  /**
   * This is create connection.
   */
  public Connection createConnection() {
    Connection c = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:ase.db");
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
    }
    System.out.println("Opened database successfully");
    return c;
    
  }
  
  /**
   * This is create table.
   * @throws SQLException a.
   */
  public boolean createTable(Connection c, String tablename) throws SQLException {
    Statement stmt = null;
    
    try {
      
      stmt = c.createStatement();
      String sql = String.format("DROP TABLE IF EXISTS %s; CREATE TABLE %s "
                     + "(ID  INT IDENTITY(1,1)  PRIMARY KEY,"
                     + "PLAYER_ID INT NOT NULL, "
                     + "TYPE CHAR, "
                     + "STARTED BOOLEAN, "
                     + "N1 TINYTEXT, "
                     + "N2 TINYTEXT, "
                     + "N3 TINYTEXT, "
                     + "N4 TINYTEXT, "
                     + "N5 TINYTEXT, "
                     + "N6 TINYTEXT, "
                     + "N7 TINYTEXT, "
                     + "N8 TINYTEXT, "
                     + "N9 TINYTEXT);", tablename, tablename);
                     
                     ;
      stmt.executeUpdate(sql);
      stmt.close();
      
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return false;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
    System.out.println("Table created successfully");
    return true;
    
  }
  
  /**
   * This is add move data to database.
   * @throws SQLException  a.
   */
  public boolean addMoveData(Connection c, char[][] boardstate, 
      int turn, char type, boolean started) throws SQLException {
    Statement stmt = null;
    int playerid;
    if (turn == 1) {
      playerid = 1; 
    } else {
      playerid = 2;
    }

    
    String[][] b = new String[3][3];
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (boardstate[i][j] != '\u0000') {
          b[i][j] = Character.toString(boardstate[i][j]);
        } else {
          b[i][j] = "null";
        }
          
      }
    }
    
    
    try {
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");
      
      stmt = c.createStatement();
      String s = "'" + playerid + "'" + ", " + "'" + type + "'" 
          + ", " + started + ", " + "'" + b[0][0] + "'" 
          + "," + "'" + b[0][1] + "'"  + "," + "'" + b[0][2] + "'"  + "," + "'" + b[1][0] + "'"  
          + "," + "'" + b[1][1] + "'"  + "," + "'" + b[1][2] + "'"  + "," + "'" + b[2][0] + "'" 
          + "," + "'" + b[2][1] + "'"  + "," + "'" + b[2][2] + "'"  + ");";
      
      String sql = "INSERT INTO ASE_I3_MOVE (PLAYER_ID, TYPE, STARTED, "
          + "N1, N2, N3, N4, N5, N6, N7, N8, N9) "
          + "VALUES (" + s;
      
//      String sql = "INSERT INTO ASE_I3_MOVE (PLAYER_ID, TYPE, STARTED, "
//          + "N1, N2, N3, N4, N5, N6, N7, N8, N9) "
//          + "VALUES (" + "'" + playerid + "'" + ", " + "'" + type + "'" 
//          + ", " + started + ", " + "'" + b[0][0] + "'" 
//          + "," + "'" + b[0][1] + "'"  + "," + "'" + b[0][2] + "'"  + "," + "'" + b[1][0] + "'"  
//          + "," + "'" + b[1][1] + "'"  + "," + "'" + b[1][2] + "'"  + "," + "'" + b[2][0] + "'" 
//          + "," + "'" + b[2][1] + "'"  + "," + "'" + b[2][2] + "'"  + ");";
      
     
      stmt.executeUpdate(sql);
      stmt.close();
      
      
      c.commit();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.err.println("error here");
      return false;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
    }
    System.out.println("Record created successfully");
    return true;
  }
  
  /**
   * This is to get board state.
   * @throws SQLException  a.
   */
  public char[][] getLastBoard(Connection c, String tablename) throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    String[][] boardstate = new String[3][3];
    char[][] boardstateFinal = new char[3][3];
    try {
      stmt = c.createStatement();
      System.out.println("getLasrBoard started");

      rs = stmt.executeQuery("SELECT * FROM " + tablename + " ORDER BY ID DESC LIMIT 1;");
      System.out.println("SELECT TOP 1 * FROM " + tablename + " ORDER BY ID DESC");

          
      while (rs.next()) {
        System.out.println("In loop");
        boardstate[0][0] = rs.getString("N1");
        boardstate[0][1] = rs.getString("N2");
        boardstate[0][2] = rs.getString("N3");
        boardstate[1][0] = rs.getString("N4");
        boardstate[1][1] = rs.getString("N5");
        boardstate[1][2] = rs.getString("N6");
        boardstate[2][0] = rs.getString("N7");
        boardstate[2][1] = rs.getString("N8");
        boardstate[2][2] = rs.getString("N9");
      }

      
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 3; j++) {
          if (boardstate[i][j].length() == 1) {
            System.out.println(boardstate[i][j] + "  fffff");
            boardstateFinal[i][j] = boardstate[i][j].charAt(0);
          } else {
            boardstateFinal[i][j] = '\u0000';
          }
            
        }
      }

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return boardstateFinal;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (rs != null) {
        rs.close();
      }
    }
    return boardstateFinal;
  }
  
  /**
   * This is to get last player id.
   * @throws SQLException a.
   */
  public int getLastPlayer(Connection c, String tablename) throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    int playerid = 0;
    try {
      stmt = c.createStatement();
      rs = stmt.executeQuery("SELECT * FROM " + tablename + " ORDER BY ID DESC LIMIT 1;");
      
      
      while (rs.next()) {
        playerid = rs.getInt("PLAYER_ID");
        System.out.println("playerid is:" + playerid);
      }

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return playerid;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (rs != null) {
        rs.close();
      }

    }
    return playerid;
  }
  

  /**
   * This is to get last player type.
   * @throws SQLException a.
   */
  public char getLastType(Connection c, String tablename) throws SQLException {
    Statement stmt = null;
    ResultSet rs = null;
    char type = 'N';
    try {
      stmt = c.createStatement();
      rs = stmt.executeQuery("SELECT * FROM " + tablename + " ORDER BY ID DESC LIMIT 1;");
      
      
      while (rs.next()) {
        type = rs.getString("TYPE").charAt(0);
      }

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return type;
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (rs != null) {
        rs.close();
      }
    }
    return type;
  }
  
  /**
   * This is to get is board started.
   * @throws SQLException a.
   */
  public boolean getStarted(Connection c, String tablename) throws SQLException {
    Statement stmt = null;
    boolean s = false;
    ResultSet rs = null;
    try {
      stmt = c.createStatement();
      rs = stmt.executeQuery("SELECT * FROM " + tablename + " ORDER BY ID DESC LIMIT 1;");
      
      
      while (rs.next()) {
        s = rs.getBoolean("STARTED");
      }


    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      return s;
      
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (rs != null) {
        rs.close();
      }
    }
    return s;
  }
  

  
  
}