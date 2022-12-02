/****
 * These classes are not part of the requirements, but we had already started work on them prior to learning that.
 *
 ****/


import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
/*
    void view_service_directory()
    void generate_eft()
    void write_individual_reports()
 */
class SystemTest {

    @Test
    void view_service_directory(){
        ArrayList<Service> list;
        list = myjdbc.get_service_directory();
        assertNotEquals(null, list);
        assertEquals(list.size(), myjdbc.get_count_serv_dir());
    }

    @Test
    void validate_member(){
        myjdbc.connect_to_database();
        assertEquals(0, myjdbc.validate_member("111112222"));
        assertEquals(4, myjdbc.validate_member("123456789"));
        assertEquals(3, myjdbc.validate_member("20"));
    }

}