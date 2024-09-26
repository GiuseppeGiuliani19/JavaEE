package com.example.javaee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.Locale;

@WebServlet("/Statoimpianti")
public class Statoservlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        try {
            // Loading MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establishing connection
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_web", "root", "Socialdex19!");
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT i.idimpianto, MAX(s.timestamp) AS last_signal " +
                                 "FROM impianti i LEFT JOIN segnalazioni s ON i.idimpianto = s.idimpianto " +
                                 "GROUP BY i.idimpianto " +
                                 "ORDER BY MAX(s.timestamp) DESC")) {
                StringBuilder jsonBuilder = new StringBuilder("[");
                while (rs.next()) {
                    Timestamp lastSignal = rs.getTimestamp("last_signal");
                    long lastSignalEpoch = lastSignal != null ? lastSignal.getTime() : 0;
                    long now = System.currentTimeMillis();
                    boolean hasRecentSignal = lastSignal != null && (now - lastSignalEpoch) < 120000;
                    jsonBuilder.append(String.format(Locale.ENGLISH, "{\"idImpianto\": %d, \"lastSignal\": \"%s\", \"hasRecentSignal\": %b},",
                            rs.getInt("idimpianto"), lastSignal, hasRecentSignal));
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
