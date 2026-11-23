package com.example.cabinetmedical;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Petite boîte à outils pour travailler avec les dates (format jj/MM/aaaa).
 * Elle sait lire une date, l'écrire, calculer le nombre de jours d'écart
 * et choisir un texte et une couleur selon l'urgence d'un rendez-vous.
 * Elle est utilisée par HomeActivity, StatsActivity et d'autres écrans.
 * Toutes les méthodes sont "static" : on les appelle directement, sans créer d'objet.
 */
public class DateUtils {

    // Valeur spéciale qui veut dire "pas de date" (le plus petit nombre possible)
    public static final long NO_DATE = Long.MIN_VALUE;

    /**
     * Transforme un texte comme "21/11/2026" en objet Date.
     * Renvoie null si le texte est vide ou si la date est invalide.
     */
    public static Date parse(String s) {
        // Si le texte est absent (null) ou ne contient que des espaces
        if (s == null || s.trim().isEmpty()) {
            // On n'a pas de date : on renvoie null
            return null;
        }
        // On crée un "lecteur" de dates avec le format jour/mois/année en français
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        // Mode strict : une date impossible comme 32/13/2026 sera refusée
        f.setLenient(false);
        // try/catch : on essaie, et si ça échoue on gère l'erreur sans faire planter l'appli
        try {
            // On lit le texte (sans espaces autour) et on renvoie la Date obtenue
            return f.parse(s.trim());
        } catch (ParseException e) {
            // Le texte n'est pas une vraie date : on renvoie null
            return null;
        }
    }

    /**
     * Transforme un Calendar en texte au format jj/MM/aaaa.
     */
    public static String format(Calendar c) {
        // %02d = nombre sur 2 chiffres (ex. 05) ; %04d = nombre sur 4 chiffres
        // Les mois commencent à 0 en Java, donc on ajoute 1 pour avoir le vrai numéro
        return String.format(Locale.FRANCE, "%02d/%02d/%04d",
                c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    /**
     * Donne la date d'aujourd'hui plus n jours (n peut être négatif), sous forme de texte.
     */
    public static String addDays(int n) {
        // On prend la date et l'heure actuelles
        Calendar c = Calendar.getInstance();
        // On ajoute n jours à cette date
        c.add(Calendar.DAY_OF_MONTH, n);
        // On renvoie le résultat écrit en jj/MM/aaaa
        return format(c);
    }

    /**
     * Donne la date d'aujourd'hui sous forme de texte.
     */
    public static String today() {
        // Aujourd'hui = aujourd'hui + 0 jour
        return addDays(0);
    }

    /**
     * Remet l'heure à 00:00:00 pour ne comparer que les jours, pas les heures.
     */
    private static void zero(Calendar c) {
        // On met l'heure à 0
        c.set(Calendar.HOUR_OF_DAY, 0);
        // On met les minutes à 0
        c.set(Calendar.MINUTE, 0);
        // On met les secondes à 0
        c.set(Calendar.SECOND, 0);
        // On met les millisecondes à 0
        c.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Nombre de jours entre aujourd'hui et la date (negatif = passe). NO_DATE si invalide.
     * Exemple : demain donne 1, hier donne -1.
     */
    public static long daysFromToday(String s) {
        // On transforme le texte en Date
        Date d = parse(s);
        // Si la date est invalide (parse a renvoyé null)
        if (d == null) {
            // On renvoie la valeur spéciale "pas de date"
            return NO_DATE;
        }
        // "a" = aujourd'hui
        Calendar a = Calendar.getInstance();
        // On remet son heure à minuit pour comparer seulement les jours
        zero(a);
        // "b" = la date donnée
        Calendar b = Calendar.getInstance();
        // On place la date lue dans "b"
        b.setTime(d);
        // On remet aussi son heure à minuit
        zero(b);
        // On calcule la différence en millisecondes, puis on divise par le nombre de
        // millisecondes dans un jour (86 400 000) et on arrondit pour avoir des jours
        return Math.round((b.getTimeInMillis() - a.getTimeInMillis()) / 86400000.0);
    }

    /**
     * Transforme un nombre de jours en phrase simple ("Demain", "Dans 3 jours", etc.).
     */
    public static String describe(long d) {
        // Si pas de date
        if (d == NO_DATE) {
            // On renvoie un texte vide
            return "";
        }
        // Si c'est aujourd'hui
        if (d == 0) {
            // On renvoie "Aujourd'hui"
            return "Aujourd'hui";
        }
        // Si c'est dans 1 jour
        if (d == 1) {
            // On renvoie "Demain"
            return "Demain";
        }
        // Si c'était il y a 1 jour
        if (d == -1) {
            // On renvoie "Hier"
            return "Hier";
        }
        // Si c'est dans plus d'un jour
        if (d > 1) {
            // Exemple : "Dans 5 jours"
            return "Dans " + d + " jours";
        }
        // Dernier cas : la date est passée depuis plus d'un jour
        // On écrit -d pour afficher un nombre positif (exemple : "En retard de 3 jours")
        return "En retard de " + (-d) + " jours";
    }

    /**
     * Choisit une couleur selon l'urgence du rendez-vous.
     * Les couleurs sont écrites en hexadécimal : 0xFF puis rouge, vert, bleu.
     */
    public static int color(long d) {
        // Pas de date : gris
        if (d == NO_DATE) {
            // Code couleur du gris
            return 0xFF666666;
        }
        // Rendez-vous déjà passé : rouge
        if (d < 0) {
            // Code couleur du rouge
            return 0xFFD32F2F;
        }
        // Rendez-vous aujourd'hui : orange
        if (d == 0) {
            // Code couleur de l'orange
            return 0xFFEF6C00;
        }
        // Rendez-vous futur : bleu
        return 0xFF1976D2;
    }

    /**
     * Cle de mois (annee * 12 + mois) d'une date jj/MM/aaaa, ou -1 si invalide.
     * Cette clé est un seul nombre qui permet de comparer facilement deux mois.
     */
    public static int monthKey(String s) {
        // On transforme le texte en Date (java.util.Date écrit avec son nom complet)
        java.util.Date d = parse(s);
        // Si la date est invalide
        if (d == null) {
            // On renvoie -1 pour dire "pas de mois"
            return -1;
        }
        // On crée un Calendar pour lire l'année et le mois
        Calendar c = Calendar.getInstance();
        // On place la date dans le Calendar
        c.setTime(d);
        // Année * 12 + numéro du mois (de 0 à 11) = un nombre unique pour chaque mois
        return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH);
    }

    /**
     * Donne la clé du mois actuel (même calcul que monthKey, mais pour aujourd'hui).
     */
    public static int currentMonthKey() {
        // On prend la date et l'heure actuelles
        Calendar c = Calendar.getInstance();
        // Année * 12 + numéro du mois en cours
        return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH);
    }
}
