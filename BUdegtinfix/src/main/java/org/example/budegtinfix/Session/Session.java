package org.example.budegtinfix.Session;

public class Session {
    private static String namaUser;
    private static int idUser;

    public static void setNamaUser(String nama) {
        namaUser = nama;
    }

    public static String getNamaUser() {
        return namaUser;
    }

    public static void setIdUser(int id) {
        idUser = id;
    }

    public static int getIdUser() {
        return idUser;
    }

    // Perbarui metode clearSession() untuk juga membersihkan idUser
    public static void clearSession() {
        namaUser = null;
        idUser = 0; // Atur kembali idUser ke nilai default (misalnya 0 atau -1)
    }
}