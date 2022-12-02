import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
/*
    void delete_file(String to_delete)
    void delete_all_files()
    void write_to_file(String file_name, String service_details, String initial_details)

 */
class File_ManageTest {

    @Test
    void delete_file() {
        String file = "to_delete";
        File to_delete = new File(file);
        File_Manage.write_to_file(file, "add this text only if the file already exists", "Only add this text if the file didn't exist yet\n");
        File_Manage.write_to_file(file, "add this text only if the file already exists", "this text should not be added\n");
        File_Manage.delete_file(file);
        // Since the file has been deleted, the FILE delete function should return false
        assertFalse(to_delete.delete());
    }

    @Test
    void delete_all_files() {

        //myjdbc.weekly_services();

    }

    @Test
    void write_to_file() {
        // write to a file that doesn't exist, then make sure that it exists
        // write_to_file(file_name, append_details, initial_details)
        String file_name = "junit_test_file_actual";
        File_Manage.write_to_file(file_name, "add this text only if the file already exists", "Only add this text if the file didn't exist yet\n");
        File_Manage.write_to_file(file_name, "add this text only if the file already exists", "this text should not be added\n");
        File actual = new File(file_name); // actual
        //File expected = new File("junit_test_file_expected");
        assertTrue(actual.exists());
        // assert that the actual output to the file is the same as expected
        // by reading the file text into a string and comparing it to the expected output.
        // make sure to destroy the file if it already exists, or it will not match correctly.
    }

    @Test
    void append_summary_report(){
        //Assert that append_summary_report() does not allow writing bad provider data:
        Provider p = null;
        assertEquals(1, File_Manage.append_summary_report(p));
    }
}