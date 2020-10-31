import umontreal.ssj.probdist.LognormalDist;
import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.randvar.RandomVariateGen;
import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.simevents.Event;
import umontreal.ssj.simevents.Sim;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;


import java.util.LinkedList;

public class PeriodeB {
    public double mu;
    public double sigma;

    public double mu_r;
    public double sigma_r;

    RandomVariateGen retard ;
    RandomVariateGen genServB;
    LinkedList<ClientB> servList = new LinkedList<ClientB>();


    public PeriodeB (double sigma, double mu, double mu_r, double sigma_r) {
        retard = new RandomVariateGen (new MRG32k3a(), new NormalDist(mu_r, sigma_r));
        genServB = new RandomVariateGen (new MRG32k3a(), new LognormalDist(mu, sigma));
    }


//    For converting arrival date of the client to seconds
    public static int convertTimeInSecond(String time){
        String[] units = time.split(":"); //will break the string up into an array
        int heures = Integer.parseInt(units[0]); //first element
        int minutes = Integer.parseInt(units[1]); //second element
        int duration =  3600 * heures  + 60*minutes - 10*3600; //add up our values
        return duration;
    }

//    Looping over the clients to schedule their arrivals
    public void genArrB(){
        for(ClientB cl : RendezVous.clients){
            int arrivalTime = convertTimeInSecond(cl.plage) + (int)retard.nextDouble();
            new PeriodeB.Arrival(cl).schedule ( arrivalTime );
        }
    }



    public void simulateOneRun (double timeHorizon) {
        Sim.init();
        new PeriodeB.EndOfSim().schedule (timeHorizon);
        Sim.start();
    }


    class Arrival extends Event {

        // Cust just arrived.
        ClientB clientB ;
        public Arrival(ClientB clientB){
            this.clientB = clientB;
        }

        public void actions() {

            clientB.arrivTime = Sim.time();
            clientB.servTime = genServB.nextDouble();
            // Check if the conseiller is free
            // if true, he enters in the waitlist of the conseiller
            // else the client enters in service

            if(RendezVous.etatConseiller.get(clientB.conseiller)){
                // We add the client in the waitlist of the conseiller
                RendezVous.waitListConseiller.get(clientB.conseiller).addLast(clientB);

                // We update the totwait by adding the size of the waitlist of the client
                Simulateur.totWait.update (RendezVous.waitListConseiller.get(clientB.conseiller).size());
            }
            else{
                // Starts service.
                Simulateur.custWaits.add (0.0);
                RendezVous.etatConseiller.put(clientB.conseiller, true);
                new PeriodeB.Departure(clientB).schedule (clientB.servTime);
            }
        }
    }


    class Departure extends Event {
        ClientB clientB;
        public Departure(ClientB clientB) {
            this.clientB = clientB;
        }

        public void actions() {
            if (RendezVous.etatConseiller.get(clientB.conseiller) == false) {
                // Starts service for next one in queue.
                ClientB clientB = RendezVous.waitListConseiller.get(this.clientB.conseiller).pop();
                Simulateur.totWait.update (RendezVous.waitListConseiller.get(this.clientB.conseiller).size());
                Simulateur.custWaits.add (Sim.time() - clientB.arrivTime);

                new PeriodeB.Departure(clientB).schedule(clientB.servTime);
            }
        }
    }

    class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }

}
