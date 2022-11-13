public class Provider extends Personal
{
    public String provider_id;
    public int consultations;
    public float total_fee;

    Provider()
    {
        provider_id = "no-id";
        consultations = 0;
        total_fee = 0.0f;
    }

    //function that sets the data members of a provider
    void set_all_provider(String name_, String address_, String city_, String state_, String zip_, String id_)
    {
        super.set_all_personal(name_, address_, city_, state_, zip_);
        provider_id = id_;
    }

    //function that increments a consultation and adds the cost of the service to the total_fee of the provider
    void add_consult(Service s_)
    {
        ++consultations;
        total_fee += s_.fee;
    }

    //returns a string with the name of the provider, total fees and consultations
    public String fee_and_consult() { return name + '\t' + consultations + '\t' + total_fee; }

    public boolean same_id(String prov_id)
    {
        // same id
        return provider_id.equals(prov_id);
    }
    
}
