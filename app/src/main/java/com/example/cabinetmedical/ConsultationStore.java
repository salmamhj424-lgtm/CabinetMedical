package com.example.cabinetmedical;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ConsultationStore {
    private static final String PREFS = "cabinet_prefs";
    private static final String KEY = "consultations";
    private static final String KEY_SEEDED = "consult_seeded";

    public static List<Consultation> loadAll(Context context) {
        List<Consultation> list = new ArrayList<>();
        String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Consultation(
                        o.getString("id"),
                        o.optString("patientId"),
                        o.optString("date"),
                        o.optString("motif"),
                        o.optString("diagnostic"),
                        o.optString("traitement"),
                        o.optInt("montant"),
                        o.optBoolean("paye")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void saveAll(Context context, List<Consultation> list) {
        JSONArray arr = new JSONArray();
        for (Consultation c : list) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", c.id);
                o.put("patientId", c.patientId);
                o.put("date", c.date);
                o.put("motif", c.motif);
                o.put("diagnostic", c.diagnostic);
                o.put("traitement", c.traitement);
                o.put("montant", c.montant);
                o.put("paye", c.paye);
                arr.put(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, arr.toString()).apply();
    }

    /** Consultations d'un patient, la plus recente en premier. */
    public static List<Consultation> loadFor(Context context, String patientId) {
        List<Consultation> result = new ArrayList<>();
        for (Consultation c : loadAll(context)) {
            if (c.patientId.equals(patientId)) {
                result.add(c);
            }
        }
        Collections.sort(result, (a, b) -> Long.compare(
                DateUtils.daysFromToday(b.date), DateUtils.daysFromToday(a.date)));
        return result;
    }

    public static void add(Context context, Consultation c) {
        List<Consultation> list = loadAll(context);
        list.add(c);
        saveAll(context, list);
    }

    public static void update(Context context, Consultation updated) {
        List<Consultation> list = loadAll(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(updated.id)) {
                list.set(i, updated);
                break;
            }
        }
        saveAll(context, list);
    }

    public static void delete(Context context, String id) {
        List<Consultation> list = loadAll(context);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(id)) {
                list.remove(i);
                break;
            }
        }
        saveAll(context, list);
    }

    public static void deleteForPatient(Context context, String patientId) {
        List<Consultation> list = loadAll(context);
        List<Consultation> kept = new ArrayList<>();
        for (Consultation c : list) {
            if (!c.patientId.equals(patientId)) {
                kept.add(c);
            }
        }
        saveAll(context, kept);
    }

    public static void clearAll(Context context) {
        saveAll(context, new ArrayList<Consultation>());
    }

    public static int countAll(Context context) {
        return loadAll(context).size();
    }

    public static int totalPaid(List<Consultation> list) {
        int sum = 0;
        for (Consultation c : list) {
            if (c.paye) {
                sum += c.montant;
            }
        }
        return sum;
    }

    public static int totalUnpaid(List<Consultation> list) {
        int sum = 0;
        for (Consultation c : list) {
            if (!c.paye) {
                sum += c.montant;
            }
        }
        return sum;
    }

    public static void seedDemo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            return;
        }
        List<Patient> patients = PatientStore.load(context);
        boolean hasP1 = false;
        boolean hasP2 = false;
        for (Patient p : patients) {
            if ("p1".equals(p.id)) {
                hasP1 = true;
            }
            if ("p2".equals(p.id)) {
                hasP2 = true;
            }
        }
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
        prefs.edit().putBoolean(KEY_SEEDED, true).apply();
    }

    /** Ajoute montants / paiements et quelques consultations pour les statistiques. */
    public static void seedDemoV3(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean("consult_v3", false)) {
            return;
        }
        List<Consultation> list = loadAll(context);
        int[] montants = {60, 50, 40};
        for (int i = 0; i < list.size(); i++) {
            Consultation c = list.get(i);
            if (c.montant == 0) {
                list.set(i, new Consultation(c.id, c.patientId, c.date, c.motif,
                        c.diagnostic, c.traitement, montants[i % 3], i % 3 != 2));
            }
        }
        List<Patient> patients = PatientStore.load(context);
        boolean hasP1 = false;
        boolean hasP2 = false;
        boolean hasP3 = false;
        for (Patient p : patients) {
            if ("p1".equals(p.id)) {
                hasP1 = true;
            }
            if ("p2".equals(p.id)) {
                hasP2 = true;
            }
            if ("p3".equals(p.id)) {
                hasP3 = true;
            }
        }
        if (hasP3) {
            list.add(new Consultation(UUID.randomUUID().toString(), "p3",
                    DateUtils.addDays(-5), "Tension élevée", "Hypertension artérielle",
                    "Amlodipine 5 mg, 1 fois par jour", 50, true));
            list.add(new Consultation(UUID.randomUUID().toString(), "p3",
                    DateUtils.addDays(-62), "Maux de tête", "Hypertension à surveiller",
                    "Mesure de la tension chaque matin", 40, true));
        }
        if (hasP1) {
            list.add(new Consultation(UUID.randomUUID().toString(), "p1",
                    DateUtils.addDays(-125), "Bilan annuel", "Bon état général",
                    "Aucun traitement", 60, true));
        }
        if (hasP2) {
            list.add(new Consultation(UUID.randomUUID().toString(), "p2",
                    DateUtils.addDays(-48), "Toux persistante", "Bronchite légère",
                    "Sirop + repos 5 jours", 40, true));
            list.add(new Consultation(UUID.randomUUID().toString(), "p2",
                    DateUtils.addDays(-2), "Contrôle", "Guérison complète",
                    "Aucun traitement", 30, false));
        }
        saveAll(context, list);
        prefs.edit().putBoolean("consult_v3", true).apply();
    }
}
