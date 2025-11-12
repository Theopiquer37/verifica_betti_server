package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;


public class Server {

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void avvio() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server in ascolto sulla porta " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                MioThread st = new MioThread(clientSocket);
                st.start();
            }
        } catch (IOException e) {
            System.out.println("Errore server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try { serverSocket.close(); } catch (IOException ignored) {}
            }
        }
    }

    private static class MioThread extends Thread {
        private Socket socket;

        private int minRange;
        private int maxRange;
        private int secret;
        private int tries;
        private boolean rangeLocked; 

        public MioThread(Socket s) {
            this.socket = s;
            this.minRange = 1;
            this.maxRange = 100;
            this.tries = 0;
            this.rangeLocked = false;
            this.secret = generateSecret(minRange, maxRange);
        }

        private int generateSecret(int a, int b) {
            int segreto = ThreadLocalRandom.current().nextInt(1, 101); // 1 incluso, 101 escluso
            return segreto;
        }

        public void run() {
            BufferedReader in = null;
            PrintWriter out = null;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                invioWelcome(out);

                while (true) {
                    String line = in.readLine();
                    if (line == null) {

                        System.out.println("il Client ha chiuso la connessione.");
                        break;
                    }

                    line = line.trim();
                    if (line.equals("")) {
 
                        continue;
                    }

        
                    String[] parts = line.split(" ", 3); 
                    String riga = parts[0];

                    try {
                        if (riga.equals("GUESS")) {
                            handleGuess(riga, parts, out);
                        } else if (riga.equals("RANGE")) {
                            handleRange(riga, parts, out);
                        } else if (riga.equals("STATS")) {
                            handleStats(out);
                        } else if (riga.equals("NEW")) {
                            handleNew(out);
                        } else if (riga.equals("QUIT")) {
                            out.println("BYE");
                            break; 
                        } else {
                            out.println("ERR UNKNOWNCMD");
                        }
                    } catch (Exception ex) {
                        out.println("ERR INTERNAL");
                    }
                }

            } catch (IOException e) {
                System.out.println("Errore di comunicazione con il client");
            } finally {
                try { socket.close(); } catch (IOException ignored) {}
            }
        }

        private void invioWelcome(PrintWriter out) {
            out.println("WELCOME INDOVINA v1 RANGE " + minRange + " " + maxRange);
        }

 
        private void handleGuess(String cmd, String[] parts, PrintWriter out) {
 
            if (parts.length < 2) {
                out.println("ERR SYNTAX");
                return;
            }

            String numStr = parts[1].trim();
            int guess;
            try {
                guess = Integer.parseInt(numStr);
            } catch (NumberFormatException e) {
                out.println("ERR SYNTAX");
                return;
            }

            if (guess < minRange || guess > maxRange) {
                out.println("ERR OUTOFRANGE " + minRange + " " + maxRange);
                return;
            }

            tries++;
            rangeLocked = true;

            if (guess == secret) {
                out.println("OK CORRECT in T=" + tries);

            } else if (guess < secret) {
                out.println("HINT HIGHER");
            } else { 
                out.println("HINT LOWER");
            }
        }

        private void handleRange(String cmd, String[] parts, PrintWriter out) {
  
            if (parts.length < 3) {
                out.println("ERR SYNTAX");
                return;
            }
            String aStr = parts[1].trim();
            String bStr = parts[2].trim();
            int a, b;
            try {
                a = Integer.parseInt(aStr);
                b = Integer.parseInt(bStr);
            } catch (NumberFormatException e) {
                out.println("ERR SYNTAX");
                return;
            }

            if (a >= b) {
                out.println("ERR SYNTAX");
                return;
            }

            if (rangeLocked) {
                out.println("ERR NOTALLOWED");
                return;
            }

            this.minRange = a;
            this.maxRange = b;
            this.secret = generateSecret(minRange, maxRange);
            out.println("OK RANGE " + minRange + " " + maxRange);
        }

        private void handleStats(PrintWriter out) {
            out.println("INFO RANGE " + minRange + " " + maxRange + "; TRIES " + tries);
        }

        private void handleNew(PrintWriter out) {
     
            this.tries = 0;
            this.rangeLocked = false;
            this.secret = generateSecret(minRange, maxRange);
            out.println("OK NEW");

            invioWelcome(out);
        }
    }
}

