package com.esitec.cahier.service;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EmailService {

    private static final String API_KEY        = "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";
    private static final String API_URL        = "https://api.brevo.com/v3/smtp/email";
    private static final String EXPEDITEUR     = "saymonkanne3@gmail.com";
    private static final String NOM_EXPEDITEUR = "ESITEC &#8212; Cahier de Texte";

    private static EmailService instance;
    private EmailService() {}
    public static EmailService getInstance() {
        if (instance == null) instance = new EmailService();
        return instance;
    }

    public boolean envoyerEmail(String destEmail, String destNom, String sujet, String htmlBody) {
        if (destEmail == null || destEmail.isBlank() || !destEmail.contains("@")) {
            System.err.println("[EMAIL] Destinataire invalide : " + destEmail); return false;
        }
        System.out.println("[EMAIL] Envoi -> " + destEmail + " | " + sujet);
        try {
            String json = buildJson(destEmail, destNom, sujet, htmlBody);
            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("api-key", API_KEY);
            conn.setConnectTimeout(10000); conn.setReadTimeout(15000); conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if (code == 201 || code == 200) { System.out.println("[EMAIL] OK -> " + destEmail); return true; }
            else {
                InputStream es = conn.getErrorStream();
                String err = es != null ? new String(es.readAllBytes()) : "";
                System.err.println("[EMAIL] Erreur HTTP " + code + " : " + err); return false;
            }
        } catch (UnknownHostException e) { System.err.println("[EMAIL] Pas de connexion."); return false;
        } catch (Exception e) { System.err.println("[EMAIL] Exception : " + e.getMessage()); return false; }
    }

    private String buildJson(String destEmail, String destNom, String sujet, String html) {
        return "{\"sender\":{\"name\":\"" + je(NOM_EXPEDITEUR) + "\",\"email\":\"" + je(EXPEDITEUR) + "\"},"
             + "\"to\":[{\"email\":\"" + je(destEmail) + "\",\"name\":\"" + je(destNom != null ? destNom : destEmail) + "\"}],"
             + "\"subject\":\"" + je(sujet) + "\","
             + "\"htmlContent\":\"" + je(html) + "\"}";
    }
    private String je(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t");
    }

    // ═══════════════════════════════════════════════════════════════
    //   TEMPLATES
    // ═══════════════════════════════════════════════════════════════

    public String templateNouvelleSeance(String nomDest, String nomEns, String cours, String classe, String date) {
        String details = row("Enseignant", nomEns, false)
                       + row("Cours",      "<b>" + cours + "</b>", true)
                       + row("Classe",     classe, false)
                       + row("Date de la s&eacute;ance", date, true)
                       + row("Statut",     badge("EN ATTENTE", "#FF9800"), false);
        String corps =
            paragraph("Une nouvelle s&eacute;ance vient d&#39;&ecirc;tre soumise par un enseignant et "
                + "<b>attend votre validation</b>. Veuillez examiner les informations ci-dessous "
                + "et vous connecter &agrave; l&#39;application ESITEC pour l&#39;approuver ou la rejeter.")
            + infoTable("D&Eacute;TAILS DE LA S&Eacute;ANCE", details)
            + tip("#FFF3E0","#E65100",
                "Action requise",
                "Connectez-vous en tant que <b>responsable de classe</b> ou <b>chef de d&eacute;partement</b> "
                + "pour valider ou rejeter cette s&eacute;ance. "
                + "Un rejet devra obligatoirement &ecirc;tre accompagn&eacute; d&#39;un motif d&eacute;taill&eacute;.")
            + cta("Valider la s&eacute;ance", "#FF9800");
        return page("Nouvelle s&eacute;ance &agrave; valider", "#FF9800",
            topBar("#FF9800", "#FF6D00", "&#9203;", "Nouvelle s&eacute;ance", "Action requise &mdash; Validation n&eacute;cessaire"),
            nomDest, corps);
    }

    public String templateSeanceValidee(String nomDest, String cours, String classe, String date) {
        String details = row("Cours",   "<b>" + cours + "</b>", false)
                       + row("Classe",  classe, true)
                       + row("Date",    date, false)
                       + row("Statut",  badge("VALID&Eacute;E", "#00C878"), true);
        String corps =
            paragraph("Bonne nouvelle ! Votre s&eacute;ance a &eacute;t&eacute; <b>valid&eacute;e</b> "
                + "par le responsable de classe. Elle est d&eacute;sormais officiellement "
                + "enregistr&eacute;e dans le <b>Cahier de Texte Num&eacute;rique ESITEC</b> "
                + "et comptabilis&eacute;e dans le suivi p&eacute;dagogique de votre cours.")
            + infoTable("D&Eacute;TAILS DE LA S&Eacute;ANCE VALID&Eacute;E", details)
            + tip("#E8F5E9","#2E7D32",
                "S&eacute;ance enregistr&eacute;e avec succ&egrave;s",
                "Votre s&eacute;ance est maintenant prise en compte dans le volume horaire effectu&eacute;. "
                + "Vous pouvez suivre l&#39;avancement global de votre cours directement depuis "
                + "votre <b>tableau de bord enseignant</b>.")
            + cta("Voir mon tableau de bord", "#00C878");
        return page("S&eacute;ance valid&eacute;e !", "#00C878",
            topBar("#00C878", "#00897B", "&#10004;", "S&eacute;ance valid&eacute;e", "Confirmation &mdash; Enregistrement effectu&eacute;"),
            nomDest, corps);
    }

    public String templateSeanceRejetee(String nomDest, String cours, String classe, String date, String motif) {
        String details = row("Cours",   "<b>" + cours + "</b>", false)
                       + row("Classe",  classe, true)
                       + row("Date",    date, false)
                       + row("Statut",  badge("REJET&Eacute;E", "#FF4646"), true);
        String corps =
            paragraph("Votre s&eacute;ance a &eacute;t&eacute; <b>rejet&eacute;e</b> par le responsable "
                + "de classe. Veuillez lire attentivement le motif indiqu&eacute; ci-dessous, "
                + "effectuer les corrections n&eacute;cessaires et soumettre &agrave; nouveau "
                + "votre s&eacute;ance depuis votre espace enseignant.")
            + infoTable("D&Eacute;TAILS DE LA S&Eacute;ANCE", details)
            + "<table width='100%' cellpadding='0' cellspacing='0' style='margin:0 0 20px'><tr>"
            + "<td style='background:#FFF3F3;border:1px solid #FFCCCC;border-left:4px solid #FF4646;"
            +   "border-radius:0 8px 8px 0;padding:18px 20px'>"
            + "<div style='color:#C62828;font-size:11px;font-weight:700;text-transform:uppercase;"
            +   "letter-spacing:1px;margin-bottom:8px'>&#10006;&nbsp; Motif du rejet</div>"
            + "<div style='color:#4A3030;font-size:14px;line-height:1.7;font-style:italic'>"
            + "&ldquo;" + motif + "&rdquo;</div>"
            + "</td></tr></table>"
            + tip("#FFF3F3","#FF4646",
                "Correction requise",
                "Veuillez corriger les points mentionn&eacute;s et <b>soumettre &agrave; nouveau</b> "
                + "votre s&eacute;ance depuis votre espace enseignant. "
                + "En cas de doute, n&#39;h&eacute;sitez pas &agrave; contacter directement le responsable de classe.")
            + cta("Corriger ma s&eacute;ance", "#FF4646");
        return page("S&eacute;ance rejet&eacute;e", "#FF4646",
            topBar("#FF4646","#C62828","&#10006;","S&eacute;ance rejet&eacute;e","Correction n&eacute;cessaire"),
            nomDest, corps);
    }

    public String templateNouveauCompte(String nomDest, String nomUser, String emailUser, String role) {
        String roleLabel = switch(role) {
            case "ENSEIGNANT"         -> "Enseignant";
            case "RESPONSABLE_CLASSE" -> "Responsable de classe";
            case "CHEF_DEPARTEMENT"   -> "Chef de d&eacute;partement";
            default -> role;
        };
        String details = row("Nom complet",       "<b>" + nomUser + "</b>", false)
                       + row("Adresse email",      emailUser, true)
                       + row("R&ocirc;le demand&eacute;", roleLabel, false)
                       + row("Statut",             badge("EN ATTENTE", "#FF9800"), true);
        String corps =
            paragraph("Un nouvel utilisateur vient de cr&eacute;er un compte sur la plateforme "
                + "<b>ESITEC Cahier de Texte Num&eacute;rique</b>. Ce compte est actuellement "
                + "<b>en attente d&#39;activation</b> et ne permettra pas l&#39;acc&egrave;s "
                + "&agrave; l&#39;application tant qu&#39;il n&#39;aura pas &eacute;t&eacute; "
                + "valid&eacute; par un chef de d&eacute;partement.")
            + infoTable("INFORMATIONS DU NOUVEAU COMPTE", details)
            + tip("#E3F2FD","#1565C0",
                "Validation requise",
                "Connectez-vous en tant que <b>chef de d&eacute;partement</b> et acc&eacute;dez "
                + "&agrave; la section <b>Gestion &rsaquo; Utilisateurs</b> pour <b>valider</b> "
                + "ou <b>rejeter</b> ce compte. "
                + "Assurez-vous que l&#39;identit&eacute; de l&#39;utilisateur est bien v&eacute;rifi&eacute;e "
                + "avant toute activation.")
            + cta("G&eacute;rer les comptes", "#008CFF");
        return page("Nouveau compte &agrave; valider", "#008CFF",
            topBar("#008CFF","#0050AA","&#128100;","Nouveau compte utilisateur","Validation requise"),
            nomDest, corps);
    }

    public String templateAvancementEleve(String nomDest, String cours, String classe, String pct) {
        int val = 80;
        try { val = Integer.parseInt(pct.replace("%","").trim()); } catch(Exception ignored) {}
        String details = row("Cours",       "<b>" + cours + "</b>", false)
                       + row("Classe",      classe, true)
                       + row("Avancement",  "<b style='color:#00C878;font-size:16px'>" + pct + "</b>", false)
                       + row("Statut",      badge("AVANCEMENT &Eacute;LEV&Eacute;", "#00D2E6"), true);
        String barre =
            "<table width='100%' cellpadding='0' cellspacing='0' style='margin:0 0 20px'><tr>"
            + "<td style='background:#F0F9FC;border:1px solid #B2EBF2;border-radius:10px;padding:16px 20px'>"
            + "<div style='display:flex;justify-content:space-between;margin-bottom:10px'>"
            + "<span style='color:#4A6070;font-size:12px;font-weight:600'>Progression du programme</span>"
            + "<span style='color:#00C878;font-size:15px;font-weight:800'>" + pct + "</span></div>"
            + "<div style='background:#D0E8EE;border-radius:8px;height:12px;overflow:hidden'>"
            + "<div style='background:linear-gradient(90deg,#00C878,#00D2E6);"
            +   "width:" + Math.min(val,100) + "%;height:100%;border-radius:8px'></div></div>"
            + "</td></tr></table>";
        String corps =
            paragraph("Le suivi p&eacute;dagogique de la plateforme ESITEC indique que le cours "
                + "<b>" + cours + "</b> a atteint un avancement <b>sup&eacute;rieur &agrave; 80%</b>. "
                + "Il est conseill&eacute; de v&eacute;rifier que la progression reste "
                + "conforme au programme officiel et d&#39;anticiper la fin du semestre.")
            + infoTable("SUIVI P&Eacute;DAGOGIQUE", details)
            + barre
            + tip("#E0F7FA","#00838F",
                "Conseil p&eacute;dagogique",
                "Le programme de ce cours est presque enti&egrave;rement couvert. "
                + "Pensez &agrave; planifier les <b>r&eacute;visions finales</b>, "
                + "&agrave; pr&eacute;parer l&#39;<b>&eacute;valuation de fin de semestre</b> "
                + "et &agrave; v&eacute;rifier la conformit&eacute; avec le syllabus officiel.")
            + cta("Voir le cahier de texte", "#00D2E6");
        return page("Avancement &eacute;lev&eacute; &mdash; " + cours, "#00D2E6",
            topBar("#00D2E6","#007C91","&#128200;","Programme bient&ocirc;t termin&eacute;","Suivi p&eacute;dagogique"),
            nomDest, corps);
    }

    // ═══════════════════════════════════════════════════════════════
    //   COMPOSANTS HTML
    // ═══════════════════════════════════════════════════════════════

    private String page(String sujet, String accent, String banner, String nomDest, String corps) {
        String now = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy '\u00e0' HH:mm", Locale.FRENCH));
        // Initiales pour avatar
        String initiales = nomDest.length() >= 2
            ? (nomDest.substring(0,1) + (nomDest.contains(" ") ? nomDest.substring(nomDest.lastIndexOf(" ")+1,nomDest.lastIndexOf(" ")+2) : nomDest.substring(1,2))).toUpperCase()
            : nomDest.toUpperCase();
        return "<!DOCTYPE html><html lang='fr'>"
            + "<head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<title>" + sujet + "</title></head>"
            + "<body style='margin:0;padding:0;background:#EEF2F8;"
            +   "font-family:\"Segoe UI\",Helvetica,Arial,sans-serif'>"
            + "<table width='100%' cellpadding='0' cellspacing='0' border='0'"
            +   " style='background:#EEF2F8;padding:36px 0'>"
            + "<tr><td align='center'>"

            // Carte principale
            + "<table width='600' cellpadding='0' cellspacing='0' border='0'"
            +   " style='background:#FFFFFF;border-radius:18px;"
            +   "overflow:hidden;box-shadow:0 12px 48px rgba(8,20,60,0.18)'>"

            + banner

            // Salutation + corps
            + "<tr><td style='padding:36px 40px 0'>"
            // Avatar + nom
            + "<table cellpadding='0' cellspacing='0' style='margin-bottom:24px'><tr>"
            + "<td style='background:linear-gradient(135deg," + accent + ",#0A2560);"
            +   "border-radius:50%;width:46px;height:46px;text-align:center;vertical-align:middle'>"
            +   "<span style='color:#fff;font-size:15px;font-weight:800'>" + initiales + "</span>"
            + "</td>"
            + "<td style='padding-left:14px'>"
            +   "<div style='color:#8A9AB8;font-size:11px;text-transform:uppercase;"
            +     "letter-spacing:1px;margin-bottom:2px'>" + now + "</div>"
            +   "<div style='color:#0A1A3A;font-size:17px;font-weight:700'>Bonjour, <span style='color:" + accent + "'>" + nomDest + "</span> !</div>"
            + "</td></tr></table>"
            + corps
            + "</td></tr>"

            // Bouton déjà inclu dans corps, ici on ajoute l'espace
            + "<tr><td style='padding:0 40px 8px'></td></tr>"

            // Séparateur
            + "<tr><td style='padding:0 40px'>"
            + "<div style='border-top:1px solid #E8EDF5'></div>"
            + "</td></tr>"

            // Footer
            + "<tr><td style='background:linear-gradient(135deg,#06142E 0%,#0A2158 100%);"
            +   "padding:26px 40px;border-radius:0 0 18px 18px'>"
            + "<table width='100%' cellpadding='0' cellspacing='0'><tr>"
            + "<td style='vertical-align:middle'>"
            +   "<table cellpadding='0' cellspacing='0'><tr>"
            +   "<td style='background:rgba(255,255,255,0.10);border-radius:6px;"
            +     "padding:5px 12px;border:1px solid rgba(255,255,255,0.15)'>"
            +   "<span style='color:#fff;font-size:12px;font-weight:900;letter-spacing:2px'>ESITEC</span></td>"
            +   "<td style='padding-left:12px'>"
            +   "<div style='color:rgba(255,255,255,0.55);font-size:10px'>Groupe SUP de CO Dakar</div>"
            +   "<div style='color:rgba(255,255,255,0.35);font-size:9px'>Cahier de Texte Num&eacute;rique &mdash; 2025&ndash;2026</div>"
            +   "</td></tr></table>"
            + "</td>"
            + "<td style='text-align:right;vertical-align:middle'>"
            +   "<div style='color:rgba(255,255,255,0.30);font-size:10px;line-height:1.8'>"
            +   "Email automatique &mdash; ne pas r&eacute;pondre<br>"
            +   "&#169; 2025-2026 ESITEC &mdash; Tous droits r&eacute;serv&eacute;s"
            +   "</div>"
            + "</td></tr></table>"
            + "</td></tr>"

            + "</table></td></tr></table>"
            + "</body></html>";
    }

    private String topBar(String c1, String c2, String icone, String titre, String sousTitre) {
        return "<tr><td style='background:linear-gradient(135deg,#061535 0%,#0D2255 45%," + c1 + " 130%)'>"
            + "<table width='100%' cellpadding='0' cellspacing='0'>"

            // Ligne top : logo ESITEC
            + "<tr><td style='padding:22px 40px 0'>"
            + "<table cellpadding='0' cellspacing='0'><tr>"
            + "<td style='border-right:1px solid rgba(255,255,255,0.2);padding-right:14px;margin-right:14px'>"
            +   "<div style='color:#fff;font-size:16px;font-weight:900;letter-spacing:2.5px'>ESITEC</div>"
            +   "<div style='color:rgba(255,255,255,0.45);font-size:9px;letter-spacing:0.5px'>GROUPE SUP DE CO DAKAR</div>"
            + "</td>"
            + "<td style='padding-left:14px'>"
            +   "<div style='color:rgba(255,255,255,0.50);font-size:10px'>Cahier de Texte Num&eacute;rique</div>"
            + "</td></tr></table>"
            + "</td></tr>"

            // Ligne centrale : icone + titre
            + "<tr><td style='padding:22px 40px 28px'>"
            + "<table cellpadding='0' cellspacing='0'><tr>"
            + "<td style='background:" + c1 + ";border-radius:14px;width:60px;height:60px;"
            +   "text-align:center;vertical-align:middle;"
            +   "box-shadow:0 6px 20px rgba(0,0,0,0.35)'>"
            +   "<span style='font-size:28px;line-height:60px'>" + icone + "</span>"
            + "</td>"
            + "<td style='padding-left:20px'>"
            +   "<div style='color:#fff;font-size:24px;font-weight:800;letter-spacing:-0.5px;line-height:1.2'>"
            +   titre + "</div>"
            +   "<div style='margin-top:7px'>"
            +   "<span style='background:rgba(255,255,255,0.13);color:rgba(255,255,255,0.85);"
            +     "padding:4px 14px;border-radius:20px;font-size:11px;font-weight:600;"
            +     "border:1px solid rgba(255,255,255,0.22);letter-spacing:0.3px'>"
            +   sousTitre + "</span>"
            +   "</div>"
            + "</td></tr></table>"
            + "</td></tr>"

            + "</table></td></tr>";
    }

    private String paragraph(String text) {
        return "<p style='color:#3A4A6A;font-size:14px;line-height:1.85;margin:0 0 24px'>" + text + "</p>";
    }

    private String infoTable(String titre, String rows) {
        return "<table width='100%' cellpadding='0' cellspacing='0'"
            + " style='border-radius:12px;overflow:hidden;border:1px solid #E0E8F4;"
            + "margin-bottom:22px;box-shadow:0 2px 8px rgba(10,30,80,0.06)'>"
            // Header tableau
            + "<tr><td colspan='2'"
            +   " style='background:linear-gradient(90deg,#0A1E55,#1A3A8F);"
            +   "padding:13px 20px'>"
            +   "<span style='color:rgba(255,255,255,0.75);font-size:10px;"
            +   "font-weight:700;text-transform:uppercase;letter-spacing:1.2px'>"
            +   "&#128203;&nbsp; " + titre + "</span>"
            + "</td></tr>"
            + rows
            + "</table>";
    }

    private String row(String label, String val, boolean alt) {
        String bg = alt ? "#F7FAFF" : "#FFFFFF";
        return "<tr style='background:" + bg + "'>"
            + "<td style='padding:13px 20px;color:#8A9AB8;font-size:11px;"
            +   "text-transform:uppercase;letter-spacing:0.5px;width:160px;"
            +   "border-right:1px solid #EEF2F8;vertical-align:middle'>" + label + "</td>"
            + "<td style='padding:13px 20px;color:#0A1A3A;font-size:13px;"
            +   "font-weight:500;vertical-align:middle'>" + val + "</td>"
            + "</tr>";
    }

    private String tip(String bg, String accent, String titre, String texte) {
        return "<table width='100%' cellpadding='0' cellspacing='0'"
            + " style='margin-bottom:24px'><tr>"
            + "<td style='background:" + bg + ";border-left:4px solid " + accent + ";"
            +   "border-radius:0 10px 10px 0;padding:16px 20px'>"
            + "<div style='color:" + accent + ";font-size:11px;font-weight:700;"
            +   "text-transform:uppercase;letter-spacing:0.8px;margin-bottom:7px'>"
            +   "&#9432;&nbsp; " + titre + "</div>"
            + "<div style='color:#4A5A7A;font-size:13px;line-height:1.75'>" + texte + "</div>"
            + "</td></tr></table>";
    }

    private String cta(String label, String couleur) {
        return "<table width='100%' cellpadding='0' cellspacing='0'"
            + " style='margin-bottom:32px'><tr><td align='center'>"
            + "<span style='display:inline-block;"
            +   "background:linear-gradient(135deg," + couleur + " 0%," + couleur + "CC 100%);"
            +   "color:#ffffff;padding:15px 44px;border-radius:30px;"
            +   "font-size:14px;font-weight:700;letter-spacing:0.5px;"
            +   "box-shadow:0 8px 24px " + couleur + "55'>"
            +   label + " &rarr;"
            + "</span>"
            + "</td></tr></table>";
    }

    private String badge(String txt, String couleur) {
        return "<span style='background:" + couleur + "1A;color:" + couleur
             + ";border:1.5px solid " + couleur + "66;padding:4px 14px;"
             + "border-radius:20px;font-size:11px;font-weight:800;"
             + "letter-spacing:0.8px;text-transform:uppercase'>" + txt + "</span>";
    }
}
