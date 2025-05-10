package projek_keuangan.item;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class keuanganItem {
    private StringProperty tanggal;
    private StringProperty nominal;
    private StringProperty catatan;
    private StringProperty kategori;

    public keuanganItem(String tanggal, String nominal, String catatan, String kategori) {
        this.tanggal = new SimpleStringProperty(tanggal);
        this.nominal = new SimpleStringProperty(nominal);
        this.catatan = new SimpleStringProperty(catatan);
        this.kategori = new SimpleStringProperty(kategori);
    }

    public String getTanggal() { return tanggal.get(); }
    public String getNominal() { return nominal.get(); }
    public String getCatatan() { return catatan.get(); }
    public String getKategori() { return kategori.get(); }

    public void setTanggal(String value) { tanggal.set(value); }
    public void setNominal(String value) { nominal.set(value); }
    public void setCatatan(String value) { catatan.set(value); }
    public void setKategori(String value) { kategori.set(value); }

    public StringProperty tanggalProperty() { return tanggal; }
    public StringProperty nominalProperty() { return nominal; }
    public StringProperty catatanProperty() { return catatan; }
    public StringProperty kategoriProperty() { return kategori; }
}


