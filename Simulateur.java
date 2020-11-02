import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.stat.Tally;

import java.util.LinkedList;

public class Simulateur {
    static  Tally custWaits     = new Tally ("Waiting times");
    static  Accumulate totWait  = new Accumulate ("Size of queue");
    static  LinkedList<ClientA> waitListA = new LinkedList<ClientA> ();


    public static void main (String[] args) {
        double sigma_a = 60;
        double mu_a = 200;
        double sigma = Math.sqrt ( Math.log10 ( 1 + Math.pow (sigma_a/mu_a, 2) ) );
        double mu = Math.log10(Math.pow ( mu_a, 2 )/Math.sqrt ( Math.pow ( sigma_a, 2 ) + Math.pow ( mu_a, 2 ) ));


        RendezVous rendezVous = new RendezVous(1, 3, 3, 1);
        rendezVous.repartirConseiller();
        rendezVous.programmerRendezVous();


        PeriodeB periode_1 = new PeriodeB (4, 6, 100, 90, 0.05);
        periode_1.simulateOneRun (7200);
        System.out.println (Simulateur.custWaits.report());
        System.out.println (Simulateur.totWait.report());
    }
}
