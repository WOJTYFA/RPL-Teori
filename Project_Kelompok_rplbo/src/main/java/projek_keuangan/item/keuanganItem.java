package projek_keuangan.item;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class keuanganItem {
    private int id;
    private StringProperty tanggal;
    private StringProperty nominal; // Nominal dalam format string untuk tampilan
    private StringProperty catatan; // Kembalikan StringProperty
    private StringProperty kategori; // Kembalikan StringProperty
    private StringProperty tipeTransaksi; // Kembalikan StringProperty
    private double nominalValue; // Nominal dalam format double untuk perhitungan

    public keuanganItem(int id, String tanggal, String nominal, String catatan, String kategori, String tipeTransaksi, double nominalValue) {
        this.id = id;
        this.tanggal = new SimpleStringProperty(tanggal);
        this.nominal = new SimpleStringProperty(nominal);
        this.catatan = new SimpleStringProperty(catatan); // Gunakan SimpleStringProperty
        this.kategori = new SimpleStringProperty(kategori); // Gunakan SimpleStringProperty
        this.tipeTransaksi = new SimpleStringProperty(tipeTransaksi); // Gunakan SimpleStringProperty
        this.nominalValue = nominalValue;
    }

    public keuanganItem(String tanggal, String nominal, String catatan, String kategori, String tipeTransaksi, double nominalValue) {
        this.id = -1; // Default ID for new items
        this.tanggal = new SimpleStringProperty(tanggal);
        this.nominal = new SimpleStringProperty(nominal);
        this.catatan = new SimpleStringProperty(catatan); // Gunakan SimpleStringProperty
        this.kategori = new SimpleStringProperty(kategori); // Gunakan SimpleStringProperty
        this.tipeTransaksi = new SimpleStringProperty(tipeTransaksi); // Gunakan SimpleStringProperty
        this.nominalValue = nominalValue;
    }

    // Konstruktor yang ada (sesuaikan agar memanggil konstruktor baru)
    public keuanganItem(int id, String tanggal, String nominal, String catatan, String kategori, String tipeTransaksi) {
         this(id, tanggal, nominal, catatan, kategori, tipeTransaksi, parseNominalDouble(nominal)); // Parse nominal string
    }

    public keuanganItem(String tanggal, String nominal, String catatan, String kategori, String tipeTransaksi) {
         this(tanggal, nominal, catatan, kategori, tipeTransaksi, parseNominalDouble(nominal)); // Parse nominal string
    }

    // Method helper untuk parsing nominal string ke double
    private static double parseNominalDouble(String nominalStr) {
         try {
             return Double.parseDouble(nominalStr.replace("Rp.", "").replace(".", "").replace(",", "."));
         } catch (NumberFormatException e) {
             System.err.println("Error parsing nominal string: " + nominalStr + " - " + e.getMessage());
             return 0.0;
         }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTanggal() { return tanggal.get(); }
    public String getNominal() { return nominal.get(); }
    public String getCatatan() { return catatan.get(); }
    public String getKategori() { return kategori.get(); }
    public String getTipeTransaksi() { return tipeTransaksi.get(); }
    public double getNominalDouble() { return nominalValue; }


    public void setTanggal(String value) { tanggal.set(value); }
    public void setNominal(String value) { nominal.set(value); }
    public void setCatatan(String value) { catatan.set(value); }
    public void setKategori(String value) { kategori.set(value); }
    public void setTipeTransaksi(String value) { tipeTransaksi.set(value); }
    public void setNominalDouble(double value) { this.nominalValue = value; }


    public StringProperty tanggalProperty() { return tanggal; }
    public StringProperty nominalProperty() { return nominal; }
    public StringProperty catatanProperty() { return catatan; }
    public StringProperty kategoriProperty() { return kategori; }
    public StringProperty tipeTransaksiProperty() { return tipeTransaksi; }
}


