package projek_keuangan.data;

import java.util.HashMap;
import java.util.Map;

public class LoginPengguna {
    private Map<String, String>  akunPengguna;
    public LoginPengguna() {
        akunPengguna = new HashMap<>();
    }
    //Fitur login noHp + password
    public boolean login(String noHp, String password) {
        if (akunPengguna.containsKey(noHp)) {
            boolean cocok = akunPengguna.get(noHp).equals(password);
            System.out.println("Login Pengguna " + noHp + " : " + cocok);
            return cocok;
        } else {
            System.out.println("Nomor HP tidak valid");
            return false;
        }
    }
}
