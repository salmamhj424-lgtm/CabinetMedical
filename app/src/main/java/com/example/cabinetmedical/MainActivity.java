package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Écran de connexion (login) : c'est le premier écran de l'application.
 * Le médecin saisit son email et son mot de passe.
 * Si c'est correct, on ouvre HomeActivity (l'accueil).
 * Utilise le fichier de mise en page activity_main.xml et SettingsStore (mot de passe).
 * On y arrive au lancement de l'application.
 */
public class MainActivity extends AppCompatActivity {

    // Email "de démonstration" accepté par l'application (constante : elle ne change jamais)
    private static final String DEMO_EMAIL = "medecin@cabinet.tn";
    // Mot de passe par défaut, utilisé pour la démo
    private static final String DEMO_PASSWORD = "123456";

    /**
     * Méthode appelée automatiquement par Android quand l'écran (Activity) est créé.
     * Une Activity = un écran de l'application.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // On appelle d'abord le onCreate de la classe parente (obligatoire)
        super.onCreate(savedInstanceState);
        // On affiche le dessin de l'écran défini dans le fichier XML activity_main
        setContentView(R.layout.activity_main);

        // findViewById : on récupère un élément du XML (ici le champ email) grâce à son id
        EditText etEmail = findViewById(R.id.etEmail);
        // On récupère le champ du mot de passe
        EditText etPassword = findViewById(R.id.etPassword);
        // On récupère le bouton "Se connecter"
        Button btnLogin = findViewById(R.id.btnLogin);
        // On récupère le texte "Mot de passe oublié ?"
        TextView tvForgot = findViewById(R.id.tvForgot);

        // On dit quoi faire quand on clique sur le bouton.
        // "v -> { ... }" est une lambda : une petite fonction écrite directement ici.
        btnLogin.setOnClickListener(v -> {
            // On lit le texte de l'email ; trim() enlève les espaces au début et à la fin
            String email = etEmail.getText().toString().trim();
            // On lit le mot de passe (sans trim : les espaces peuvent faire partie du mot de passe)
            String password = etPassword.getText().toString();

            // Si l'email est vide...
            if (email.isEmpty()) {
                // ...on affiche un message d'erreur rouge sur le champ
                etEmail.setError("Entrez votre email");
                // return : on arrête ici, on ne vérifie pas la suite
                return;
            }
            // Si le mot de passe est vide...
            if (password.isEmpty()) {
                // ...on affiche une erreur sur le champ du mot de passe
                etPassword.setError("Entrez votre mot de passe");
                // On arrête ici
                return;
            }
            // Si l'email est bon (majuscules ignorées) ET le mot de passe est celui enregistré...
            // MainActivity.this = le Context (l'écran actuel), dont SettingsStore a besoin
            if (email.equalsIgnoreCase(DEMO_EMAIL) && password.equals(SettingsStore.getPassword(MainActivity.this))) {
                // Intent = un "message" pour demander à Android d'ouvrir un autre écran (accueil)
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                // On ferme l'écran de connexion pour ne pas y revenir avec le bouton "retour"
                finish();
            } else {
                // Sinon : un Toast est un petit message qui apparaît puis disparaît tout seul
                Toast.makeText(MainActivity.this,
                        "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        // Quand on clique sur "Mot de passe oublié", on affiche juste un Toast d'information
        tvForgot.setOnClickListener(v -> Toast.makeText(MainActivity.this,
                "Contactez l'administrateur du cabinet", Toast.LENGTH_LONG).show());
    }
}
