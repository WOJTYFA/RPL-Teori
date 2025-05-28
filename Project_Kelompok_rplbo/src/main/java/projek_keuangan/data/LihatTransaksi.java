package projek_keuangan.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LihatTransaksi {

    public static class Transaksi {
        public String keterangan;
        public int jumlah;
        public Date tanggal;

        public Transaksi(String keterangan, int jumlah, Date tanggal) {
            this.keterangan = keterangan;
            this.jumlah = jumlah;
            this.tanggal = tanggal;
        }
    }

    private List<Transaksi> semuaTransaksi;

    public LihatTransaksi() {
        semuaTransaksi = new ArrayList<>();
    }

    public void tambahTransaksi(String keterangan, int jumlah, Date tanggal) {
        semuaTransaksi.add(new Transaksi(keterangan, jumlah, tanggal));
    }

    //Melihat daftar transaksi dalam waktu tertentu
    public List<Transaksi> getTransaksiDalamRentang(Date dari, Date sampai) {
        List<Transaksi> hasil = new ArrayList<>();
        for (Transaksi t : semuaTransaksi) {
            if (!t.tanggal.before(dari) && !t.tanggal.after(sampai)) {
                hasil.add(t);
            }
        }
        return hasil;
    }
}
