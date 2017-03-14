package com.example.guestbook;

import com.example.guestbook.entity.Greeting;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;

public class GuestBookDAO {

    // Détermine l'URL du serveur MySQL à utiliser
    // Teste si on est en mode développement ou en mode production
    private String jdbcUrl() throws ServletException {
		String url;
		if (System.getProperty("com.google.appengine.runtime.version").startsWith("Google App Engine/")) {
			// Check the System properties to determine if we are running on appengine or not
			// Google App Engine sets a few system properties that will reliably be present on a remote
			// instance.
			url = System.getProperty("ae-cloudsql.cloudsql-database-url");
			try {
				// Load the class that provides the new "jdbc:google:mysql://" prefix.
				Class.forName("com.mysql.jdbc.GoogleDriver");
			} catch (ClassNotFoundException e) {
				throw new ServletException("Error loading Google JDBC Driver", e);
			}
		} else {
			// Set the url with the local MySQL database connection url when running locally
			url = System.getProperty("ae-cloudsql.local-database-url");
		}
		System.err.println(url);
		return url;
	}

        
	public void addGreeting(String book, String authorId, String authorEmail, String content) throws SQLException, ServletException {
		String sql = "INSERT INTO GREETING(book, authorId, authorEmail, content) VALUES(?, ?, ?, ?)";
		try (Connection conn = DriverManager.getConnection(jdbcUrl());
			 PreparedStatement create = conn.prepareStatement(sql)) {
			create.setString(1, book);
			create.setString(2, authorId);
			create.setString(3, authorEmail);
			create.setString(4, content);
			create.executeUpdate();
		}
	}

	public List<String> existingBooks() throws SQLException, ServletException {
		LinkedList<String> result = new LinkedList<>();
		String sql = "SELECT DISTINCT book FROM GREETING ORDER BY book";
		try (Connection conn = DriverManager.getConnection(jdbcUrl());
			 Statement select = conn.createStatement();
			 ResultSet rs = select.executeQuery(sql)) {
			while (rs.next()) {
				result.add(rs.getString("book"));
			}
		}
		
		return result;
	}

	public List<Greeting> findGreetingsIn(String book) throws SQLException, ServletException {
		LinkedList<Greeting> result = new LinkedList<>();
		String sql = "SELECT * FROM GREETING WHERE book = ? ORDER BY created DESC";
		try (Connection conn = DriverManager.getConnection(jdbcUrl());
			 PreparedStatement create = conn.prepareStatement(sql)) {
			create.setString(1, book);
			try (ResultSet rs = create.executeQuery()) {
				while (rs.next()) {
					long id = rs.getLong("id");
					String authorId = rs.getString("authorId");
					String authorEmail = rs.getString("authorEmail");
					String content = rs.getString("content");
					Timestamp created = rs.getTimestamp("created");	
					Greeting g = new Greeting(id, book, authorId, authorEmail, content, created);
					result.add(g);
				}				
			}
		}
		
		return result;
	}
	
        // Récupère les statistiques
        public Map<String, Integer> getStats() throws SQLException, ServletException {
                HashMap<String, Integer> result = new HashMap<>();
                String sql = "SELECT book, COUNT(*) AS nb FROM GREETING GROUP BY book";
                try (Connection conn = DriverManager.getConnection(jdbcUrl());
                        Statement select = conn.createStatement();
                        ResultSet rs = select.executeQuery(sql)) {
                                while (rs.next()) {
                                        String book = rs.getString("book");
                                        int nb = rs.getInt("nb");
                                        result.put(book, nb);
                                }
                        }
                return result;
 	}

}
