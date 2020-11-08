import umontreal.ssj.rng.MRG32k3a;
import umontreal.ssj.simevents.Accumulate;
//import umontreal.ssj.stat.Tally;
import umontreal.ssj.stat.*;
//import umontreal.ssj.stat.TallyStore;
import umontreal.ssj.charts.HistogramChart;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Simulateur {
    static  Tally custWaitsA = new Tally ("Waiting times of Client A");
    static TallyStore statA = new TallyStore("Statistics on times of Client A");
    static  Tally custWaitsB   = new Tally ("Waiting times of Client B ");
    static  Accumulate totWait  = new Accumulate ("Size of queue");
    static  LinkedList<Client> waitListA = new LinkedList<Client> ();

    /***
     * Data File Reading Params
     */
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


    /**
     * Parameters of genServA sigma and mu and genServB
     */
    static  double sigma_a = Math.sqrt ( Math.log10 ( 1 + Math.pow (σa/μa, 2) ) );
    static  double mu_a = Math.log10(Math.pow ( μa, 2 )/Math.sqrt ( Math.pow ( σa, 2 ) + Math.pow ( μa, 2 ) ));

    static  double sigma_b = Math.sqrt ( Math.log10 ( 1 + Math.pow (σb/μb, 2) ) );
    static  double mu_b = Math.log10(Math.pow ( μb, 2 )/Math.sqrt ( Math.pow ( σb, 2 ) + Math.pow ( μb, 2 ) ));

    static  double [] lambdas = {λ1, λ2, λ3};

    public static void simulateOneDay(int j){

        Plannificator rendezVous = new Plannificator(n1, n2, n3,m1, m2, m3, r);
        rendezVous.repartirConseiller();
        rendezVous.repartirCaissiers();
        rendezVous.programmerRendezVous();


        Journee journee_1 = new Journee (sigma_a, mu_a, lambdas,sigma_b ,mu_b ,μr, σr,p , s);
        journee_1.simulateOneRun (7200 * 3);

        System.out.println ("W"+j+",a : "+Simulateur.custWaitsA.sum());
        System.out.println ("W"+j+",b : "+Simulateur.custWaitsB.sum());
        System.out.println(Simulateur.statA.getArray());
    }

    public static  void simulateDays(int n){
        for(int i =1; i<n+1 ; i++) simulateOneDay(i);
    }

    /***
     * The main function of the Class Simulator
     * @param args
     */
    public static void main (String[] args) {

        Simulateur.simulateDays(1);

        System.out.println(custWaitsA.report());
        System.out.println(custWaitsB.report());
        System.out.println(totWait.report());
    }
}
