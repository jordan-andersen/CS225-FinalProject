package tests;

import model.PubChemService;
import java.util.Scanner;

/* this is a tester for PubChemService
** Code written by Andrew Lightfoot
 */
public class PubChemServiceTester {
    public static void main(String[] args) {
        PubChemService pubChemService = new PubChemService();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a CAS number (or exit to quit):");
        String cas = scanner.nextLine();
        while (!(cas.trim().equalsIgnoreCase("exit"))) {
            System.out.println("CAS number: " + cas + " being tested");
            pubChemService.browseByCas(cas);
            System.out.println("Finished test for CAS number: " + cas);
            System.out.print("Enter a CAS number (or exit to quit):");
            cas = scanner.nextLine();
        }
        scanner.close();
    }
}
