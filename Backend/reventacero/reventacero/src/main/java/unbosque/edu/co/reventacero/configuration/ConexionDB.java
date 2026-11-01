package unbosque.edu.co.reventacero.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class ConexionDB {
	
	
	private static final Properties config = cargar();
	
	private static Properties cargar() {
		Properties props = new Properties();
		try(FileInputStream in = new FileInputStream("config.properties")){
			props.load(in);
		} catch (IOException e) {
            throw new RuntimeException("No se encontró config.properties en la raíz del proyecto", e);
        }
        return props;
    }
	
	public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(
                config.getProperty("db.url"),
                config.getProperty("db.usuario"),
                config.getProperty("db.clave"));
    }

}
