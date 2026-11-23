// On indique le dossier (package) où se trouve cette classe
package com.example.cabinetmedical;

// ContentValues : paires colonne -> valeur pour INSERT / UPDATE
import android.content.ContentValues;
// Context : donne accès aux services de l'application
import android.content.Context;
// Cursor : résultat d'une requête SELECT
import android.database.Cursor;
// SQLiteDatabase : la base de données
import android.database.sqlite.SQLiteDatabase;

// ArrayList : un type de liste que l'on peut agrandir
import java.util.ArrayList;
// List : une liste d'éléments
import java.util.List;

/**
 * Classe qui gère la table "patients" de la base SQLite (CRUD) :
 * Create (insert), Read (load / getById), Update (update), Delete (delete).
 * Elle est appelée par PatientsActivity, PatientFormActivity et d'autres écrans.
 */
public class PatientStore {

    /**
     * Transforme la ligne courante du curseur en objet Patient.
     */
    private static Patient fromCursor(Cursor c) {
        // getColumnIndexOrThrow : numéro de la colonne à partir de son nom
        return new Patient(
                c.getString(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("nom")),
                c.getInt(c.getColumnIndexOrThrow("age")),
                nonNull(c.getString(c.getColumnIndexOrThrow("telephone"))),
                nonNull(c.getString(c.getColumnIndexOrThrow("notes"))),
                nonNull(c.getString(c.getColumnIndexOrThrow("rdv"))),
                nonNull(c.getString(c.getColumnIndexOrThrow("groupe"))),
                nonNull(c.getString(c.getColumnIndexOrThrow("allergies"))));
    }

    // Remplace null par "" (évite les erreurs si une valeur est vide dans la base)
    private static String nonNull(String s) {
        return s == null ? "" : s;
    }

    /**
     * Transforme un Patient en ContentValues (colonne -> valeur) pour SQLite.
     */
    private static ContentValues toValues(Patient p) {
        // Conteneur vide
        ContentValues v = new ContentValues();
        // Une ligne par colonne de la table
        v.put("id", p.id);
        v.put("nom", p.nom);
        v.put("age", p.age);
        v.put("telephone", p.telephone);
        v.put("notes", p.notes);
        v.put("rdv", p.rdv);
        v.put("groupe", p.groupe);
        v.put("allergies", p.allergies);
        // On renvoie le conteneur rempli
        return v;
    }

    /**
     * READ : lit tous les patients de la base (SELECT * FROM patients).
     */
    public static List<Patient> load(Context context) {
        // Liste vide à remplir
        List<Patient> list = new ArrayList<>();
        // On ouvre la base en lecture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        // SELECT * FROM patients ORDER BY rowid (ordre d'ajout)
        Cursor c = db.query(DatabaseHelper.T_PATIENTS, null, null, null,
                null, null, "rowid");
        // Tant qu'il y a une ligne suivante
        while (c.moveToNext()) {
            // On convertit la ligne en Patient et on l'ajoute
            list.add(fromCursor(c));
        }
        // On ferme le curseur
        c.close();
        // On renvoie la liste
        return list;
    }

    /**
     * READ : cherche un patient par son id (null s'il n'existe pas).
     */
    public static Patient getById(Context context, String id) {
        // Base en lecture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        // SELECT * FROM patients WHERE id = ?
        Cursor c = db.query(DatabaseHelper.T_PATIENTS, null, "id = ?",
                new String[]{id}, null, null, null);
        // Résultat : null au début
        Patient p = null;
        // Si on a trouvé une ligne, on la convertit
        if (c.moveToFirst()) {
            p = fromCursor(c);
        }
        // On ferme le curseur
        c.close();
        // On renvoie le patient (ou null)
        return p;
    }

    /**
     * CREATE : ajoute un nouveau patient (INSERT INTO patients ...).
     */
    public static void insert(Context context, Patient p) {
        // Base en écriture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        // On insère la ligne
        db.insert(DatabaseHelper.T_PATIENTS, null, toValues(p));
    }

    /**
     * UPDATE : modifie un patient existant (UPDATE patients SET ... WHERE id = ?).
     */
    public static void update(Context context, Patient p) {
        // Base en écriture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        // On met à jour la ligne dont l'id correspond
        db.update(DatabaseHelper.T_PATIENTS, toValues(p), "id = ?", new String[]{p.id});
    }

    /**
     * DELETE : supprime un patient (DELETE FROM patients WHERE id = ?).
     * Ses consultations sont supprimées automatiquement (ON DELETE CASCADE).
     */
    public static void delete(Context context, String id) {
        // Base en écriture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        // On supprime la ligne dont l'id correspond
        db.delete(DatabaseHelper.T_PATIENTS, "id = ?", new String[]{id});
    }

    /**
     * Ajoute 3 patients d'exemple, mais seulement au tout premier lancement.
     */
    public static void seedIfFirstLaunch(Context context) {
        // Si les patients d'exemple ont déjà été ajoutés, on quitte
        if (SettingsStore.getFlag(context, "seeded")) {
            return;
        }
        // On insère les trois patients d'exemple
        insert(context, new Patient("p1", "Ahmed Ben Ali", 45, "22345678", "", ""));
        insert(context, new Patient("p2", "Sarra Mansour", 32, "55123456", "", ""));
        insert(context, new Patient("p3", "Youssef Trabelsi", 58, "98765432", "", ""));
        // On note que c'est fait, pour ne pas le refaire au prochain lancement
        SettingsStore.setFlag(context, "seeded");
    }

    /** Données de démonstration supplémentaires (rendez-vous, notes), une seule fois. */
    public static void seedDemoV2(Context context) {
        // Si déjà fait, on quitte
        if (SettingsStore.getFlag(context, "demo_v2")) {
            return;
        }
        // On passe sur chaque patient enregistré
        for (Patient p : load(context)) {
            // S'il a déjà un rendez-vous, on le laisse tel quel
            if (!p.rdv.isEmpty()) {
                continue;
            }
            // Si c'est le patient p1 : note + rendez-vous aujourd'hui
            if ("p1".equals(p.id)) {
                update(context, new Patient(p.id, p.nom, p.age, p.telephone,
                        p.notes.isEmpty() ? "Diabète type 2, suivi régulier" : p.notes,
                        DateUtils.addDays(0), p.groupe, p.allergies));
            } else if ("p2".equals(p.id)) {
                // Patient p2 : note + rendez-vous dans 2 jours
                update(context, new Patient(p.id, p.nom, p.age, p.telephone,
                        p.notes.isEmpty() ? "Allergie à la pénicilline" : p.notes,
                        DateUtils.addDays(2), p.groupe, p.allergies));
            } else if ("p3".equals(p.id)) {
                // Patient p3 : note + rendez-vous qui date d'il y a 2 jours
                update(context, new Patient(p.id, p.nom, p.age, p.telephone,
                        p.notes.isEmpty() ? "Hypertension artérielle" : p.notes,
                        DateUtils.addDays(-2), p.groupe, p.allergies));
            }
        }
        // On note que c'est fait
        SettingsStore.setFlag(context, "demo_v2");
    }

    /** Groupe sanguin et allergies de démonstration, une seule fois. */
    public static void seedDemoV3(Context context) {
        // Si déjà fait, on quitte
        if (SettingsStore.getFlag(context, "demo_v3")) {
            return;
        }
        // On passe sur chaque patient enregistré
        for (Patient p : load(context)) {
            // Copies du groupe sanguin et des allergies actuels
            String groupe = p.groupe;
            String allergies = p.allergies;
            if ("p1".equals(p.id)) {
                // p1 : groupe A+ s'il n'en avait pas
                groupe = groupe.isEmpty() ? "A+" : groupe;
            } else if ("p2".equals(p.id)) {
                // p2 : groupe O- et allergie Pénicilline s'ils n'étaient pas remplis
                groupe = groupe.isEmpty() ? "O-" : groupe;
                allergies = allergies.isEmpty() ? "Pénicilline" : allergies;
            } else if ("p3".equals(p.id)) {
                // p3 : groupe B+ s'il n'en avait pas
                groupe = groupe.isEmpty() ? "B+" : groupe;
            } else {
                // Autre patient : on ne change rien
                continue;
            }
            // On met à jour le patient dans la base
            update(context, new Patient(p.id, p.nom, p.age, p.telephone, p.notes, p.rdv,
                    groupe, allergies));
        }
        // On note que c'est fait
        SettingsStore.setFlag(context, "demo_v3");
    }

    /**
     * Efface tous les patients (DELETE FROM patients) ; les consultations suivent (CASCADE).
     */
    public static void clearAll(Context context) {
        // Base en écriture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        // Sans condition : supprime toutes les lignes
        db.delete(DatabaseHelper.T_PATIENTS, null, null);
    }
}
