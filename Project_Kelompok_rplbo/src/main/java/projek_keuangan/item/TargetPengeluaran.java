package projek_keuangan.item;

import java.util.HashMap;
import java.util.Map;

public class TargetPengeluaran {
    private Map<String, Integer> pengeluaranPerBulan;
    private int target;

    public TargetPengeluaran(int target) {
        this.target = target;
        this.pengeluaranPerBulan = new HashMap<>();
    }

    // Mengatur pengeluaran dalam jangka waktu tertwntu

    public void tambahPengeluaran(String bulan, int jumlah) {
        int total = pengeluaranPerBulan.getOrDefault(bulan, 0) + jumlah;
        pengeluaranPerBulan.put(bulan, total);

        if (total > target) {
            System.out.println("[WARNING]Pengeluaran bulan" + bulan + " telah melebihi target:"+total);
        } else {
            System.out.println("Pengeluaran ditambahkan untuk"+bulan+":"+total);
        }
    }
    public Map<String, Integer>getPengeluaranPerBulan(){
        return pengeluaranPerBulan;
    }
}
