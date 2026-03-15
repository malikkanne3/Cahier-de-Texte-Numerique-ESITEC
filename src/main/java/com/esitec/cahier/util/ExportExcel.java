package com.esitec.cahier.util;

import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportExcel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String genererFicheSuivi(Cours cours, List<Seance> seances, String cheminFichier) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet("Fiche de suivi");

        // ── Couleurs ────────────────────────────────────────
        byte[] BLEU   = {26, 75, (byte)153};
        byte[] BLEU_F = {10, 20, 45};
        byte[] BLANC  = {(byte)255,(byte)255,(byte)255};
        byte[] GRIS_C = {(byte)245,(byte)247,(byte)252};
        byte[] GRIS_L = {(byte)230,(byte)236,(byte)248};
        byte[] VERT   = {(byte)230,(byte)255,(byte)240};
        byte[] ORANGE = {(byte)255,(byte)248,(byte)220};
        byte[] ROUGE  = {(byte)255,(byte)235,(byte)235};
        byte[] VERT_T = {0,(byte)130,80};
        byte[] ORA_T  = {(byte)180,(byte)100,0};
        byte[] ROU_T  = {(byte)180,40,40};
        byte[] BLEU_T = {26,75,(byte)153};
        byte[] GRIS_T = {80,80,80};

        // ── Styles ───────────────────────────────────────────
        // Bandeau principal
        CellStyle sBandeau = style(wb, BLEU, BLANC, 16, true, HorizontalAlignment.CENTER, BorderStyle.NONE);

        // Sous-titre bandeau
        CellStyle sSousBandeau = style(wb, BLEU, new byte[]{(byte)180,(byte)210,(byte)255}, 10, false, HorizontalAlignment.CENTER, BorderStyle.NONE);

        // Section titre
        CellStyle sSection = style(wb, GRIS_C, BLEU_T, 11, true, HorizontalAlignment.LEFT, BorderStyle.THIN);

        // Label info cours
        CellStyle sLabel = style(wb, GRIS_L, new byte[]{60,80,(byte)130}, 10, true, HorizontalAlignment.LEFT, BorderStyle.THIN);

        // Valeur info cours
        CellStyle sValeur = style(wb, BLANC, new byte[]{0,0,0}, 10, false, HorizontalAlignment.LEFT, BorderStyle.THIN);

        // Valeur info cours (lignes alternées)
        CellStyle sValeurAlt = style(wb, GRIS_L, new byte[]{0,0,0}, 10, false, HorizontalAlignment.LEFT, BorderStyle.THIN);

        // Entête tableau
        CellStyle sEntete = style(wb, BLEU_F, BLANC, 9, true, HorizontalAlignment.CENTER, BorderStyle.THIN);

        // Cellule normale
        CellStyle sCellule = style(wb, BLANC, new byte[]{0,0,0}, 9, false, HorizontalAlignment.LEFT, BorderStyle.THIN);
        CellStyle sCelluleAlt = style(wb, GRIS_L, new byte[]{0,0,0}, 9, false, HorizontalAlignment.LEFT, BorderStyle.THIN);
        CellStyle sCelluleC = style(wb, BLANC, new byte[]{0,0,0}, 9, false, HorizontalAlignment.CENTER, BorderStyle.THIN);
        CellStyle sCelluleCAlt = style(wb, GRIS_L, new byte[]{0,0,0}, 9, false, HorizontalAlignment.CENTER, BorderStyle.THIN);

        // Statuts
        CellStyle sValide  = style(wb, VERT,   VERT_T, 9, true, HorizontalAlignment.CENTER, BorderStyle.THIN);
        CellStyle sAttente = style(wb, ORANGE, ORA_T,  9, true, HorizontalAlignment.CENTER, BorderStyle.THIN);
        CellStyle sRejete  = style(wb, ROUGE,  ROU_T,  9, true, HorizontalAlignment.CENTER, BorderStyle.THIN);

        // Footer
        CellStyle sFooter = style(wb, BLANC, GRIS_T, 8, false, HorizontalAlignment.RIGHT, BorderStyle.NONE);
        XSSFFont fFooter = wb.createFont();
        fFooter.setItalic(true); fFooter.setFontHeightInPoints((short)8);
        sFooter.setFont(fFooter);

        int r = 0;

        // ── Bandeau titre ────────────────────────────────────
        Row rTitre = sh.createRow(r++); rTitre.setHeightInPoints(32);
        Cell cTitre = rTitre.createCell(0);
        cTitre.setCellValue("FICHE DE SUIVI PÉDAGOGIQUE");
        cTitre.setCellStyle(sBandeau);
        sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,5));

        Row rSous = sh.createRow(r++); rSous.setHeightInPoints(18);
        Cell cSous = rSous.createCell(0);
        cSous.setCellValue("ESITEC — Groupe SUP de CO Dakar  ·  " + cours.getIntitule() + "  ·  " + cours.getClasseNom());
        cSous.setCellStyle(sSousBandeau);
        sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,5));
        r++; // Ligne vide

        // ── Section infos cours ───────────────────────────────
        Row rSec1 = sh.createRow(r++); rSec1.setHeightInPoints(22);
        Cell cSec1 = rSec1.createCell(0);
        cSec1.setCellValue("INFORMATIONS DU COURS");
        cSec1.setCellStyle(sSection);
        sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,5));

        String[][] infos = {
            {"Intitulé du cours", cours.getIntitule()},
            {"Enseignant",        cours.getNomEnseignant()},
            {"Classe",            cours.getClasseNom()},
            {"Volume horaire prévu",   cours.getVolumeHorairePrevu() + " heures"},
            {"Volume horaire effectué", cours.getVolumeHoraireEffectue() + " heures"},
            {"Taux d'avancement",  String.format("%.1f%%", cours.getPourcentageAvancement())}
        };
        boolean altInfo = false;
        for (String[] info : infos) {
            Row row = sh.createRow(r++); row.setHeightInPoints(18);
            Cell c1 = row.createCell(0); c1.setCellValue(info[0]); c1.setCellStyle(sLabel);
            sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,1));
            Cell c2 = row.createCell(2); c2.setCellValue(info[1]);
            c2.setCellStyle(altInfo ? sValeurAlt : sValeur);
            sh.addMergedRegion(new CellRangeAddress(r-1,r-1,2,5));
            altInfo = !altInfo;
        }
        r++; // Ligne vide

        // ── Section séances ───────────────────────────────────
        Row rSec2 = sh.createRow(r++); rSec2.setHeightInPoints(22);
        Cell cSec2 = rSec2.createCell(0);
        cSec2.setCellValue("DÉTAIL DES SÉANCES");
        cSec2.setCellStyle(sSection);
        sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,5));

        String[] cols = {"Date","Heure","Durée","Contenu","Observations","Statut"};
        Row rEnt = sh.createRow(r++); rEnt.setHeightInPoints(22);
        for (int i=0; i<cols.length; i++) {
            Cell c = rEnt.createCell(i); c.setCellValue(cols[i]); c.setCellStyle(sEntete);
        }

        if (seances.isEmpty()) {
            Row rv = sh.createRow(r++);
            Cell cv = rv.createCell(0); cv.setCellValue("Aucune séance enregistrée pour ce cours.");
            cv.setCellStyle(sCellule);
            sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,5));
        } else {
            boolean alt = false;
            for (Seance s : seances) {
                Row row = sh.createRow(r++); row.setHeightInPoints(18);
                CellStyle cs  = alt ? sCelluleCAlt : sCelluleC;
                CellStyle cst = alt ? sCelluleAlt  : sCellule;
                row.createCell(0).setCellValue(s.getDate().format(DATE_FMT));  row.getCell(0).setCellStyle(cs);
                row.createCell(1).setCellValue(s.getHeure().toString());        row.getCell(1).setCellStyle(cs);
                row.createCell(2).setCellValue(s.getDureeFormatee());           row.getCell(2).setCellStyle(cs);
                row.createCell(3).setCellValue(s.getContenu());                 row.getCell(3).setCellStyle(cst);
                row.createCell(4).setCellValue(s.getObservations()!=null?s.getObservations():"—"); row.getCell(4).setCellStyle(cst);
                Cell cStat = row.createCell(5);
                cStat.setCellValue(s.getStatut().name());
                cStat.setCellStyle(switch(s.getStatut()){case VALIDE->sValide; case REJETE->sRejete; default->sAttente;});
                alt = !alt;
            }
        }
        r++; // Ligne vide

        // ── Récapitulatif ─────────────────────────────────────
        Row rSec3 = sh.createRow(r++); rSec3.setHeightInPoints(22);
        Cell cSec3 = rSec3.createCell(0);
        cSec3.setCellValue("RÉCAPITULATIF");
        cSec3.setCellStyle(sSection);
        sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,5));

        long valides = seances.stream().filter(s->s.getStatut()==Seance.Statut.VALIDE).count();
        long attente = seances.stream().filter(s->s.getStatut()==Seance.Statut.EN_ATTENTE).count();
        long rejetes = seances.stream().filter(s->s.getStatut()==Seance.Statut.REJETE).count();

        String[][] recap = {
            {"Total des séances",   String.valueOf(seances.size())},
            {"Séances validées",    String.valueOf(valides)},
            {"Séances en attente",  String.valueOf(attente)},
            {"Séances rejetées",    String.valueOf(rejetes)},
            {"Progression globale", String.format("%.1f%%", cours.getPourcentageAvancement())}
        };
        altInfo = false;
        for (String[] rec : recap) {
            Row row = sh.createRow(r++); row.setHeightInPoints(18);
            Cell c1 = row.createCell(0); c1.setCellValue(rec[0]); c1.setCellStyle(sLabel);
            sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,1));
            Cell c2 = row.createCell(2); c2.setCellValue(rec[1]);
            c2.setCellStyle(altInfo ? sValeurAlt : sValeur);
            sh.addMergedRegion(new CellRangeAddress(r-1,r-1,2,3));
            altInfo = !altInfo;
        }
        r++;

        // ── Footer ────────────────────────────────────────────
        Row rFoot = sh.createRow(r++);
        Cell cFoot = rFoot.createCell(0);
        cFoot.setCellValue("Document généré le " + java.time.LocalDate.now().format(DATE_FMT)
            + "  —  ESITEC, Groupe SUP de CO Dakar  —  Cahier de Texte Numérique");
        cFoot.setCellStyle(sFooter);
        sh.addMergedRegion(new CellRangeAddress(r-1,r-1,0,5));

        // ── Largeurs colonnes ─────────────────────────────────
        sh.setColumnWidth(0, 3200);
        sh.setColumnWidth(1, 2600);
        sh.setColumnWidth(2, 2600);
        sh.setColumnWidth(3, 11000);
        sh.setColumnWidth(4, 7500);
        sh.setColumnWidth(5, 3200);

        FileOutputStream fos = new FileOutputStream(cheminFichier);
        wb.write(fos); fos.close(); wb.close();
        return cheminFichier;
    }

    private static CellStyle style(XSSFWorkbook wb, byte[] bg, byte[] fg,
                                    int size, boolean bold,
                                    HorizontalAlignment align, BorderStyle border) {
        XSSFCellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(bg, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setWrapText(true);
        if (border != BorderStyle.NONE) {
            s.setBorderTop(border); s.setBorderBottom(border);
            s.setBorderLeft(border); s.setBorderRight(border);
            s.setTopBorderColor(new XSSFColor(new byte[]{(byte)200,(byte)210,(byte)230}, null));
            s.setBottomBorderColor(new XSSFColor(new byte[]{(byte)200,(byte)210,(byte)230}, null));
        }
        XSSFFont f = wb.createFont();
        f.setColor(new XSSFColor(fg, null));
        f.setFontHeightInPoints((short) size);
        f.setBold(bold);
        f.setFontName("Calibri");
        s.setFont(f);
        return s;
    }
}
