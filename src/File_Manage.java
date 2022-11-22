import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

//TODO Move the write_to_file function from myjdbc into this class

public class File_Manage
{
    public static void delete_file(String to_delete)
    {
        File d_delete = new File(to_delete);
        if(d_delete.delete())
            System.out.println(to_delete + " Deleted");
    }

    public static void delete_all_files()
    {
        ArrayList<Member> m;
        ArrayList<Provider> p;
        m = myjdbc.get_all_members();
        p = myjdbc.get_all_providers();

        //interate through array list and do the following for each
        for (Member member : m)
        {
            String file_name = member.name.replaceAll("\\s", "").concat("-" + member.member_id);
            File_Manage.delete_file(file_name);
        }
//        String file_name = n.name.replaceAll("\\s", "").concat("-" + mem_id);
        for (Provider provider : p)
        {
            String file_name = provider.name.replaceAll("\\s", "").concat("-" + provider.provider_id);
            File_Manage.delete_file(file_name);
        }
//        File_Manage.delete_file(file_name);
    }

    static void write_to_file(String file_name, String service_details, String initial_details)
    {
        try
        {
            File output_file = new File(file_name); // get rid of spaces in the name for the file
            if (!output_file.exists())
            {
                // member file doesn't exist. we will make a new file
                //System.out.println("generating new file for member report...");
                // write to the file only the initial part -- maybe there is a better way but this currently works
                FileWriter fw = new FileWriter(file_name, true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(initial_details);
                bw.close();
                fw.close();
            }

            // open another writer to write only the service information part
            FileWriter fw = new FileWriter(file_name, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(service_details);
            bw.newLine();
            bw.close();
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
