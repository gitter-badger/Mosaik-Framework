import io.github.splotycode.mosaik.database.connection.Connection;
import io.github.splotycode.mosaik.database.connection.impl.MySQLConnection;
import io.github.splotycode.mosaik.database.connection.sql.SQLDriverConnection;
import io.github.splotycode.mosaik.database.repo.SQLExecutor;
import io.github.splotycode.mosaik.database.repo.TableExecutor;
import io.github.splotycode.mosaik.database.table.*;
import io.github.splotycode.mosaik.runtime.startup.StartUpInvoke;
import io.github.splotycode.mosaik.util.prettyprint.PrettyPrint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

public class MySQlTest {

    @Test
    public void testConnection() {
//        Connection<MySQLConnection> connection = new MySQLConnection().connect("localhost", "database");
  //      connection.makeDefault();

    //    TableExecutor<Person, SQLDriverConnection> repo = new SQLExcecutor<>();

    }

    @Test
    void test() {
        StartUpInvoke.invokeTestSuite();
        Connection<MySQLConnection> connection = new MySQLConnection().connect("localhost", "root", "1234", "test");
        connection.makeDefault();

        TableExecutor<Person, SQLDriverConnection> repo = new SQLExecutor<>(Person.class);
        repo.createIfNotExists();
        repo.save(new Person("aa", "bbb", 2, "dasd@asd.ed"), Access.FIRST_NAME, Access.LAST_NAME);
        repo.save(new Person("aaa", "bbasb", 3, "dasd@asd.ed"));
        repo.save(new Person("aadsad", "bbbsdfd", 8, "dasd@asfdddd.ed"));
        System.out.println(new PrettyPrint(repo.selectAll()).prettyPrintType());
    }

    enum Access implements ColumnNameResolver {
        FIRST_NAME{
            @Override
            public String getColumnName() {
                return "firstName";
            }
        }, LAST_NAME {
            @Override
            public String getColumnName() {
                return "lastName";
            }
        };

        @Override
        public abstract String getColumnName();

    }

    @Table
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Person {

        @Column(typeParameters = 30)
        private String firstName, lastName;

        @Column
        @AutoIncrement
        @Primary
        @NotNull
        private long id;

        @Column(type = ColumnType.TEXT, typeParameters = 20)
        private String email;

    }
}
