import umontreal.ssj.probdist.LognormalDist;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.randvar.ExponentialGen;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;

import java.util.LinkedList;

public class Journee {

    public double mu;
    public double sigma;

    public double mu_r;
    public double sigma_r;

    int nbCaissiers;

    RandomVariateGen genArrA;
    RandomVariateGen genServA;
    RandomVariateGen retard ;
    RandomVariateGen genServB;

    LinkedList<Client> servList = new LinkedList<Client> ();

    double p;  // The probability for the client to be not present
    // LinkedList<Client> servList = new LinkedList<Client>();

    RandomStream stream = new MRG32k3a();  // Generate the probability for the client to be not present


    public Journee (double sigma_a, double mu_a, double lambda, int nbCaisiers, double sigma_b, double mu_b, double mu_r, double sigma_r, double p) {
        genArrA = new ExponentialGen (new MRG32k3a(), lambda);
        genServA = new RandomVariateGen (new MRG32k3a(), new LognormalDist (mu, sigma));
        this.nbCaissiers = nbCaisiers;

        retard = new RandomVariateGen (new MRG32k3a(), new NormalDist(mu_r, sigma_r));
        genServB = new RandomVariateGen (new MRG32k3a(), new LognormalDist(mu_b, sigma_b));
        this.p = p;
    }



    //    Looping over the clients to schedule their arrivals
    public void genArrB(){
        /*
        Method for scheduling the rendez-vous
        We first check the probability for the client not to be present
        If he will be present, we schedule the RV with a certain lateness
         */
        for(Client cl : RendezVous.clients){
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



    class Arrival extends Event {

        // Cust just arrived.
        Client client ;
        public Arrival(Client comming){
            this.client = comming;
        }

        public void actions() {
            client.arrivTime = Sim.time();
            client.servTime = genServB.nextDouble();
            // Check if the conseiller is free
            // if true, he enters in the waitlist of the conseiller
            // else the client enters in service
            if(RendezVous.etatConseiller.get(client.conseiller)==true){
                // We add the client in the waitlist of the conseiller
                RendezVous.waitListConseiller.get(client.conseiller).addLast(client);
                // We update the totwait by adding the size of the waitlist of the client
                Simulateur.totWait.update (RendezVous.waitListConseiller.get(client.conseiller).size());
            }
            else{
                // Starts service
                Simulateur.custWaits.add (0.0);
                RendezVous.etatConseiller.put(client.conseiller, true);
                new Departure(client).schedule (client.servTime);
                // System.out.println(RendezVous.etatConseiller.toString());
            }
        }
    }


    class Departure extends Event {
        Client Client;
        public Departure(Client Client) {
            this.Client = Client;
        }

        public void actions() {
            RendezVous.etatConseiller.put(Client.conseiller, false);

            // Starts service for next one in queue.
            // If there is a client in the stack of the 'conseiller', we will take him.
            if(RendezVous.waitListConseiller.get(this.Client.conseiller).size() > 0){
                Client Client = RendezVous.waitListConseiller.get(this.Client.conseiller).removeFirst();
                Simulateur.totWait.update (RendezVous.waitListConseiller.get(this.Client.conseiller).size());
                Simulateur.custWaits.add (Sim.time() - Client.arrivTime);
                RendezVous.etatConseiller.put(Client.conseiller, true);

                new Journee.Departure(Client).schedule(Client.servTime);
            }

        }
    }

    class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }
}
