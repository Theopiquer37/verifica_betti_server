package com.example;


public class Main {
    public static void main(String[] args) {
        int porta = 3000;
        System.out.println("Avvio Server sulla porta " + porta);
        Server server = new Server(porta);
        server.avvio();
    }
}
