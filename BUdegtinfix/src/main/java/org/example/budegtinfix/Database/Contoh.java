//package org.example.budegtinfix.Database;//package org.example.budegtinfix.Database;
//
//import java.sql.Connection;
//
//public class Contoh  {
//    public static void main(String[] args) {
//        try (Connection conn = CatatanDB.connect();
//             var stmt = conn.createStatement()) {
//
//            String sql = "ALTER TABLE transaksi ADD COLUMN gambar_path TEXT";
//            stmt.execute(sql);
//
//            System.out.println("Kolom 'nama' berhasil ditambahkan.");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//// untuk database gambar