package co.edu.unbosque.reventacero;

import io.javalin.Javalin;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import co.edu.unbosque.reventacero.configuration.ConexionDB;


public class Main {
	public static void main(String[] args) {
        Javalin app = Javalin.create().start(7071);

        app.get("/ping-bd", ctx -> {
            try (Connection con = ConexionDB.obtener();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT 'OK' FROM dual")) {
                rs.next();
                ctx.result("Conexión " + rs.getString(1));
            } catch (SQLException e) {
                ctx.status(500).result("Error: " + e.getMessage());
            }
        });
    }
}
