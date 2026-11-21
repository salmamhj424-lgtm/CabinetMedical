package com.example.cabinetmedical;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PatientStore {
    private static final String PREFS = "cabinet_prefs";
    private static final String KEY_PATIENTS = "patients";
    private static final String KEY_SEEDED = "seeded";

    public static List<Patient> load(Context context) {
        List<Patient> list = new ArrayList<>();
        String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PATIENTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                list.add(new Patient(
                        o.getString("id"),
                        o.optString("nom"),
                        o.optInt("age"),
                        o.optString("telephone"),
                        o.optString("notes"),
                        o.optString("rdv"),
                        o.optString("groupe"),
                        o.optString("allergies")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void save(Context context, List<Patient> list) {
        JSONArray arr = new JSONArray();
        for (Patient p : list) {
            JSONObject o = new JSONObject();
            try {
                o.put("id", p.id);
                o.put("nom", p.nom);
                o.put("age", p.age);
                o.put("telephone", p.telephone);
                o.put("notes", p.notes);
                o.put("rdv", p.rdv);
                o.put("groupe", p.groupe);
                o.put("allergies", p.allergies);
                arr.put(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY_PATIENTS, arr.toString()).apply();
    }

    public static void seedIfFirstLaunch(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            return;
        }
        List<Patient> list = new ArrayList<>();
        list.add(new Patient("p1", "Ahmed Ben Ali", 45, "22345678", "", ""));
        list.add(new Patient("p2", "Sarra Mansour", 32, "55123456", "", ""));
        list.add(new Patient("p3", "Youssef Trabelsi", 58, "98765432", "", ""));
        save(context, list);
        prefs.edit().putBoolean(KEY_SEEDED, true).apply();
    }

    /** Donnees de demonstration supplementaires (rendez-vous, notes), une seule fois. */
    public static void seedDemoV2(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean("demo_v2", false)) {
            return;
        }
        List<Patient> list = load(context);
        for (int i = 0; i < list.size(); i++) {
            Patient p = list.get(i);
            if (!p.rdv.isEmpty()) {
                continue;
            }
            if ("p1".equals(p.id)) {
                list.set(i, new Patient(p.id, p.nom, p.age, p.telephone,
                        p.notes.isEmpty() ? "Diabète type 2, suivi régulier" : p.notes,
                        DateUtils.addDays(0), p.groupe, p.allergies));
            } else if ("p2".equals(p.id)) {
                list.set(i, new Patient(p.id, p.nom, p.age, p.telephone,
                        p.notes.isEmpty() ? "Allergie à la pénicilline" : p.notes,
                        DateUtils.addDays(2), p.groupe, p.allergies));
            } else if ("p3".equals(p.id)) {
                list.set(i, new Patient(p.id, p.nom, p.age, p.telephone,
                        p.notes.isEmpty() ? "Hypertension artérielle" : p.notes,
                        DateUtils.addDays(-2), p.groupe, p.allergies));
            }
        }
        save(context, list);
        prefs.edit().putBoolean("demo_v2", true).apply();
    }

    /** Groupe sanguin et allergies de demonstration, une seule fois. */
    public static void seedDemoV3(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean("demo_v3", false)) {
            return;
        }
        List<Patient> list = load(context);
        for (int i = 0; i < list.size(); i++) {
            Patient p = list.get(i);
            String groupe = p.groupe;
            String allergies = p.allergies;
            if ("p1".equals(p.id)) {
                groupe = groupe.isEmpty() ? "A+" : groupe;
            } else if ("p2".equals(p.id)) {
                groupe = groupe.isEmpty() ? "O-" : groupe;
                allergies = allergies.isEmpty() ? "Pénicilline" : allergies;
            } else if ("p3".equals(p.id)) {
                groupe = groupe.isEmpty() ? "B+" : groupe;
            } else {
                continue;
            }
            list.set(i, new Patient(p.id, p.nom, p.age, p.telephone, p.notes, p.rdv,
                    groupe, allergies));
        }
        save(context, list);
        prefs.edit().putBoolean("demo_v3", true).apply();
    }

    public static void clearAll(Context context) {
        save(context, new ArrayList<Patient>());
    }
}
