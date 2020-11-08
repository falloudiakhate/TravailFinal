import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;

import java.util.*;

class Plannificator{

    /***
     * The Plannificator Class parameters
     */
    public  double n1;
    public double n2;
    public  double n3;
    public  double m1;
    public double m2;
    public  double m3;
    public double  r ;

   RandomStream stream = new MRG32k3a();

    /***
     * The Constructor of the Plannificator Class
     * @param n1
     * @param n2
     * @param n3
     * @param m1
     * @param m2
     * @param m3
     * @param r
     */
    public Plannificator(double n1, double n2, double n3, double m1, double m2, double m3, double r) {
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;

        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
        this.r = r;

        init();
    }


    static ArrayList<String> conseillers = new ArrayList<String>();
    static Hashtable<Integer, String> plageHoraire = new Hashtable<>();

    /***
     * waitList for every "Conseiller"
     */

    static  Hashtable<String, LinkedList<Client>> waitListConseiller = new Hashtable<String, LinkedList<Client>>();

    /***
     *      It will be in this following format :
     *         {
     *             "C1" : client1 -- > client2 -- > clientn,
     *             "C2" : client1 -- > client2 -- > client3,
     *             "C3" : client1 -- > null,
     *
     *        }
     */


    /***
     * Méthode pour définir les conseillers
     */
    public void init(){
        for(Integer i = 1; i<Math.max(Math.max(m1, m2), m3) + 1; i++){
            conseillers.add("C"+i.toString());
        }

        plageHoraire.put(1,"10:00");
        plageHoraire.put(2,"10:30");
        plageHoraire.put(3,"11:00");
        plageHoraire.put(4,"11:30");
        plageHoraire.put(5,"12:00");
        plageHoraire.put(6,"12:30");
        plageHoraire.put(7,"13:00");
        plageHoraire.put(8,"13:30");
        plageHoraire.put(9,"14:00");
        plageHoraire.put(10,"14:30");
        plageHoraire.put(11,"15:00");
        plageHoraire.put(12,"15:30");

    }


    /***
     * La répartition des conseillers dans chaque plage
     */
    static ArrayList<ArrayList<String>> repartitionConseillers = new ArrayList<ArrayList<String>>();

    static ArrayList<ArrayList<String>> repartitionCaissiers = new ArrayList<ArrayList<String>>();

    static ArrayList<Client> clients = new ArrayList<Client>();

    /***
     * Contient les rendez vous de chaque conseiller
     */
    static ArrayList<Hashtable<String, ArrayList<String>>> tab_rendez_vous = new ArrayList<Hashtable<String, ArrayList<String>>>();

    static Hashtable<String, Boolean> etatConseiller = new Hashtable<String, Boolean>();

    public void repartirConseiller() {
        /***
         * On répartit chacun des conseillers dans les périodes ou ils travaillent
         */
        for (double mi : new double[]{m1, m2, m3}){
            ArrayList<String> pi = new ArrayList<String>();
            for( int i=1; i < mi +1 ; i++){
                pi.add("C"+i);
            }
            repartitionConseillers.add(pi);
        }

    }

    public void repartirCaissiers() {
        /***
         * On répartit chacun des caissiers dans les périodes ou ils travaillent
         */
        for (double ni : new double[]{n1, n2, n3}){
            ArrayList<String> pi = new ArrayList<String>();
            for( int i=1; i < ni +1 ; i++){
                pi.add("c"+i);
            }
            repartitionCaissiers.add(pi);
        }

    }
    public void programmerRendezVous() {
        /***
         *  Ce tableau contiendra l'ensemble des rendez vous sous format
         *
         * 		[
         *                          {
         * 				"C1" : [1, 0, 1, 0, ...]
         *             },
         *             {
         * 				"C2" : [1, 0, 0, 0, 1, ...]
         *             },
         *             {
         * 				 "C3": [0, 0, 0 ...]
         *             }
         *
         * 		]
         *
         */
        for(String conseiller : conseillers) {
            int probabilite = 0;
            ArrayList<String> plages = definirPlage(conseiller);
            for(int i = 0; i < plages.size(); i++) {
                /**
                 * On vérifie la probabilité r d'avoir un    System.out.println(client); rendez vous
                 */
                probabilite = stream.nextDouble() <= r ? 1 : 0;
                if(probabilite == 1) {
                    /**
                     * Puis on ajoute le client à la liste de client avec un rendez vous
                     */
                    Client client = new Client();
                    client.conseiller = conseiller;
                    client.plage = plages.get(i);
                    clients.add(client);
                    /**
                     * oui for saying he has a RV on this plage
                     */
                    plages.set(i, plages.get(i) + " oui");

                }
                else {
                    /***
                     * Il aura ou pas un rendez vous à la plage i
                     * non for saying he hasn't a RV on this plage
                     */
                    plages.set(i, plages.get(i) + " non");
                }
                /***
                 * We create the waitlist of this 'conseiller'
                 */
                waitListConseiller.put(conseiller, new LinkedList<Client>());

                /***
                 * We set the busyness of the 'conseiller'
                 * It will change every time we have an arrival or a departure
                 */
                etatConseiller.put(conseiller, false); // false --> pas occupe
            }
            /***
             * Fais correspondre le conseiller avec ses horaires de rendez vous
             */
            Hashtable<String, ArrayList<String>> meeting = new Hashtable<String, ArrayList<String>>();
            meeting.put(conseiller, plages);
            tab_rendez_vous.add(meeting);
        }
    }


    ArrayList<String> definirPlage(String conseiller) {
        /***
         * Définir les plages de chaque conseiller pour les rendez-vous
         */
        ArrayList<String> plage = new ArrayList<String>();

        /***
         * Si le coneiller est dans une période donnée, les plages correspondantes sont ajoutées
         */
        if(Plannificator.repartitionConseillers.get(0).contains(conseiller)) {
            for(int i = 1; i <4 ; i++) plage.add(plageHoraire.get(i));
        }
        if(Plannificator.repartitionConseillers.get(1).contains(conseiller)) {
            for(int i = 4; i < 8; i++) plage.add(plageHoraire.get(i));
        }
        if(Plannificator.repartitionConseillers.get(2).contains(conseiller)) {
            for(int i = 8; i < 12; i++) plage.add(plageHoraire.get(i));
        }
        return plage;


    }

    /***
     * toString() RV
     */
    public void StringRV() {
        for(Client cl : clients){
            System.out.println("Un Client C a un Rendez-Vous avec le Conseiller " +cl.conseiller+ " à "+cl.plage);
        }
    };

    /***
     * The main Function
     * @param args
     */
    public static void main (String[] args) {
        Plannificator rendezVous = new Plannificator(3, 4, 3, 2, 3, 3, 0.8);
        rendezVous.repartirConseiller();

        rendezVous.programmerRendezVous();
        rendezVous.StringRV();


    }

}