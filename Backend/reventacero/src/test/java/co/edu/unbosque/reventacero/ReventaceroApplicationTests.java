package co.edu.unbosque.reventacero;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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

	@Test
	void contextLoads() {
	}

}
