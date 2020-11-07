package models;

public class Player {
  
  /**
  * Construction method.
  */
  public Player(char type, int id) {
    super();
    this.type = type;
    this.id = id;
  }

  public Player() {
    // TODO Auto-generated constructor stub
  }

  public char getType() {
    return type;
  }

  public void setType(char type) {
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  private char type;

  private int id;

}
