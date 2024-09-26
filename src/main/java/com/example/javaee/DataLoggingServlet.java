package com.example.javaee;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

@WebServlet("/DataLoggingServlet")
public class DataLoggingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int idImpianto = Integer.parseInt(request.getParameter("idImpianto"));
        int idPalinsesto = Integer.parseInt(request.getParameter("idPalinsesto"));
        int idCartellone = Integer.parseInt(request.getParameter("idCartellone"));
        int durata = Integer.parseInt(request.getParameter("durata"));

        try {
            // Explicitly load MySQL JDBC driver to ensure it is available to the DriverManager
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connection setup and execution of the PreparedStatement
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db_web", "root", "Socialdex19!")) {
                System.out.println("Connection established successfully.");

                PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO segnalazioni (idimpianto, idpalinsesto, idcartellone, durata, timestamp) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)");
                pstmt.setInt(1, idImpianto);
                pstmt.setInt(2, idPalinsesto);
                pstmt.setInt(3, idCartellone);
                pstmt.setInt(4, durata);
                pstmt.executeUpdate();
                pstmt.close();  // Explicitly close PreparedStatement
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to log data");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "MySQL Driver not found");
        }
    }
}
