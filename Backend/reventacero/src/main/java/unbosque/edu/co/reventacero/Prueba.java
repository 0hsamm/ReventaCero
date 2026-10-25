package unbosque.edu.co.reventacero;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Prueba {
	
	 private final JdbcTemplate jdbc;

	    public Prueba(JdbcTemplate jdbc) {
	        this.jdbc = jdbc;
	    }

	    @GetMapping("/prueba")
	    public String prueba() {
	        return jdbc.queryForObject(
	            "SELECT USER || ' en ' || SYS_CONTEXT('USERENV','CON_NAME') FROM dual",
	            String.class);
	    }
	    @GetMapping("/datos")
	    public List<Map<String, Object>> datos() {
	        return jdbc.queryForList("SELECT id, mensaje FROM prueba_conexion");
	    }
}
