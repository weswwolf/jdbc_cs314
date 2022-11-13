public class Member extends Personal
{
    public String member_id;

    /*
    constructor
    output
    get
    set
     */
    public boolean same_id(String prov_id)
    {
        // same id
        return prov_id.equals(member_id);
    }
}