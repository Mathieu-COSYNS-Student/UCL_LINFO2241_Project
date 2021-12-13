package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class Database {

  private static final BasicDataSource ds = new BasicDataSource();

  static {
    ds.setUrl("jdbc:sqlite:password.db");
    ds.setMinIdle(2);
    ds.setMaxIdle(10);
    ds.setMaxOpenPreparedStatements(100);
  }

  public static Connection getConnection() throws SQLException {
    return ds.getConnection();
  }

  public void initDb() {
    try (Connection connection = getConnection()) {
      this.dropAll(connection);
      this.createAll(connection);
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }

  private void dropAll(Connection connection) throws SQLException {
    String dropSQL = "DROP TABLE IF EXISTS password_sha1";

    try (PreparedStatement drop = connection.prepareStatement(dropSQL)) {
      drop.execute();
    }
  }

  private void createAll(Connection connection) throws SQLException {
    String createSQL = "CREATE TABLE password_sha1 (sha1 BLOB NOT NULL PRIMARY KEY, password VARCHAR(255) NOT NULL)";

    try (PreparedStatement create = connection.prepareStatement(createSQL)) {
      create.execute();
    }
  }

  public void insertHash(byte[] hash, String password) {
    try (Connection connection = getConnection()) {
      this.insertHash(connection, hash, password);
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }

  private void insertHash(Connection connection, byte[] hash, String password) throws SQLException {
    String sql = "INSERT INTO password_sha1 (sha1, password) VALUES (?, ?)";
    try (PreparedStatement insert = connection.prepareStatement(sql)) {
      insert.setBytes(1, hash);
      insert.setString(2, password);
      insert.executeUpdate();
    }
  }

  public void insertHashList(byte[][] hashes, String[] passwords, int size) {
    try (Connection connection = getConnection()) {
      this.insertHashList(connection, hashes, passwords, size);
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }
  }

  private void insertHashList(Connection connection, byte[][] hashes, String[] passwords, int size)
      throws SQLException {
    StringBuilder sql = new StringBuilder(
        "INSERT OR IGNORE INTO password_sha1 (sha1, password) VALUES ");
    sql.append("(?, ?),".repeat(size));
    sql.setCharAt(sql.length() - 1, ';');
    try (PreparedStatement insert = connection.prepareStatement(sql.toString())) {
      for (int i = 0; i < size; i++) {
        insert.setBytes(i * 2 + 1, hashes[i]);
        insert.setString(i * 2 + 2, passwords[i]);
      }
      insert.executeUpdate();
    }
  }

  public String getPassword(byte[] hash) {
    try (Connection connection = getConnection()) {
      return this.getPassword(connection, hash);
    } catch (SQLException e) {
      System.err.println(e.getMessage());
    }

    return null;
  }


  private String getPassword(Connection connection, byte[] hash) throws SQLException {
    String sql = "SELECT password FROM password_sha1 WHERE sha1 = ?";
    try (PreparedStatement getPassword = connection.prepareStatement(sql)) {
      getPassword.setBytes(1, hash);
      ResultSet resultSet = getPassword.executeQuery();

      if (resultSet.next()) {
        return resultSet.getString("password");
      }
    }

    return null;
  }
}
