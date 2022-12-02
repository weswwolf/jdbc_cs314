/****
 * These classes are not part of the requirements, but we had already started work on them prior to learning that.
 *
 ****/

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.io.*;

/*
    void add_to_database()
    void write_member_reports()
    void write_provider_reports()
    void write_summary_report()
 */



class SmokeTest {
    @Test
    void add_to_database(){
        myjdbc.connect_to_database();
        int init_count = myjdbc.get_count_wkly_svc();
        Service test_service = new Service(LocalDate.now(), "112233445", "123456788", "101010", "Test for new service");
        myjdbc.insert_service_record(test_service);
        myjdbc.insert_service_record(test_service);
        myjdbc.insert_service_record(test_service);
        int final_count = myjdbc.get_count_wkly_svc();
        assertEquals(init_count, final_count - 3);
    }

    @Test
    void write_provider_reports() throws SQLException {
        String query = "select * from Providers";
        File rpt;
        myjdbc.connect_to_database();
        Provider prov = new Provider();
        String prov_id = null;
        Statement stmt = myjdbc.conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()){
            prov_id = rs.getString("id");
            myjdbc.generate_individual_provider_report(prov_id);
            myjdbc.fill_provider_data(prov_id, prov);
            rpt = new File(prov.name.replaceAll( " ", "") + "-" + prov_id);
            assertTrue(rpt.exists());
        }
    }

    @Test
    void write_summary_report(){

    }
}
