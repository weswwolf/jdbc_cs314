public class Provider extends Personal
{
    public String provider_id;
    public int consultations;
    public float total_fee;

    /*
    Constructor
    output
    set
    get
     */
    public boolean same_id(String prov_id)
    {
        // same id
        return provider_id.equals(prov_id);
    }
    
}
