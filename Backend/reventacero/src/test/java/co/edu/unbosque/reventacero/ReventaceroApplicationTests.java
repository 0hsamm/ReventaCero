package co.edu.unbosque.reventacero;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "jwt.secret=secreto-exclusivo-para-pruebas-con-mas-de-32-caracteres",
        "app.frontend.allowed-origins=http://localhost:4200"
})
class ReventaceroApplicationTests {

    @MockitoBean
    private JdbcClient jdbcClient;

    @Autowired
    private ApplicationContext contexto;

	@Test
	void contextLoads() {
		assertNotNull(contexto);
	}

}
