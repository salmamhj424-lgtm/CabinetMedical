// On indique le dossier (package) où se trouve cette classe
package com.example.cabinetmedical;

// ContentValues : paires colonne -> valeur pour insérer dans SQLite
import android.content.ContentValues;
// Context : donne accès aux services de l'application
import android.content.Context;
// Cursor : résultat d'une requête SELECT (on le lit ligne par ligne)
import android.database.Cursor;
// SQLiteDatabase : la base de données
import android.database.sqlite.SQLiteDatabase;

/**
 * Classe utilitaire pour les réglages (mot de passe, nom du médecin, indicateurs).
 * Les réglages sont stockés dans la table "settings" (colonnes cle, valeur) de SQLite.
 */
public class SettingsStore {
    // Clé du mot de passe
    private static final String KEY_PWD = "password";
    // Clé du nom du médecin
    private static final String KEY_DOCTOR = "doctor_name";
    // Mot de passe par défaut
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * Lit la valeur d'une clé ; renvoie defaut si la clé n'existe pas.
     */
    public static String get(Context context, String cle, String defaut) {
        // On ouvre la base en lecture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        // SELECT valeur FROM settings WHERE cle = ?  (le ? est remplacé par la clé)
        Cursor c = db.query(DatabaseHelper.T_SETTINGS, new String[]{"valeur"},
                "cle = ?", new String[]{cle}, null, null, null);
        // Valeur à renvoyer : par défaut au début
        String result = defaut;
        // Si une ligne existe, on lit sa première colonne
        if (c.moveToFirst()) {
            result = c.getString(0);
        }
        // On ferme le curseur pour libérer la mémoire
        c.close();
        // On renvoie la valeur
        return result;
    }

    /**
     * Enregistre une valeur (insère la ligne, ou la remplace si la clé existe déjà).
     */
    public static void set(Context context, String cle, String valeur) {
        // On ouvre la base en écriture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        // On prépare les valeurs de la ligne
        ContentValues v = new ContentValues();
        // Colonne cle
        v.put("cle", cle);
        // Colonne valeur
        v.put("valeur", valeur);
        // INSERT OR REPLACE : remplace la ligne si la clé existe déjà
        db.insertWithOnConflict(DatabaseHelper.T_SETTINGS, null, v,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Indique si un indicateur (par exemple "demo_v2") est déjà mis à vrai.
     */
    public static boolean getFlag(Context context, String cle) {
        // Vrai seulement si la valeur enregistrée est "1"
        return "1".equals(get(context, cle, "0"));
    }

    /**
     * Met un indicateur à vrai (valeur "1").
     */
    public static void setFlag(Context context, String cle) {
        // On enregistre "1" pour cette clé
        set(context, cle, "1");
    }

    /** Renvoie le mot de passe (123456 par défaut). */
    public static String getPassword(Context context) {
        return get(context, KEY_PWD, DEFAULT_PASSWORD);
    }

    /** Change le mot de passe. */
    public static void setPassword(Context context, String password) {
        set(context, KEY_PWD, password);
    }

    /** Renvoie le nom du médecin ("" par défaut). */
    public static String getDoctor(Context context) {
        return get(context, KEY_DOCTOR, "");
    }

    /** Change le nom du médecin. */
    public static void setDoctor(Context context, String name) {
        set(context, KEY_DOCTOR, name);
    }
}
