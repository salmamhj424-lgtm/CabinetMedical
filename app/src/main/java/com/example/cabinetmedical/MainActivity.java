package com.example.cabinetmedical;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String DEMO_EMAIL = "medecin@cabinet.tn";
    private static final String DEMO_PASSWORD = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();

            if (email.isEmpty()) {
                etEmail.setError("Entrez votre email");
                return;
            }
            if (password.isEmpty()) {
                etPassword.setError("Entrez votre mot de passe");
                return;
            }
            if (email.equalsIgnoreCase(DEMO_EMAIL) && password.equals(SettingsStore.getPassword(MainActivity.this))) {
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(MainActivity.this,
                        "Email ou mot de passe incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        tvForgot.setOnClickListener(v -> Toast.makeText(MainActivity.this,
                "Contactez l'administrateur du cabinet", Toast.LENGTH_LONG).show());
    }
}
