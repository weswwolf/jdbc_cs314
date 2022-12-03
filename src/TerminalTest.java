import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
/*
    void verify_provider()
    void handle_member()
 */
class TerminalTest {

    @Test
    void verify_provider()
    {
        String good_prov_id = "000000000";
        String bad_prov_id = "112233445";
        //testing for database connection error
        assertEquals(1, myjdbc.validate_provider(good_prov_id), "expected connection error, failed");
        myjdbc.connect_to_database();
        //testing for a valid provider id
        assertEquals(0, myjdbc.validate_provider(good_prov_id), "expected validated, failed");
        //testing for a invalid provider id
        assertEquals(2, myjdbc.validate_provider(bad_prov_id), "expected not validated, failed");
    }

    @Test
    void handle_member() {
        Terminal t = new Terminal();
        assertEquals(t.handle_member(0), 0);
        assertEquals(t.handle_member(4), 2);
        assertEquals(t.handle_member(1), 1);
    }

    @Test
    void login_handle(){
        //Should print Database Error then Invalid Provider then Access Granted:
        Terminal t = new Terminal();
        assertEquals(1, t.login_handle(1));
        assertEquals(2, t.login_handle(2));
        assertEquals(0, t.login_handle(0));
        assertEquals(3, t.login_handle(100));
    }

    @Test
    void view_service_directory(){
        //Makes sure that an error is returned if the directory is null/empty:
        Terminal t = new Terminal();
        assertEquals(1, t.view_service_directory());
    }

}