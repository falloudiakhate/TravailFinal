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

    static  double λ1 = ReadData.readData().get(0);
    static  double λ2 = ReadData.readData().get(1);
    static  double λ3 = ReadData.readData().get(2);
    static  double n1 = ReadData.readData().get(3);
    static  double n2 =  ReadData.readData().get(4);
    static  double n3 = ReadData.readData().get(5);
    static  double m1 = ReadData.readData().get(6);
    static  double m2 = ReadData.readData().get(7);
    static  double m3 = ReadData.readData().get(8);
    static  double r = ReadData.readData().get(9);
    static  double μa = ReadData.readData().get(10);
    static  double σa = ReadData.readData().get(11);
    static  double μb = ReadData.readData().get(12);
    static  double σb = ReadData.readData().get(13);
    static  double μr = ReadData.readData().get(14);
    static  double σr = ReadData.readData().get(15);
    static  double p = ReadData.readData().get(16);
    static  double s = ReadData.readData().get(17);
    static  double n = ReadData.readData().get(18);

    public static void main (String[] args) {


        // Parameters of genServA sigma and mu
        double sigma = Math.sqrt ( Math.log10 ( 1 + Math.pow (σa/μa, 2) ) );
        double mu = Math.log10(Math.pow ( μa, 2 )/Math.sqrt ( Math.pow ( σa, 2 ) + Math.pow ( μa, 2 ) ));
        double [] lambdas = {λ1, λ2, λ3};

        Plannificator rendezVous = new Plannificator(n1, n2, n3,m1, m2, m3, r);
        rendezVous.repartirConseiller();
        rendezVous.repartirCaissiers();

        rendezVous.programmerRendezVous();


        Journee journee_1 = new Journee (sigma, mu, lambdas, 4,σb ,μb ,μr, σr,p , s);
        journee_1.simulateOneRun (7200 * 3);

        System.out.println (Simulateur.custWaitsA.report());
        System.out.println (Simulateur.custWaitsB.report());
        System.out.println (Simulateur.totWait.report());
    }
}
