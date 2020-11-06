import umontreal.ssj.simevents.Accumulate;
import umontreal.ssj.stat.Tally;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Simulateur {
    static  Tally custWaitsA    = new Tally ("Waiting times of Client A");
    static  Tally custWaitsB   = new Tally ("Waiting times of Client B");
    static  Accumulate totWait  = new Accumulate ("Size of queue");
    static  LinkedList<Client> waitListA = new LinkedList<Client> ();





    public static void main (String[] args) {



        // Parameters of genServA sigma and mu
        double sigma = Math.sqrt ( Math.log10 ( 1 + Math.pow (sigma_a/mu_a, 2) ) );
        double mu = Math.log10(Math.pow ( mu_a, 2 )/Math.sqrt ( Math.pow ( sigma_a, 2 ) + Math.pow ( mu_a, 2 ) ));
        int [] lambdas = {20, 35, 28};

        Plannificator rendezVous = new Plannificator(3, 4, 3, 2, 3, 3, 1);
        rendezVous.repartirConseiller();
        rendezVous.repartirCaissiers();

        rendezVous.programmerRendezVous();


        Journee journee_1 = new Journee (sigma, mu, lambdas, 4, 5, 8, 100, 90, 0.05, 10*60);
        journee_1.simulateOneRun (7200 * 3);

        System.out.println (Simulateur.custWaitsA.report());
        System.out.println (Simulateur.custWaitsB.report());
        System.out.println (Simulateur.totWait.report());
    }
}
