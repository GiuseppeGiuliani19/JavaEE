package com.example.javaee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Locale;

@WebServlet("/MonitoringServlet")
public class MonitoringServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*"); // Permetti accesso da qualsiasi origine
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type"); // Permette header specifici se necessario
        try {
            // Loading MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establishing connection
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_web", "root", "Socialdex19!");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT i.idimpianto, i.descrizione, i.latitudine, i.longitudine, MAX(s.timestamp) AS last_signal " +
                                 "FROM impianti i JOIN segnalazioni s ON i.idimpianto = s.idimpianto " +
                                 "GROUP BY i.idimpianto, i.descrizione, i.latitudine, i.longitudine")) {
                StringBuilder jsonBuilder = new StringBuilder("[");
                while (rs.next()) {
                    long lastSignalEpoch = rs.getTimestamp("last_signal").getTime();
                    long now = System.currentTimeMillis();
                    boolean isActive = (now - lastSignalEpoch) < 120000;
                    // Usa Locale.ENGLISH per garantire che il separatore decimale sia il punto
                    jsonBuilder.append(String.format(Locale.ENGLISH, "{\"idImpianto\": %d, \"descrizione\": \"%s\", \"latitude\": %.6f, \"longitude\": %.6f, \"lastSignal\": \"%s\", \"attivo\": %b},",
                            rs.getInt("idimpianto"), rs.getString("descrizione"), rs.getDouble("latitudine"), rs.getDouble("longitudine"), rs.getTimestamp("last_signal"), isActive));
                }
                if (jsonBuilder.length() > 1) jsonBuilder.setLength(jsonBuilder.length() - 1);
                jsonBuilder.append("]");
                response.getWriter().write(jsonBuilder.toString());
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve data");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MySQL Driver not found");
        }
    }
}
