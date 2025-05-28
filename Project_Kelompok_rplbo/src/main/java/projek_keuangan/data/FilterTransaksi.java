package projek_keuangan.data;

import java.util.ArrayList;
import java.util.List;

public class FilterTransaksi {

    public static class Transaksi {
        public String keterangan;
        public String kategori;
        public int jumlah;

        public Transaksi(String keterangan, String kategori, int jumlah) {
            this.keterangan = keterangan;
            this.kategori = kategori;
            this.jumlah = jumlah;
        }
    }

    private List<Transaksi> semuaTransaksi;

    public FilterTransaksi() {
        semuaTransaksi = new ArrayList<>();
    }

    public void tambahTransaksi(Transaksi transaksi) {
        semuaTransaksi.add(transaksi);
    }

    //Pengguna dapat menyaring berdasarkan transaksi
    public List<Transaksi> filterBerdasarkanKategori(String kategori) {
        List<Transaksi> hasil = new ArrayList<>();
        for (Transaksi t : semuaTransaksi) {
            if (t.kategori.equalsIgnoreCase(kategori)) {
                hasil.add(t);
            }
        }
        return hasil;
    }
}