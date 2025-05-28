package projek_keuangan.data;

import java.util.ArrayList;
import java.util.List;

public class TambahTransaksi {

    private List<String> transaksiList;

    public TambahTransaksi() {
        transaksiList = new ArrayList<>();
    }

    //Pengguna dapat menambahkan transaksi pengeluaran dan pemasukan
    public void tambahTransaksi(String keterangan, int jumlah) {
        String tipe = jumlah > 0 ? "Pemasukan" : "Pengeluaran";
        transaksiList.add(tipe + " - " + keterangan + ": " + jumlah);
        System.out.println("Transaksi berhasil ditambahkan: " + tipe + " - " + keterangan + " sebesar " + jumlah);
    }

    public List<String> getTransaksiList() {
        return transaksiList;
    }
}
