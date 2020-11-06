import umontreal.ssj.probdist.LognormalDist;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;

import java.util.*;

public class Journee {

    public double mu;
    public double sigma;

    public double mu_r;
    public double sigma_r;

    int nbCaissiers;
    int s;

    static int currentPeriode = 1; // For storing the actual periode

    RandomVariateGen genArrA;
    RandomVariateGen genServA;
    RandomVariateGen retard ;
    RandomVariateGen genServB;
    int [] lambdas;
    LinkedList<Client> servList = new LinkedList<Client> ();

    double p;  // The probability for the client to be not present
    // LinkedList<Client> servList = new LinkedList<Client>();

    RandomStream stream = new MRG32k3a();  // Generate the probability for the client to be not present


    public Journee (double sigma_a, double mu_a, int [] lambdas, int nbCaisiers, double sigma_b, double mu_b, double mu_r, double sigma_r, double p, int s) {
        this.lambdas = lambdas;
        genArrA = new ExponentialGen (new MRG32k3a(), lambdas[currentPeriode - 1]);
        genServA = new RandomVariateGen (new MRG32k3a(), new LognormalDist (mu_a, sigma_a));
        this.nbCaissiers = nbCaisiers;

        retard = new RandomVariateGen (new MRG32k3a(), new NormalDist(mu_r, sigma_r));
        genServB = new RandomVariateGen (new MRG32k3a(), new LognormalDist(mu_b, sigma_b));
        this.p = p;
        this.s = s;
    }



    //    Looping over the clients to schedule their arrivals
    public void genArrB(){
        /*
        Method for scheduling the rendez-vous
        We first check the probability for the client not to be present
        If he will be present, we schedule the RV with a certain lateness
         */
        for(Client cl : Plannificator.clients){
            final boolean present = stream.nextDouble() <= p ? false : true;
            if(present){
                int arrivalTime = PeriodeB.convertTimeInSecond(cl.plage) + (int)retard.nextDouble();
                new Journee.Arrival(cl).schedule ( arrivalTime );
            }

        }
    }


    public void simulateOneRun (double timeHorizon) {
        Sim.init();
        new Journee.EndOfSim().schedule (timeHorizon);
        new Journee.Arrival(new Client()).schedule (genArrA.nextDouble());
        genArrB();
        Sim.start();
    }

    public String comparateur (double simulation_time){
        /*
        Take every conseiller int the simulation
        Check if the conseiller is free
        If it s the case, check he has a RV in s second
        If No return this conseiller
        In the time where this function is called, if the return value is conseiller, the conseiller returned will take the client
         */
      for(int i = 0; i < Plannificator.tab_rendez_vous.size(); i ++){
          Hashtable<String, ArrayList<String>> plagesConseillers = Plannificator.tab_rendez_vous.get(i);
          String conseiller = plagesConseillers.keySet().iterator().next();

          //  For every conseiller, we check if the conseiller is free and if the conseiller belongs to this actual periode
          for (int j = 0;
               (j < plagesConseillers.get(conseiller).size()) &&
               (!Plannificator.etatConseiller.get(conseiller)) &&  // Free ?
               Plannificator.repartitionConseillers.get(currentPeriode -1).contains(conseiller); // Belongs to this periode ?
               j ++)
          {
              if(PeriodeB.convertTimeInSecond(plagesConseillers.get(conseiller).get(j).split(" ")[0]) - simulation_time > s){
                  return conseiller;
              }
          }
      }
      return null;
    }

    class Arrival extends Event {

        // Cust just arrived.

        Client client  ;
        public Arrival(Client client){
            this.client = client;
        }

        public void actions() {

            client.arrivTime = Sim.time();

            if(client.conseiller == ""){ // If the arrived client is an A type
                new Arrival(new Client()).schedule (genArrA.nextDouble()); // Next arrival.
                client.servTime = genServA.nextDouble();

                String conseiller = comparateur(Sim.time()); // Check if the returned conseiller dont have a RV in s seconds


                // Check first if the client can enter the List of service
                if(servList.size() < Plannificator.repartitionCaissiers.get(currentPeriode - 1).size() + 1){
                    //Start Service
                    Simulateur.custWaitsA.add (0.0);
                    servList.addLast (client);
                    client.type_serveur = "caissier";

                    new Journee.Departure(client).schedule (client.servTime);
                }

                // If not the case, cherching for a free conseiller ...
                else if(conseiller != null && Simulateur.waitListA.size() == 0){
                    Simulateur.custWaitsA.add (0.0);
                    Plannificator.etatConseiller.put(conseiller, true); // The conseiler become occupied because he has taken a new client of type A
                    client.type_serveur = "conseiller";
                    client.conseiller = conseiller;
                    new Departure(client).schedule (client.servTime);
                }
                else {
                    Simulateur.waitListA.addLast(client);
                    Simulateur.totWait.update (Simulateur.waitListA.size());
                }
            }

            else{ // We have a client of type B
                // Check if the conseiller is free
                // if true, he enters in the waitlist of the conseiller
                // else the client enters in service

                client.servTime = genServB.nextDouble();

                if(Plannificator.etatConseiller.get(client.conseiller)) {
                    // We add the client in the waitlist of the conseiller
                    Plannificator.waitListConseiller.get(client.conseiller).addLast(client);

                    // We update the totwait by adding the size of the waitlist of the client
                    Simulateur.totWait.update(Plannificator.waitListConseiller.get(client.conseiller).size());
                }
                else {
                    // Starts service

                    Simulateur.custWaitsB.add (0.0);
                    Plannificator.etatConseiller.put(client.conseiller, true);
                    new Departure(client).schedule (client.servTime);
                }
            }

        }
    }


    class Departure extends Event {
        Client client;
        public Departure(Client client) {
            this.client = client;
        }

        public void actions() {

            if (client.conseiller == "") { // If the arrived client is an A type
                if(client.type_serveur == "caissier"){
                    servList.removeFirst();
                    if (Simulateur.waitListA.size() > 0) {
                        // Starts service for next one in queue.
                        Client cust = Simulateur.waitListA.removeFirst();
                        Simulateur.totWait.update (Simulateur.waitListA.size());
                        Simulateur.custWaitsA.add (Sim.time() - cust.arrivTime);
                        servList.addLast (cust);
                        new Departure(client).schedule (cust.servTime);
                    }
                }
                else{
                    if(Plannificator.waitListConseiller.get(this.client.conseiller).size() > 0){
                        Client client = Plannificator.waitListConseiller.get(this.client.conseiller).removeFirst();
                        Simulateur.totWait.update (Plannificator.waitListConseiller.get(this.client.conseiller).size());
                        Simulateur.custWaitsB.add (Sim.time() - client.arrivTime);
                        Plannificator.etatConseiller.put(client.conseiller, true);

                        new Departure(client).schedule(client.servTime);
                    }
                }

            }
            else {

                if(Plannificator.waitListConseiller.get(this.client.conseiller).size() > 0) {
                    Client client = Plannificator.waitListConseiller.get(this.client.conseiller).removeFirst();
                    Simulateur.totWait.update(Plannificator.waitListConseiller.get(this.client.conseiller).size());
                    Simulateur.custWaitsB.add(Sim.time() - client.arrivTime);
                    Plannificator.etatConseiller.put(client.conseiller, true);

                    new Departure(client).schedule(client.servTime);
                }
                else{
                    Plannificator.etatConseiller.put(client.conseiller, false);
                }
            }
        }
    }

    class NextPeriode extends Event {
        /*
        It is for changing the current periode every 2 hours
        */
        @Override
        public void actions() {
            Journee.currentPeriode ++;
            genArrA = new ExponentialGen (new MRG32k3a(), lambdas[currentPeriode - 1]);

            if(currentPeriode < 3) new NextPeriode().schedule(7200);
        }
    }
    class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }
}
