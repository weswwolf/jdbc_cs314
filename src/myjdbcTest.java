import org.junit.jupiter.api.Test;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class myjdbcTest
{
    // test that we can successfully connect to the database


    @Test
    void get_service_directory() {
        myjdbc.connect_to_database();
        ArrayList<Service> list;
        list = myjdbc.get_service_directory();
        assertNotEquals(null, list);
        assertEquals(list.size(), myjdbc.get_count_serv_dir());
    }

    @Test void connect_to_database()
    {
        // assertEquals(expected, actual)
        assertTrue(myjdbc.connect_to_database());
    }

    @Test
    void fill_member_data()
    {
        Member m = new Member();
        String id = "111112222";
        myjdbc.connect_to_database();
        myjdbc.fill_member_data(id,m);
        assertEquals("Count Chocula", m.name);
        assertEquals("321 Poof St", m.address);
        assertEquals("Salem", m.city);
        assertEquals("OR", m.state);
        assertEquals("95505", m.zip);
    }

    @Test
    void fill_provider_data()
    {
        Provider p = new Provider();
        String id = "111111111";
        myjdbc.connect_to_database();
        myjdbc.fill_provider_data(id, p);
        assertEquals("Bob Barker", p.name);
        assertEquals("1 Birch St", p.address);
        assertEquals("Salem", p.city);
        assertEquals("OR", p.state);
        assertEquals("65656", p.zip);

    }

    @Test
    void fill_service_data_good()
    {
        Service s = new Service();
        String good_serv_code = "656565";
        myjdbc.connect_to_database();
        myjdbc.fill_service_data(good_serv_code, s);
        assertEquals("Dynamic Chocoanalysis", s.name);
        assertEquals(277.77f, s.fee, 0.1f);
    }
    @Test
    void fill_service_data_invalid()
    {
        Service s = new Service();
        String bad_serv_code = "99";
        myjdbc.connect_to_database();
        myjdbc.fill_service_data(bad_serv_code, s);
        assertEquals("no-name", s.name); // default service value not changed
    }

    // VALIDATE PROVIDER
    // a test with valid (happy) provider id
    @Test
    void validate_provider_good()
    {
        String good_pid = "000000000";
        myjdbc.connect_to_database();
        // a valid provider returns 0
        assertEquals(0, myjdbc.validate_provider(good_pid));
    }
    // a test with invalid (bad) provider id
    @Test
    void validate_provider_bad_id()
    {
        String bad_pid = "123";
        myjdbc.connect_to_database();
        // an invalid provider id returns 2
        assertEquals(2, myjdbc.validate_provider(bad_pid));
    }
    // a test with a good provider id, but bad connection to database
    @Test
    void validate_provider_bad_conn()
    {
        String good_pid = "123456789";
        //myjdbc.conn
        myjdbc.end_connection();
        //myjdbc.connect_to_database();
        // a database problem returns 1
        assertEquals(1, myjdbc.validate_provider(good_pid));
    }

    // VALIDATE MEMBER
    @Test
    void validate_member_good()
    {
        String good_mid = "111112222";
        myjdbc.connect_to_database();
        // an valid member id returns 0
        assertEquals(0, myjdbc.validate_member(good_mid));
    }
    // this test actually uncovered a bug!
    @Test
    void validate_member_invalid_id()
    {
        String bad_mid = "1234";
        myjdbc.connect_to_database();
        // an invalid member id returns 3
        assertEquals(3, myjdbc.validate_member(bad_mid));
    }
    @Test
    void validate_member_suspended()
    {
        String sus_mid = "123456789";
        myjdbc.connect_to_database();
        // an invalid member id returns 3
        assertEquals(4, myjdbc.validate_member(sus_mid));
    } @Test
    void validate_member_bad_conn()
    {
        String good_mid = "111112222";
        //myjdbc.connect_to_database();
        myjdbc.end_connection();
        // a bad database conn returns 1
        assertEquals(1, myjdbc.validate_member(good_mid));
    }
}