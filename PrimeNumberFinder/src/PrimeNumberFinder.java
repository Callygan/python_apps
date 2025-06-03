import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrimeNumberFinder {

    private static final int MAX_VALUE = 100000;

    private static List<Integer> primeBuffer = new ArrayList<>();
    private static final Object lock = new Object();

    public static void main(String[] args) {
        // Cream două thread-uri: unul pentru căutarea numerelor prime și altul pentru scrierea în fișier
        Thread searchThread = new Thread(new PrimeSearch());
        Thread writeThread = new Thread(new FileWriterThread());

        // Pornim cele două thread-uri
        searchThread.start();
        writeThread.start();
    }

    /*
    Interfața Runnable în Java este utilizată pentru a defini un fir de execuție,
    adică o unitate de lucru care poate fi executată de un thread.
    Atunci când o clasă implementează interfața Runnable, ea trebuie să ofere o
    implementare a metodei run(). Această metodă conține codul care va fi executat în
    cadrul firului de execuție atunci când acesta este pornit.
     */
    static class PrimeSearch implements Runnable {
        @Override
        public void run() {
            // Parcurgem numerele până la MAX_VALUE pentru a găsi numerele prime
            for (int i = 2; i <= MAX_VALUE; i++) {
                if (isPrime(i)) {
                    // Dacă găsim un număr prim, îl adăugăm în buffer și notificăm thread-ul de scriere
                    synchronized (lock) {
                        primeBuffer.add(i);
                        lock.notify(); // Notificăm thread-ul de scriere că există date în buffer
                    }
                }
            }
        }

        private boolean isPrime(int n) {
            // Metoda auxiliară care verifică dacă un număr este prim
            if (n <= 1) {
                return false;
            }
            for (int i = 2; i <= Math.sqrt(n); i++) {
                if (n % i == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    static class FileWriterThread implements Runnable {
        @Override
        public void run() {
            // Cream un FileWriter pentru a scrie în fișierul "prime_numbers.txt"
            try (FileWriter writer = new FileWriter("prime_numbers.txt")) {
                while (true) {
                    synchronized (lock) {
                        // Așteptăm până când există date în buffer
                        if (primeBuffer.isEmpty()) {
                            try {
                                lock.wait(); // Așteptăm până când există date în buffer
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // Iterăm prin buffer și scriem numerele prime în fișier
                        for (int prime : primeBuffer) {
                            writer.write(prime + "\n");
                        }
                        primeBuffer.clear(); // Golim buffer-ul după ce am scris datele în fișier
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
