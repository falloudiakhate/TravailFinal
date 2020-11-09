package travail_final;
public class Client{
    double arrivTime;
    double servTime;
    char type;
    /**
     *   Specific to client of type 
     */
    String conseiller = "";
    String plage;
    String type_serveur;
    
    public Client(char type) {
    	this.type = type;
    }
}
