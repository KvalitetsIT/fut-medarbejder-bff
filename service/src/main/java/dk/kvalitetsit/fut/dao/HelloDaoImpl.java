package dk.kvalitetsit.fut.dao;

//import org.springframework.jdbc.core.DataClassRowMapper;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;


public class HelloDaoImpl {

    /*
    private static final Logger logger = LoggerFactory.getLogger(HelloDaoImpl.class);
    private final NamedParameterJdbcTemplate template;

    public HelloDaoImpl(DataSource dataSource) {
        template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void insert(HelloEntity helloEntity) {
        logger.info("Inserting new entry in database.");

        var sql = "insert into hello_table(name) values(:name)";

        var parameterMap = new HashMap<String, Object>();
        parameterMap.put("name", helloEntity.name());

        template.update(sql, parameterMap);
    }

    @Override
    public List<HelloEntity> findAll() {
        var sql = "select * from hello_table";

        return template.query(sql, new DataClassRowMapper<>(HelloEntity.class));
    }

    */
}
