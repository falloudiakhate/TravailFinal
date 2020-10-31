import com.sun.org.apache.xpath.internal.operations.Bool;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;

import java.util.*;

class RendezVous{

   public  int m1;
   public int m2;
   public  int m3;
   public double  r ;

   RandomStream stream = new MRG32k3a();


    public RendezVous(int m1, int m2, int m3, double r) {
        this.m1 = m1;
        this.m2 = m2;
        this.m3 = m3;
        this.r = r;

        init();
    }


    static ArrayList<String> conseillers = new ArrayList<String>();
    static Hashtable<Integer, String> plageHoraire = new Hashtable<Integer, String>();

    //waitList for every "Conseiller"

    static  Hashtable<String, LinkedList<ClientB>> waitListConseiller = new Hashtable<String, LinkedList<ClientB>>();

    //    It will be in this following format :
    //    {
    //        "C1" : client1 -- > client2 -- > clientn,
    //        "C2" : client1 -- > client2 -- > client3,
    //        "C3" : client1 -- > null,
    //
    //    }


    //Méthode pour définir les conseillers
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


    // La répartition des conseillers dans chaque plage
    static ArrayList<ArrayList<String>> repartitionConseillers = new ArrayList<ArrayList<String>>();
    static ArrayList<ClientB> clients = new ArrayList<ClientB>();

    // Contient les rendez vous de chaque conseiller
    static ArrayList<Hashtable<String, ArrayList<String>>> tab_rendez_vous = new ArrayList<Hashtable<String, ArrayList<String>>>();

    static Hashtable<String, Boolean> etatConseiller = new Hashtable<String, Boolean>();

    public void repartirConseiller() {
		/*
			On répartit chacun des conseillers dans les périodes ou ils travaillent
		*/

        for (int mi : new int[]{m1, m2, m3}){
            ArrayList<String> pi = new ArrayList<String>();
            for( int i=1; i < mi +1 ; i++){
                pi.add("C"+i);
            }
            repartitionConseillers.add(pi);
        }

    }

    public void programmerRendezVous() {
        //Ce tableau contiendra l'ensemble des rendez vous sous format
		/*
		[
			 {
				"C1" : [1, 0, 1, 0, ...]
			 },
			 {
				"C2" : [1, 0, 0, 0, 1, ...]
			 },
			 {
				 "C3": [0, 0, 0 ...]
			 }

		]
		*/
        for(String conseiller : conseillers) {
            int probabilite = 0;
            ArrayList<String> plages = definirPlage(conseiller);
            for(int i = 0; i < plages.size(); i++) {
                // On vérifie la probabilité r d'avoir un    System.out.println(client); rendez vous
                probabilite = stream.nextDouble() <= r ? 1 : 0;
                if(probabilite == 1) {
                    // Puis on ajoute le client à la liste de client avec un rendez vous
                    ClientB client = new ClientB();
                    client.conseiller = conseiller;
                    client.plage = plages.get(i);
                    clients.add(client);

                }
                else {
                    // Il aura ou pas un rendez vous à la plage i
                    plages.set(i, null);
                }
                // We create the waitlist of this 'conseiller'
                waitListConseiller.put(conseiller, new LinkedList<ClientB>());

                // We set the busyness of the 'conseiller'
                // It will change every time we have an arrival or a departure
                etatConseiller.put(conseiller, false); // false --> pas occupe
            }
            // Fais correspondre le conseiller avec ses horaires de rendez vous
            Hashtable<String, ArrayList<String>> meeting = new Hashtable<String, ArrayList<String>>();
            meeting.put(conseiller, plages);
            tab_rendez_vous.add(meeting);
        }
    }


    ArrayList<String> definirPlage(String conseiller) {
		/*
			Définir les plages de chaque conseiller pour les rendez-vous
		 */
        ArrayList<String> plage = new ArrayList<String>();

        // Si le coneiller est dans une période donnée, les plages correspondantes sont ajoutées

        if(RendezVous.repartitionConseillers.get(0).contains(conseiller)) {
            for(int i = 1; i < 5; i++) plage.add(plageHoraire.get(i));
        }
        if(RendezVous.repartitionConseillers.get(1).contains(conseiller)) {
            for(int i = 5; i < 9; i++) plage.add(plageHoraire.get(i));
        }
        if(RendezVous.repartitionConseillers.get(2).contains(conseiller)) {
            for(int i = 9; i < 13; i++) plage.add(plageHoraire.get(i));
        }
        return plage;


    }


    public void StringRV() {
        for(ClientB cl : clients){
            System.out.println("Un Client C a un Rendez-Vous avec le Conseiller " +cl.conseiller+ " à "+cl.plage);
        }
    };
    public static void main (String[] args) {
        RendezVous rendezVous = new RendezVous(2, 3, 3, 0.8);

        rendezVous.repartirConseiller();
        rendezVous.programmerRendezVous();
        rendezVous.StringRV();


//        System.out.println(RendezVous.tab_rendez_vous.toString());
//        System.out.println(RendezVous.clients.get(1).conseiller.toString());
//        System.out.println(RendezVous.clients.get(1).plage);
    }
}