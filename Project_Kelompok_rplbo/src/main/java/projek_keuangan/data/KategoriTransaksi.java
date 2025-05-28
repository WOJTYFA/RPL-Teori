package projek_keuangan.data;

import java.util.HashMap;
import java.util.Map;

public class KategoriTransaksi {

    private Map<String, String> kategoriTransaksi;

    public KategoriTransaksi() {
        kategoriTransaksi = new HashMap<>();
    }

    //Pengguna dapat mengkategorisasi transaksi
    public void kategorikan(String keterangan, String kategori) {
        kategoriTransaksi.put(keterangan, kategori);
        System.out.println("Transaksi '" + keterangan + "' dikategorikan sebagai: " + kategori);
    }

    public Map<String, String> getKategoriTransaksi() {
        return kategoriTransaksi;
    }
}
