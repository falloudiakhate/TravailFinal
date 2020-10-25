import umontreal.ssj.probdist.LognormalDist;
import umontreal.ssj.simevents.*;
import umontreal.ssj.rng.*;
import umontreal.ssj.randvar.*;
import umontreal.ssj.stat.*;
import java.util.LinkedList;

public class Periode {

    int nbCaissiers;

    RandomVariateGen genArrA;
    RandomVariateGen genServA;
    LinkedList<CustomerA> servList = new LinkedList<CustomerA> ();



    public Periode (double sigma, double mu, double lambda, int nbCaisiers) {
        genArrA = new ExponentialGen (new MRG32k3a(), lambda);
        genServA = new RandomVariateGen (new MRG32k3a(), new LognormalDist (mu, sigma));
        this.nbCaissiers = nbCaisiers;
    }

    public void simulateOneRun (double timeHorizon) {
        Sim.init();
        new EndOfSim().schedule (timeHorizon);
        new Arrival().schedule (genArrA.nextDouble());
        Sim.start();
    }

    class Arrival extends Event {
        public void actions() {
            new Arrival().schedule (genArrA.nextDouble()); // Next arrival.
            CustomerA cust = new CustomerA();  // Cust just arrived.
            cust.arrivTime = Sim.time();
            cust.servTime = genServA.nextDouble();
            if (servList.size() > nbCaissiers + 1) {       // Must join the queue.
                Simulateur.waitList.addLast (cust);
                Simulateur.totWait.update (Simulateur.waitList.size());
            } else {                         // Starts service.
                Simulateur.custWaits.add (0.0);
                servList.addLast (cust);
                new Departure().schedule (cust.servTime);
            }
        }
    }

    class Departure extends Event {
        public void actions() {
            servList.removeFirst();
            if (Simulateur.waitList.size() > 0) {
                // Starts service for next one in queue.
                CustomerA cust = Simulateur.waitList.removeFirst();
                Simulateur.totWait.update (Simulateur.waitList.size());
                Simulateur.custWaits.add (Sim.time() - cust.arrivTime);
                servList.addLast (cust);
                new Departure().schedule (cust.servTime);
            }
        }
    }

    class EndOfSim extends Event {
        public void actions() {
            Sim.stop();
        }
    }

}
