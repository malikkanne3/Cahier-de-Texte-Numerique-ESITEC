package com.esitec.cahier.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Renderers de cellules — Thème sombre SUPDECO. */
public class TableRenderers {

    /** Badge coloré pour le statut (VALIDE / EN_ATTENTE / REJETE). */
    public static class StatutCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {

            String v = String.valueOf(val);
            Color bgRow = sel ? new Color(0,100,200,80)
                    : (row % 2 == 0 ? UIHelper.FOND_CARD : new Color(20,36,72));

            JPanel p = new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(bgRow); g.fillRect(0,0,getWidth(),getHeight());
                }
            };
            p.setOpaque(true);

            Color couleur = switch (v) {
                case "VALIDE"     -> UIHelper.VERT_BADGE;
                case "REJETE"     -> UIHelper.ROUGE_BADGE;
                case "EN_ATTENTE" -> UIHelper.ORANGE_BADGE;
                default           -> UIHelper.TEXTE_GRIS;
            };
            String txt = switch (v) {
                case "VALIDE"     -> "✓  Validé";
                case "REJETE"     -> "✗  Rejeté";
                case "EN_ATTENTE" -> "⏳  En attente";
                default           -> v;
            };

            JLabel badge = new JLabel(txt) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(couleur.getRed(),couleur.getGreen(),couleur.getBlue(),28));
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                    g2.setColor(new Color(couleur.getRed(),couleur.getGreen(),couleur.getBlue(),160));
                    g2.setStroke(new BasicStroke(1));
                    g2.draw(new RoundRectangle2D.Float(0,0,getWidth()-1,getHeight()-1,20,20));
                    g2.dispose(); super.paintComponent(g);
                }
            };
            badge.setForeground(couleur);
            badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(3,10,3,10));
            badge.setOpaque(false);
            p.add(badge);
            return p;
        }
    }

    /** Barre de progression colorée pour l'avancement. */
    public static class ProgressCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean foc, int row, int col) {

            String s = String.valueOf(val);
            int pct = 0;
            try { pct = Integer.parseInt(s.replace("%","").trim()); } catch (Exception ignored) {}

            Color bgRow = sel ? new Color(0,100,200,80)
                    : (row % 2 == 0 ? UIHelper.FOND_CARD : new Color(20,36,72));
            final int finalPct = pct;

            JPanel p = new JPanel(new GridBagLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    g.setColor(bgRow); g.fillRect(0,0,getWidth(),getHeight());
                }
            };
            p.setOpaque(true);
            p.setBorder(new EmptyBorder(6,12,6,12));

            Color barColor = pct >= 80 ? UIHelper.VERT_BADGE
                    : pct >= 40 ? UIHelper.ORANGE_BADGE : UIHelper.BLEU_ACCENT;

            JPanel barContainer = new JPanel(new BorderLayout(8,0));
            barContainer.setOpaque(false);

            JPanel bar = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(40,55,90));
                    g2.fill(new RoundRectangle2D.Float(0,3,getWidth(),8,8,8));
                    int w = (int)(getWidth() * finalPct / 100.0);
                    if (w > 0) {
                        GradientPaint gp = new GradientPaint(0,0, barColor.brighter(), w,0, barColor);
                        g2.setPaint(gp);
                        g2.fill(new RoundRectangle2D.Float(0,3,w,8,8,8));
                    }
                    g2.dispose();
                }
            };
            bar.setOpaque(false); bar.setPreferredSize(new Dimension(90,14));

            JLabel lbl = new JLabel(s);
            lbl.setFont(new Font("Segoe UI",Font.BOLD,11));
            lbl.setForeground(barColor);
            lbl.setPreferredSize(new Dimension(40,14));

            barContainer.add(bar, BorderLayout.CENTER);
            barContainer.add(lbl, BorderLayout.EAST);
            p.add(barContainer);
            return p;
        }
    }
}
