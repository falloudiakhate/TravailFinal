import umontreal.ssj.probdist.LognormalDist;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.rng.RandomStream;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import java.util.LinkedList;


import java.util.LinkedList;

public class PeriodeB {
    public double mu;
    public double sigma;

    public double mu_r;
    public double sigma_r;

    RandomVariateGen retard ;
    RandomVariateGen genServB;
    double p;  // The probability for the client to be not present
    // LinkedList<Client> servList = new LinkedList<Client>();

    RandomStream stream = new MRG32k3a();  // Generate the probability for the client to be not present


    public PeriodeB (double sigma_b, double mu_b, double mu_r, double sigma_r, double p) {
        retard = new RandomVariateGen (new MRG32k3a(), new NormalDist(mu_r, sigma_r));
        genServB = new RandomVariateGen (new MRG32k3a(), new LognormalDist(mu_b, sigma_b));
        this.p = p;
    }


//    For converting arrival date of the client to seconds
    public static int convertTimeInSecond(String time){
        int MINUTE = 60;
        int HOUR = 3600;
        String[] units = time.split(":"); //will break the string up into an array
        int heures = Integer.parseInt(units[0]); //first element
        int minutes = Integer.parseInt(units[1]); //second element
        int duration =  HOUR * heures  + MINUTE*minutes - 10*HOUR; //add up our values
        return duration;
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
                int arrivalTime = convertTimeInSecond(cl.plage) + (int)retard.nextDouble();
                new PeriodeB.Arrival(cl).schedule ( arrivalTime );
            }

        }
    }


    public void simulateOneRun (double timeHorizon) {
        Sim.init();
        new PeriodeB.EndOfSim().schedule (timeHorizon);
        genArrB();
        Sim.start();
    }



    class Arrival extends Event {

        // Cust just arrived.
        Client Client ;
        public Arrival(Client Client){
            this.Client = Client;
        }

        public void actions() {
            Client.arrivTime = Sim.time();
            Client.servTime = genServB.nextDouble();
            // Check if the conseiller is free
            // if true, he enters in the waitlist of the conseiller
            // else the client enters in service
            if(RendezVous.etatConseiller.get(Client.conseiller)==true){
                // We add the client in the waitlist of the conseiller
                RendezVous.waitListConseiller.get(Client.conseiller).addLast(Client);
                // We update the totwait by adding the size of the waitlist of the client
                Simulateur.totWait.update (RendezVous.waitListConseiller.get(Client.conseiller).size());
            }
            else{
                // Starts service
                Simulateur.custWaits.add (0.0);
                RendezVous.etatConseiller.put(Client.conseiller, true);
                new PeriodeB.Departure(Client).schedule (Client.servTime);
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

                    new PeriodeB.Departure(Client).schedule(Client.servTime);
                }

        }
    }

    class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }

}
