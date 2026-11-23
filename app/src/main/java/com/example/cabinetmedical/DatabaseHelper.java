// On indique le dossier (package) où se trouve cette classe
package com.example.cabinetmedical;

// Context : donne accès aux services de l'application
import android.content.Context;
// SQLiteDatabase : représente la base de données SQLite (on y exécute le SQL)
import android.database.sqlite.SQLiteDatabase;
// SQLiteOpenHelper : classe d'Android qui crée et met à jour la base
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Classe qui crée et gère la base de données SQLite "cabinet.db".
 * La base contient 3 tables : patients, consultations et settings.
 * Les classes PatientStore, ConsultationStore et SettingsStore l'utilisent.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    // Nom du fichier de la base de données
    public static final String DB_NAME = "cabinet.db";
    // Numéro de version de la base (à augmenter si on change les tables)
    public static final int DB_VERSION = 1;

    // Nom de la table des patients
    public static final String T_PATIENTS = "patients";
    // Nom de la table des consultations
    public static final String T_CONSULTATIONS = "consultations";
    // Nom de la table des réglages (clé -> valeur)
    public static final String T_SETTINGS = "settings";

    // Une seule instance partagée dans toute l'application (singleton)
    private static DatabaseHelper instance;

    /**
     * Renvoie l'unique instance de DatabaseHelper (la crée au premier appel).
     * synchronized : évite que deux threads la créent en même temps.
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        // Si l'instance n'existe pas encore
        if (instance == null) {
            // On la crée avec le contexte de l'application
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        // On renvoie l'instance
        return instance;
    }

    // Constructeur privé : on passe par getInstance()
    private DatabaseHelper(Context context) {
        // On appelle le constructeur parent : contexte, nom du fichier, curseur par défaut, version
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Appelée à chaque ouverture : on active les clés étrangères
     * (pour que la suppression d'un patient supprime ses consultations).
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        // On garde le comportement normal du parent
        super.onConfigure(db);
        // PRAGMA foreign_keys = ON : active le contrôle des clés étrangères
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Appelée une seule fois, quand la base n'existe pas encore : crée les tables.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table patients : id = clé primaire (identifiant unique)
        db.execSQL("CREATE TABLE " + T_PATIENTS + " ("
                + "id TEXT PRIMARY KEY, "
                + "nom TEXT NOT NULL, "
                + "age INTEGER, "
                + "telephone TEXT, "
                + "notes TEXT, "
                + "rdv TEXT, "
                + "groupe TEXT, "
                + "allergies TEXT)");
        // Table consultations : patient_id est une clé étrangère vers patients(id)
        // ON DELETE CASCADE : si on supprime le patient, ses consultations disparaissent aussi
        db.execSQL("CREATE TABLE " + T_CONSULTATIONS + " ("
                + "id TEXT PRIMARY KEY, "
                + "patient_id TEXT NOT NULL, "
                + "date TEXT, "
                + "motif TEXT, "
                + "diagnostic TEXT, "
                + "traitement TEXT, "
                + "montant INTEGER, "
                + "paye INTEGER, "
                + "FOREIGN KEY (patient_id) REFERENCES " + T_PATIENTS + "(id) ON DELETE CASCADE)");
        // Table settings : une ligne par réglage (mot de passe, nom du médecin, indicateurs)
        db.execSQL("CREATE TABLE " + T_SETTINGS + " ("
                + "cle TEXT PRIMARY KEY, "
                + "valeur TEXT)");
    }

    /**
     * Appelée quand DB_VERSION augmente : on supprime et on recrée les tables.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On supprime les anciennes tables (consultations d'abord à cause de la clé étrangère)
        db.execSQL("DROP TABLE IF EXISTS " + T_CONSULTATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_SETTINGS);
        // On recrée les tables vides
        onCreate(db);
    }
}
