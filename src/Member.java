public class Member extends Personal
{
    public String member_id;

    Member()
    {
        member_id = "no-id";
    }

    public void set_all_member(String name_, String address_, String city_, String state_, String zip_, String id_)
    {
        super.set_all_personal(name_, address_, city_, state_, zip_);
        member_id = id_;
    }

    public boolean same_id(String mem_id)
    {
        // same id
        return mem_id.equals(member_id);
    }
}