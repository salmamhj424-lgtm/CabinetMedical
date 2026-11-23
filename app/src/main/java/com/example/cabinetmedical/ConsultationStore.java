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
// Collections : outils pour trier les listes
import java.util.Collections;
// List : une liste d'éléments
import java.util.List;
// UUID : génère des identifiants uniques
import java.util.UUID;

/**
 * Classe qui gère la table "consultations" de la base SQLite (CRUD).
 * Chaque consultation est liée à un patient par la colonne patient_id.
 */
public class ConsultationStore {

    // Remplace null par "" (évite les erreurs si une valeur est vide)
    private static String nonNull(String s) {
        return s == null ? "" : s;
    }

    /**
     * Transforme la ligne courante du curseur en objet Consultation.
     */
    private static Consultation fromCursor(Cursor c) {
        return new Consultation(
                c.getString(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("patient_id")),
                nonNull(c.getString(c.getColumnIndexOrThrow("date"))),
                nonNull(c.getString(c.getColumnIndexOrThrow("motif"))),
                nonNull(c.getString(c.getColumnIndexOrThrow("diagnostic"))),
                nonNull(c.getString(c.getColumnIndexOrThrow("traitement"))),
                c.getInt(c.getColumnIndexOrThrow("montant")),
                // paye est stocké en entier : 1 = payé, 0 = non payé
                c.getInt(c.getColumnIndexOrThrow("paye")) == 1);
    }

    /**
     * Transforme une Consultation en ContentValues pour SQLite.
     */
    private static ContentValues toValues(Consultation c) {
        ContentValues v = new ContentValues();
        v.put("id", c.id);
        v.put("patient_id", c.patientId);
        v.put("date", c.date);
        v.put("motif", c.motif);
        v.put("diagnostic", c.diagnostic);
        v.put("traitement", c.traitement);
        v.put("montant", c.montant);
        // Un booléen devient 1 ou 0 dans SQLite
        v.put("paye", c.paye ? 1 : 0);
        return v;
    }

    /**
     * READ : lit toutes les consultations (SELECT * FROM consultations).
     */
    public static List<Consultation> loadAll(Context context) {
        // Liste vide à remplir
        List<Consultation> list = new ArrayList<>();
        // Base en lecture
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        // SELECT * FROM consultations ORDER BY rowid
        Cursor c = db.query(DatabaseHelper.T_CONSULTATIONS, null, null, null,
                null, null, "rowid");
        // Pour chaque ligne, on crée une Consultation
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        // On ferme le curseur
        c.close();
        return list;
    }

    /**
     * READ : consultations d'un patient, la plus récente en premier.
     * SELECT * FROM consultations WHERE patient_id = ?
     */
    public static List<Consultation> loadFor(Context context, String patientId) {
        List<Consultation> result = new ArrayList<>();
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        // On ne garde que les lignes de ce patient
        Cursor c = db.query(DatabaseHelper.T_CONSULTATIONS, null, "patient_id = ?",
                new String[]{patientId}, null, null, "rowid");
        while (c.moveToNext()) {
            result.add(fromCursor(c));
        }
        c.close();
        // Tri : la date la plus récente d'abord
        Collections.sort(result, (a, b) -> Long.compare(
                DateUtils.daysFromToday(b.date), DateUtils.daysFromToday(a.date)));
        return result;
    }

    /**
     * CREATE : ajoute une consultation (INSERT INTO consultations ...).
     */
    public static void add(Context context, Consultation c) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        db.insert(DatabaseHelper.T_CONSULTATIONS, null, toValues(c));
    }

    /**
     * UPDATE : modifie une consultation (UPDATE consultations SET ... WHERE id = ?).
     */
    public static void update(Context context, Consultation updated) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        db.update(DatabaseHelper.T_CONSULTATIONS, toValues(updated),
                "id = ?", new String[]{updated.id});
    }

    /**
     * DELETE : supprime une consultation (DELETE FROM consultations WHERE id = ?).
     */
    public static void delete(Context context, String id) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        db.delete(DatabaseHelper.T_CONSULTATIONS, "id = ?", new String[]{id});
    }

    /**
     * Supprime toutes les consultations d'un patient.
     */
    public static void deleteForPatient(Context context, String patientId) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        db.delete(DatabaseHelper.T_CONSULTATIONS, "patient_id = ?", new String[]{patientId});
    }

    /**
     * Supprime toutes les consultations.
     */
    public static void clearAll(Context context) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getWritableDatabase();
        db.delete(DatabaseHelper.T_CONSULTATIONS, null, null);
    }

    /**
     * Compte les consultations (SELECT COUNT(*) FROM consultations).
     */
    public static int countAll(Context context) {
        SQLiteDatabase db = DatabaseHelper.getInstance(context).getReadableDatabase();
        // rawQuery : on écrit directement la requête SQL
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.T_CONSULTATIONS, null);
        int n = 0;
        // La première colonne contient le nombre
        if (c.moveToFirst()) {
            n = c.getInt(0);
        }
        c.close();
        return n;
    }

    /** Total des montants payés dans la liste. */
    public static int totalPaid(List<Consultation> list) {
        int sum = 0;
        for (Consultation c : list) {
            if (c.paye) {
                sum += c.montant;
            }
        }
        return sum;
    }

    /** Total des montants non payés dans la liste. */
    public static int totalUnpaid(List<Consultation> list) {
        int sum = 0;
        for (Consultation c : list) {
            if (!c.paye) {
                sum += c.montant;
            }
        }
        return sum;
    }

    /** Ajoute quelques consultations d'exemple, une seule fois. */
    public static void seedDemo(Context context) {
        // Si déjà fait, on quitte
        if (SettingsStore.getFlag(context, "consult_seeded")) {
            return;
        }
        // On vérifie quels patients existent (la clé étrangère l'exige)
        boolean hasP1 = PatientStore.getById(context, "p1") != null;
        boolean hasP2 = PatientStore.getById(context, "p2") != null;
        if (hasP1) {
            add(context, new Consultation(UUID.randomUUID().toString(), "p1",
                    DateUtils.addDays(-30), "Contrôle de la glycémie",
                    "Diabète stable", "Metformine 850 mg, 2 fois par jour", 60, true));
            add(context, new Consultation(UUID.randomUUID().toString(), "p1",
                    DateUtils.addDays(-90), "Fatigue et soif",
                    "Diabète type 2", "Régime alimentaire + bilan sanguin", 50, true));
        }
        if (hasP2) {
            add(context, new Consultation(UUID.randomUUID().toString(), "p2",
                    DateUtils.addDays(-15), "Éruption cutanée",
                    "Allergie légère", "Antihistaminique pendant 5 jours", 40, false));
        }
        // On note que c'est fait
        SettingsStore.setFlag(context, "consult_seeded");
    }

    /** Ajoute des consultations supplémentaires pour les statistiques, une seule fois. */
    public static void seedDemoV3(Context context) {
        // Si déjà fait, on quitte
        if (SettingsStore.getFlag(context, "consult_v3")) {
            return;
        }
        // Montants donnés aux consultations qui n'en ont pas encore
        int[] montants = {60, 50, 40};
        List<Consultation> list = loadAll(context);
        for (int i = 0; i < list.size(); i++) {
            Consultation c = list.get(i);
            // Montant 0 : on met un montant d'exemple
            if (c.montant == 0) {
                update(context, new Consultation(c.id, c.patientId, c.date, c.motif,
                        c.diagnostic, c.traitement, montants[i % 3], i % 3 != 2));
            }
        }
        // Les clés étrangères imposent que le patient existe avant d'ajouter sa consultation
        boolean hasP1 = PatientStore.getById(context, "p1") != null;
        boolean hasP2 = PatientStore.getById(context, "p2") != null;
        boolean hasP3 = PatientStore.getById(context, "p3") != null;
        if (hasP3) {
            add(context, new Consultation(UUID.randomUUID().toString(), "p3",
                    DateUtils.addDays(-5), "Tension élevée", "Hypertension artérielle",
                    "Amlodipine 5 mg, 1 fois par jour", 50, true));
            add(context, new Consultation(UUID.randomUUID().toString(), "p3",
                    DateUtils.addDays(-62), "Maux de tête", "Hypertension à surveiller",
                    "Mesure de la tension chaque matin", 40, true));
        }
        if (hasP1) {
            add(context, new Consultation(UUID.randomUUID().toString(), "p1",
                    DateUtils.addDays(-125), "Bilan annuel", "Bon état général",
                    "Aucun traitement", 60, true));
        }
        if (hasP2) {
            add(context, new Consultation(UUID.randomUUID().toString(), "p2",
                    DateUtils.addDays(-48), "Toux persistante", "Bronchite légère",
                    "Sirop + repos 5 jours", 40, true));
            add(context, new Consultation(UUID.randomUUID().toString(), "p2",
                    DateUtils.addDays(-2), "Contrôle", "Guérison complète",
                    "Aucun traitement", 30, false));
        }
        // On note que c'est fait
        SettingsStore.setFlag(context, "consult_v3");
    }
}
