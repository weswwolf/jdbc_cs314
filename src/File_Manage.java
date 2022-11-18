import java.io.File;

//TODO Move the write_to_file function from myjdbc into this class

public class File_Manage
{
    public static void delete_file(String to_delete)
    {
        File delete = new File(to_delete);
        delete.delete();
    }
}
