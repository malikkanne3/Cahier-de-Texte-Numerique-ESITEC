package com.esitec.cahier.util;

import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportPDF {

    // Couleurs
    private static final BaseColor BLEU_ESITEC  = new BaseColor(26, 75, 153);
    private static final BaseColor BLEU_FONCE   = new BaseColor(10, 20, 45);
    private static final BaseColor GRIS_CLAIR   = new BaseColor(245, 247, 252);
    private static final BaseColor GRIS_LIGNE   = new BaseColor(230, 236, 248);
    private static final BaseColor VERT         = new BaseColor(0, 130, 80);
    private static final BaseColor ORANGE       = new BaseColor(200, 120, 0);
    private static final BaseColor ROUGE        = new BaseColor(200, 50, 50);

    // Polices — toutes avec couleur explicite NOIRE (lisible sur fond blanc)
    private static final Font F_HEADER_GRAND = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD,  BaseColor.WHITE);
    private static final Font F_HEADER_SMALL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(180,210,255));
    private static final Font F_SECTION      = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,  BLEU_ESITEC);
    private static final Font F_LABEL        = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,  new BaseColor(60, 80, 130));
    private static final Font F_VALEUR       = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font F_ENTETE_TAB   = new Font(Font.FontFamily.HELVETICA, 9,  Font.BOLD,  BaseColor.WHITE);
    private static final Font F_CELLULE      = new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, BaseColor.BLACK);
    private static final Font F_FOOTER       = new Font(Font.FontFamily.HELVETICA, 8,  Font.ITALIC, BaseColor.GRAY);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static String genererFicheSuivi(Cours cours, List<Seance> seances, String cheminFichier) throws Exception {
        Document doc = new Document(PageSize.A4, 36, 36, 50, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(cheminFichier));
        doc.open();

        ajouterBandeau(doc, cours);
        doc.add(Chunk.NEWLINE);
        ajouterSection(doc, "INFORMATIONS DU COURS");
        ajouterInfosCours(doc, cours);
        doc.add(Chunk.NEWLINE);
        ajouterSection(doc, "DÉTAIL DES SÉANCES");
        ajouterTableauSeances(doc, seances);
        doc.add(Chunk.NEWLINE);
        ajouterSection(doc, "RÉCAPITULATIF");
        ajouterRecapitulatif(doc, cours, seances);
        ajouterPiedDePage(doc);

        doc.close();
        return cheminFichier;
    }

    private static void ajouterBandeau(Document doc, Cours cours) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BLEU_ESITEC);
        cell.setPaddingTop(18); cell.setPaddingBottom(14);
        cell.setPaddingLeft(20); cell.setPaddingRight(20);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph t1 = new Paragraph("FICHE DE SUIVI PÉDAGOGIQUE", F_HEADER_GRAND);
        t1.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(t1);

        Paragraph t2 = new Paragraph("ESITEC — Groupe SUP de CO Dakar", F_HEADER_SMALL);
        t2.setAlignment(Element.ALIGN_CENTER);
        t2.setSpacingBefore(3);
        cell.addElement(t2);

        // Sous-bandeau cours
        Paragraph t3 = new Paragraph(cours.getIntitule().toUpperCase() + "  ·  " + cours.getClasseNom(),
                new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(200, 220, 255)));
        t3.setAlignment(Element.ALIGN_CENTER);
        t3.setSpacingBefore(5);
        cell.addElement(t3);

        header.addCell(cell);
        doc.add(header);
    }

    private static void ajouterSection(Document doc, String texte) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4);
        PdfPCell c = new PdfPCell(new Phrase(texte, F_SECTION));
        c.setBackgroundColor(GRIS_CLAIR);
        c.setBorderColor(BLEU_ESITEC);
        c.setBorderWidthBottom(2f);
        c.setBorderWidthTop(0); c.setBorderWidthLeft(0); c.setBorderWidthRight(0);
        c.setPadding(7);
        t.addCell(c);
        doc.add(t);
    }

    private static void ajouterInfosCours(Document doc, Cours cours) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{2f, 3f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(3);

        ligneInfo(table, "Intitulé du cours",      cours.getIntitule(), false);
        ligneInfo(table, "Enseignant",             cours.getNomEnseignant(), true);
        ligneInfo(table, "Classe",                 cours.getClasseNom(), false);
        ligneInfo(table, "Volume horaire prévu",   cours.getVolumeHorairePrevu() + " heures", true);
        ligneInfo(table, "Volume horaire effectué", cours.getVolumeHoraireEffectue() + " heures", false);
        ligneInfo(table, "Taux d'avancement",
                String.format("%.1f%%", cours.getPourcentageAvancement()), true);
        doc.add(table);
    }

    private static void ligneInfo(PdfPTable table, String label, String valeur, boolean alt) {
        BaseColor bg = alt ? GRIS_LIGNE : BaseColor.WHITE;

        PdfPCell cLabel = new PdfPCell(new Phrase(label, F_LABEL));
        cLabel.setBackgroundColor(bg);
        cLabel.setPadding(7); cLabel.setBorderColor(GRIS_LIGNE);
        table.addCell(cLabel);

        PdfPCell cVal = new PdfPCell(new Phrase(valeur != null ? valeur : "—", F_VALEUR));
        cVal.setBackgroundColor(bg);
        cVal.setPadding(7); cVal.setBorderColor(GRIS_LIGNE);
        table.addCell(cVal);
    }

    private static void ajouterTableauSeances(Document doc, List<Seance> seances) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[]{1.8f, 1.3f, 1f, 3.8f, 2f, 1.5f});
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        // En-têtes
        for (String h : new String[]{"Date", "Heure", "Durée", "Contenu", "Observations", "Statut"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, F_ENTETE_TAB));
            cell.setBackgroundColor(BLEU_FONCE);
            cell.setPadding(7);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(cell);
        }

        if (seances.isEmpty()) {
            PdfPCell vide = new PdfPCell(new Phrase("Aucune séance enregistrée pour ce cours.", F_CELLULE));
            vide.setColspan(6); vide.setPadding(12);
            vide.setHorizontalAlignment(Element.ALIGN_CENTER);
            vide.setBackgroundColor(GRIS_CLAIR);
            table.addCell(vide);
        } else {
            boolean alt = false;
            for (Seance s : seances) {
                BaseColor bg = alt ? GRIS_LIGNE : BaseColor.WHITE;
                cellule(table, s.getDate().format(DATE_FMT), bg, F_CELLULE, Element.ALIGN_CENTER);
                cellule(table, s.getHeure().toString(),       bg, F_CELLULE, Element.ALIGN_CENTER);
                cellule(table, s.getDureeFormatee(),           bg, F_CELLULE, Element.ALIGN_CENTER);
                cellule(table, s.getContenu(),                 bg, F_CELLULE, Element.ALIGN_LEFT);
                cellule(table, s.getObservations() != null ? s.getObservations() : "—", bg, F_CELLULE, Element.ALIGN_LEFT);

                // Statut avec couleur
                Font fStat = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
                BaseColor bgStat;
                switch (s.getStatut()) {
                    case VALIDE    -> { fStat.setColor(VERT);   bgStat = new BaseColor(230,255,240); }
                    case REJETE    -> { fStat.setColor(ROUGE);  bgStat = new BaseColor(255,235,235); }
                    default        -> { fStat.setColor(ORANGE); bgStat = new BaseColor(255,248,220); }
                }
                PdfPCell cs = new PdfPCell(new Phrase(s.getStatut().name(), fStat));
                cs.setBackgroundColor(bgStat); cs.setPadding(6);
                cs.setHorizontalAlignment(Element.ALIGN_CENTER);
                cs.setBorderColor(GRIS_LIGNE);
                table.addCell(cs);
                alt = !alt;
            }
        }
        doc.add(table);
    }

    private static void cellule(PdfPTable table, String txt, BaseColor bg, Font f, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt != null ? txt : "—", f));
        c.setBackgroundColor(bg); c.setPadding(6);
        c.setHorizontalAlignment(align);
        c.setBorderColor(GRIS_LIGNE);
        table.addCell(c);
    }

    private static void ajouterRecapitulatif(Document doc, Cours cours, List<Seance> seances) throws DocumentException {
        long valides  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.VALIDE).count();
        long attente  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.EN_ATTENTE).count();
        long rejetes  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.REJETE).count();

        PdfPTable table = new PdfPTable(new float[]{2.5f, 1.5f});
        table.setWidthPercentage(55);
        table.setSpacingBefore(4);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        ligneInfo(table, "Total des séances",    String.valueOf(seances.size()), false);
        ligneInfo(table, "Séances validées",     String.valueOf(valides),  true);
        ligneInfo(table, "Séances en attente",   String.valueOf(attente),  false);
        ligneInfo(table, "Séances rejetées",     String.valueOf(rejetes),  true);
        ligneInfo(table, "Progression globale",
                String.format("%.1f%%", cours.getPourcentageAvancement()), false);
        doc.add(table);
    }

    private static void ajouterPiedDePage(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph(
            "Document généré le " + java.time.LocalDate.now().format(DATE_FMT)
            + "  —  ESITEC, Groupe SUP de CO Dakar  —  Cahier de Texte Numérique", F_FOOTER);
        footer.setAlignment(Element.ALIGN_RIGHT);
        doc.add(footer);
    }
}
