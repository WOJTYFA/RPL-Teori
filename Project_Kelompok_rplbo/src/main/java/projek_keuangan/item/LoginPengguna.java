package projek_keuangan.item;

import java.util.HashMap;
import java.util.Map;

public class LoginPengguna {
    private Map<String, String> akunPengguna;

    public LoginPengguna(){
        akunPengguna = new HashMap<>();
    }

    // Pengguna dapat login menggunakan no hp dan password
    public boolean login(String noHp, String password){
        if(akunPengguna.containsKey(noHp)){
            boolean cocok = akunPengguna.get(noHp).equals(password);
            System.out.println( cocok ? "Login berhasil.":"Password.");
            return cocok;
        } else {
            System.out.println("Nomor hp tidak ditemukan");
            return false;
        }
    }
}
