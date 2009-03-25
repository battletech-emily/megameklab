/*
 * MegaMekLab - Copyright (C) 2008
 *
 * Original author - jtighe (torren@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */

package megameklab.com.ui.Mek.Printing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import megamek.common.AmmoType;
import megamek.common.CriticalSlot;
import megamek.common.Engine;
import megamek.common.EquipmentType;
import megamek.common.Mech;
import megamek.common.MiscType;
import megamek.common.Mounted;
import megamek.common.TechConstants;
import megamek.common.WeaponType;
import megameklab.com.util.ImageHelper;
import megameklab.com.util.UnitUtil;

public class PrintMech implements Printable {

    protected Image awtImage = null;
    protected Image awtHud = null;
    private Mech mech = null;
    private ArrayList<Mech> mechList;

    private Mounted startingMount = null;
    private int startMountx = 0;
    private int startMounty = 0;
    private int endMountx = 0;
    private int endMounty = 0;

    public PrintMech(ArrayList<Mech> list) {
        awtImage = ImageHelper.getRecordSheet(list.get(0), false);
        mechList = list;

        /*
         * if (awtImage != null) { System.out.println("Width: " +
         * awtImage.getWidth(null)); System.out.println("Height: " +
         * awtImage.getHeight(null)); }
         */
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex >= 1) {
            return Printable.NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) graphics;
        // f.setPaper(this.paper);
        printImage(g2d, awtImage, awtHud, pageFormat);
        return Printable.PAGE_EXISTS;
    }

    public void printImage(Graphics2D g2d, Image image, Image hud, PageFormat pageFormat) {
        // System.out.println("printImage(Graphics2D g2d, Image image)");
        if (g2d == null) {
            return;
        }

        System.gc();
        // g2d.drawImage(image, 2, 0, (int)pageFormat.getImageableWidth(),
        // (int)pageFormat.getImageableHeight(), null);
        g2d.drawImage(image, 18, 18, 558, 738, Color.BLACK, null);
        printMekImage(g2d, hud);

        printMechData(g2d);
        printArmor(g2d);
        printWeaponsNEquipment(g2d);
        printLACrits(g2d);
        printRACrits(g2d);
        printCTCrits(g2d);
        printLTCrits(g2d);
        printRTCrits(g2d);
        printHeadCrits(g2d);
        printLLCrits(g2d);
        printRLCrits(g2d);

        // If its clan print case for the crits.
        if (mech.isClan()) {
            printLACase(g2d);
            printLLCase(g2d);
            printLTCase(g2d);
            printHeadCase(g2d);
            printCTCase(g2d);
            printRACase(g2d);
            printRTCase(g2d);
            printRLCase(g2d);
        }

        // Armor Pips
        printLAArmor(g2d);
        printRAArmor(g2d);
        printLTArmor(g2d);
        printRTArmor(g2d);
        printCTArmor(g2d);
        printLLArmor(g2d);
        printRLArmor(g2d);
        printLTRArmor(g2d);
        printRTRArmor(g2d);
        printCTRArmor(g2d);
        printHeadArmor(g2d);

        // Internal Pips
        printLAStruct(g2d);
        printRAStruct(g2d);
        printLTStruct(g2d);
        printRTStruct(g2d);
        printCTStruct(g2d);
        printHeadStruct(g2d);
        printLLStruct(g2d);
        printRLStruct(g2d);
        printHeatSinks(g2d);

        // g2d.translate(pageFormat.getImageableX(),
        // pageFormat.getImageableY());
        g2d.scale(pageFormat.getImageableWidth(), pageFormat.getImageableHeight());

    }

    private void printMechData(Graphics2D g2d) {
        String mekName = mech.getChassis().toUpperCase() + " " + mech.getModel().toUpperCase();

        g2d.setFont(UnitUtil.getNewFont(g2d, mekName, true, 180, 10.0f));
        g2d.drawString(mekName, 49, 121);

        Font font = UnitUtil.deriveFont(8.0f);
        g2d.setFont(font);

        if (mech.hasTSM()) {
            int walkTSM = mech.getWalkMP() + 1;
            int runTSM = (int) Math.ceil(walkTSM * 1.5) - (mech.getArmorType() == EquipmentType.T_ARMOR_HARDENED ? 1 : 0);
            g2d.drawString(Integer.toString(mech.getWalkMP()) + " [" + walkTSM + "]", 79, 144);
            g2d.drawString(Integer.toString(mech.getRunMP()) + " [" + runTSM + "]", 79, 155);
        } else if ((mech.getMASC() != null) && (mech.getSuperCharger() != null)) {
            int mascMP = (int) Math.ceil((mech.getWalkMP() * 2.5)) - (mech.getArmorType() == EquipmentType.T_ARMOR_HARDENED ? 1 : 0);
            g2d.drawString(Integer.toString(mech.getWalkMP()), 79, 144);
            g2d.drawString(Integer.toString(mech.getRunMPwithoutMASC()) + " [" + mascMP + "]", 79, 155);
        } else if ((mech.getMASC() != null) || (mech.getSuperCharger() != null)) {
            int mascMP = (mech.getWalkMP() * 2) - (mech.getArmorType() == EquipmentType.T_ARMOR_HARDENED ? 1 : 0);
            g2d.drawString(Integer.toString(mech.getWalkMP()), 79, 144);
            g2d.drawString(Integer.toString(mech.getRunMPwithoutMASC()) + " [" + mascMP + "]", 79, 155);
        } else {
            g2d.drawString(Integer.toString(mech.getWalkMP()), 79, 144);
            g2d.drawString(Integer.toString(mech.getRunMP()), 79, 155);
        }

        if (mech.hasUMU()) {
            font = UnitUtil.deriveFont(true, 8.0f);
            g2d.setFont(font);
            String movment = "Underwater: ";
            g2d.drawString(movment, 34, 166);

            font = UnitUtil.deriveFont(8.0f);
            g2d.setFont(font);
            g2d.drawString(Integer.toString(mech.getAllUMUCount()), 79, 166);
        } else if (mech.getJumpMP() > 0) {
            font = UnitUtil.deriveFont(true, 8.0f);
            g2d.setFont(font);
            String movment = "Jumping: ";
            g2d.drawString(movment, 34, 166);

            font = UnitUtil.deriveFont(8.0f);
            g2d.setFont(font);
            g2d.drawString(Integer.toString(mech.getJumpMP()), 79, 166);
        }

        // g2d.drawString(Integer.toString(mech.getJumpMP()), 79, 166);

        int tonnage = (int) Math.ceil(mech.getWeight());

        if (tonnage % 5 != 0) {
            tonnage += 5 - (tonnage % 5);
        }

        g2d.drawString(Integer.toString(tonnage), 177, 134);

        String techBase = "Inner Sphere";

        if (mech.isMixedTech()) {
            if (mech.isClan()) {
                techBase = "Mixed Tech (Clan)";
            } else {
                techBase = "Mixed Tech (I.S.)";
            }
        } else if (mech.isClan()) {
            techBase = "Clan";
        }

        int nextDataLine = 153;
        int startLine = 188;
        int lineFeed = 8;

        if (mech.isIndustrial()) {
            if (mech.isPrimitive()) {
                ImageHelper.printCenterString(g2d, "(Primitive Industrial)", font, startLine, nextDataLine);
            } else {
                ImageHelper.printCenterString(g2d, "(Industrial)", font, startLine, nextDataLine);
            }

            nextDataLine += lineFeed;
        } else if (mech.isPrimitive()) {
            ImageHelper.printCenterString(g2d, "(Primitive)", font, startLine, nextDataLine);
            nextDataLine += lineFeed;
        } else {

        switch (mech.getTechLevel()) {

            case TechConstants.T_INTRO_BOXSET:
                ImageHelper.printCenterString(g2d, "(Intro)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
            case TechConstants.T_IS_TW_NON_BOX:
            case TechConstants.T_IS_TW_ALL:
            case TechConstants.T_CLAN_TW:
                break;
            case TechConstants.T_IS_ADVANCED:
            case TechConstants.T_CLAN_ADVANCED:
                ImageHelper.printCenterString(g2d, "(Advanced)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
            case TechConstants.T_IS_EXPERIMENTAL:
            case TechConstants.T_CLAN_EXPERIMENTAL:
                ImageHelper.printCenterString(g2d, "(Experimental)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
            case TechConstants.T_IS_UNOFFICIAL:
            case TechConstants.T_CLAN_UNOFFICIAL:
                ImageHelper.printCenterString(g2d, "(Unofficial)", font, startLine, nextDataLine);
                nextDataLine += lineFeed;
                break;
            }
        }

        // Cost/BV
        DecimalFormat myFormatter = new DecimalFormat("#,###");
        g2d.drawString(myFormatter.format(mech.calculateBattleValue(true, true)), 150, 350);

        myFormatter = new DecimalFormat("#,###.##");
        g2d.drawString(myFormatter.format(mech.getCost()) + " C-bills", 52, 350);

        String isName = "";

        if (mech.hasCompositeStructure()) {
            isName = EquipmentType.getStructureTypeName(EquipmentType.T_STRUCTURE_COMPOSITE);
        } else if (mech.hasReinforcedStructure()) {
            isName = EquipmentType.getStructureTypeName(EquipmentType.T_STRUCTURE_REINFORCED);
        }

        if (isName.trim().length() > 0) {
            g2d.setFont(UnitUtil.getNewFont(g2d, isName, true, 44, 10.0f));
            g2d.drawString(isName, 442, 553);
        }

        String armorName = "";

        if (mech.getArmorType() == EquipmentType.T_ARMOR_HARDENED) {
            armorName = EquipmentType.getArmorTypeName(EquipmentType.T_ARMOR_HARDENED);
        }

        if (armorName.trim().length() > 0) {
            g2d.setFont(UnitUtil.getNewFont(g2d, armorName, true, 38, 10.0f));
            g2d.drawString(armorName, 461, 249);
        }

        String yearFluff = mech.getYear() + " " + mech.getSource();
        g2d.setFont(UnitUtil.getNewFont(g2d, yearFluff.trim(), false, 80, 8.0f));
        ImageHelper.printCenterString(g2d, yearFluff.trim(), font, startLine, nextDataLine);

        g2d.setFont(UnitUtil.getNewFont(g2d, techBase, false, 51, 10.0f));
        g2d.drawString(techBase, 177, 145);

        font = new Font("Arial", Font.BOLD, 7);
        g2d.setFont(font);
        g2d.drawString("2009", 106.5f, 744.5f);

        if (mech.getGyroType() == Mech.GYRO_HEAVY_DUTY) {
            g2d.drawImage(ImageHelper.getGyroPipImage(), 235, 588, 9, 8, null);
        }

    }

    private void printHeatSinks(Graphics2D g2d) {
        Font font = UnitUtil.deriveFont(true, 8.0f);
        g2d.setFont(font);

        // Heat Sinks
        if (mech.hasDoubleHeatSinks()) {
            g2d.drawString(Integer.toString(mech.heatSinks()) + " (" + Integer.toString(mech.heatSinks() * 2) + ")", 502, 595);
            g2d.drawString("Double", 502, 603);
        } else {
            g2d.drawString(Integer.toString(mech.heatSinks()) + " (" + Integer.toString(mech.heatSinks()) + ")", 502, 595);
            g2d.drawString("Single", 502, 603);
        }

        Dimension column = new Dimension(504, 612);
        Dimension pipShift = new Dimension(9, 9);

        for (int pos = 1; pos <= mech.heatSinks(); pos++) {
            ImageHelper.drawHeatSinkPip(g2d, column.width, column.height);
            column.height += pipShift.height;

            if (pos % 10 == 0) {
                column.height -= pipShift.height * 10;
                column.width += pipShift.width;
            }

        }

    }

    private void printArmor(Graphics2D g2d) {
        // Armor
        Font font = UnitUtil.deriveFont(7.0f);
        g2d.setFont(font);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_HEAD)) + ")", 485, 46);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_LT)) + ")", 434, 60);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_RT)) + ")", 506, 60);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_CT)) + ")", 472, 222);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_LARM)) + ")", 398, 215);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_RARM)) + ")", 546, 215);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_LLEG)) + ")", 389, 273);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_RLEG)) + ")", 554, 273);
        // Rear
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_LT, true)) + ")", 403, 362);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_CT, true)) + ")", 481, 278);
        g2d.drawString("(" + Integer.toString(mech.getArmor(Mech.LOC_RT, true)) + ")", 546, 362);
        // Internal
        g2d.drawString("(" + Integer.toString(mech.getInternal(Mech.LOC_LT)) + ")", 432, 403);
        g2d.drawString("(" + Integer.toString(mech.getInternal(Mech.LOC_RT)) + ")", 525, 403);
        g2d.drawString("(" + Integer.toString(mech.getInternal(Mech.LOC_LARM)) + ")", 390, 480);
        g2d.drawString("(" + Integer.toString(mech.getInternal(Mech.LOC_RARM)) + ")", 530, 480);
        g2d.drawString("(" + Integer.toString(mech.getInternal(Mech.LOC_CT)) + ")", 459, 509);
        g2d.drawString("(" + Integer.toString(mech.getInternal(Mech.LOC_LLEG)) + ")", 403, 538);
        g2d.drawString("(" + Integer.toString(mech.getInternal(Mech.LOC_RLEG)) + ")", 518, 539);
    }

    private void printLACrits(Graphics2D g2d) {

        int lineStart = 56;
        int linePoint = 408;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_LARM, lineStart, linePoint, lineFeed);
    }

    private void printRACrits(Graphics2D g2d) {

        int lineStart = 292;
        int linePoint = 408;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_RARM, lineStart, linePoint, lineFeed);
    }

    private void printCTCrits(Graphics2D g2d) {

        int lineStart = 174;
        int linePoint = 469;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_CT, lineStart, linePoint, lineFeed);
    }

    private void printLTCrits(Graphics2D g2d) {

        int lineStart = 56;
        int linePoint = 545;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_LT, lineStart, linePoint, lineFeed);
    }

    private void printRTCrits(Graphics2D g2d) {

        int lineStart = 292;
        int linePoint = 545;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_RT, lineStart, linePoint, lineFeed);
    }

    private void printHeadCrits(Graphics2D g2d) {

        int lineStart = 174;
        int linePoint = 401;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_HEAD, lineStart, linePoint, lineFeed);
    }

    private void printLLCrits(Graphics2D g2d) {

        int lineStart = 56;
        int linePoint = 682;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_LLEG, lineStart, linePoint, lineFeed);
    }

    private void printRLCrits(Graphics2D g2d) {

        int lineStart = 292;
        int linePoint = 682;
        int lineFeed = 8;

        printLocationCriticals(g2d, Mech.LOC_RLEG, lineStart, linePoint, lineFeed);
    }

    private void printWeaponsNEquipment(Graphics2D g2d) {

        ImageHelper.printMechWeaponsNEquipment(mech, g2d);
    }

    public void print() {

        try {
            PrinterJob pj = PrinterJob.getPrinterJob();

            if (pj.printDialog()) {
                // Paper paper = new Paper();
                PageFormat pageFormat = new PageFormat();
                pageFormat = pj.getPageFormat(null);

                Paper p = pageFormat.getPaper();
                p.setImageableArea(0, 0, p.getWidth(), p.getHeight());
                pageFormat.setPaper(p);

                pj.setPrintable(this, pageFormat);

                for (Mech currentMech : mechList) {

                    mech = currentMech;
                    awtHud = ImageHelper.getFluffImage(currentMech, "mech");
                    pj.setJobName(mech.getChassis() + " " + mech.getModel());

                    try {
                        pj.print();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    System.gc();
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void printRLArmor(Graphics2D g2d) {
        float[] topColumn = { 499, 181 };
        float[] middleColumn = { 509, 253 };
        float[] bottomColumn = { 531, 270 };
        float[] footColumn = { 519, 296 };
        float[] pipShift = { 8, -2 };

        int totalArmor = mech.getArmor(Mech.LOC_RLEG);

        if (totalArmor < 1) {
            return;
        }

        Vector<float[]> pipPlotter = new Vector<float[]>(20);

        for (int pos = 1; pos <= 20; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            // ImageHelper.drawArmorPip(g2d, topColumn[0], topColumn[1]);
            topColumn[0] += pipShift[0];
            topColumn[1] += pipShift[1];
            if (pos % 2 == 0) {
                pipShift[0] *= -1;
                topColumn[0] += pipShift[0] + 1.8f;
                pipShift[1] *= -1;
                topColumn[1] += pipShift[1] + 7;
            }
        }

        if (totalArmor < 1) {
            return;
        }

        for (int pos = 1; pos <= 12; pos++) {
            pipPlotter.add(new float[] { middleColumn[0], middleColumn[1] });
            middleColumn[0] += pipShift[0];
            middleColumn[1] += pipShift[1];
            if (pos % 4 == 0) {
                pipShift[0] *= -1;
                middleColumn[0] += pipShift[0] + 1.8f;
                pipShift[1] *= -1;
                middleColumn[1] += pipShift[1] + 7;
            }

        }

        if (totalArmor < 1) {
            return;
        }

        for (int pos = 1; pos <= 6; pos++) {
            pipPlotter.add(new float[] { bottomColumn[0], bottomColumn[1] });
            bottomColumn[0] += pipShift[0];
            bottomColumn[1] += pipShift[1];
            if (pos % 2 == 0) {
                pipShift[0] *= -1;
                bottomColumn[0] += pipShift[0] + 1.8f;
                pipShift[1] *= -1;
                bottomColumn[1] += pipShift[1] + 7;
            }
        }

        for (int pos = 1; pos <= 4; pos++) {
            pipPlotter.add(new float[] { footColumn[0], footColumn[1] });
            footColumn[0] += pipShift[0];
        }
        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printLLArmor(Graphics2D g2d) {
        float[] topColumn = { 443, 179 };
        float[] middleColumn = { 417, 247 };
        float[] bottomColumn = { 427.6f, 272 };
        float[] footColumn = { 406, 296 };
        float[] pipShift = { 8, 2 };

        int totalArmor = mech.getArmor(Mech.LOC_LLEG);

        if (totalArmor < 1) {
            return;
        }

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 20; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];
            topColumn[1] += pipShift[1];
            if (pos % 2 == 0) {
                pipShift[0] *= -1;
                topColumn[0] += pipShift[0] - 1.8f;
                pipShift[1] *= -1;
                topColumn[1] += pipShift[1] + 7;
            }

        }

        for (int pos = 1; pos <= 12; pos++) {
            pipPlotter.add(new float[] { middleColumn[0], middleColumn[1] });
            middleColumn[0] += pipShift[0];
            middleColumn[1] += pipShift[1];
            if (pos % 4 == 0) {
                pipShift[0] *= -1;
                middleColumn[0] += pipShift[0] - 1.8f;
                pipShift[1] *= -1;
                middleColumn[1] += pipShift[1] + 7;
            }
        }

        for (int pos = 1; pos <= 6; pos++) {
            pipPlotter.add(new float[] { bottomColumn[0], bottomColumn[1] });
            bottomColumn[0] += pipShift[0];
            bottomColumn[1] += pipShift[1];
            if (pos % 2 == 0) {
                pipShift[0] *= -1;
                bottomColumn[0] += pipShift[0] - 1.9f;
                pipShift[1] *= -1;
                bottomColumn[1] += pipShift[1] + 7;
            }
        }

        for (int pos = 1; pos <= 4; pos++) {
            pipPlotter.add(new float[] { footColumn[0], footColumn[1] });
            footColumn[0] += pipShift[0];
        }

        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printLAArmor(Graphics2D g2d) {
        float[] rightColumn = { 417, 77 };
        float[] centerColumn = { 409, 84 };
        float[] leftColumn = { 402, 90 };
        float[] pipShift = { -1, 7 };

        int totalArmor = mech.getArmor(Mech.LOC_LARM);

        if (totalArmor < 1) {
            return;
        }

        int pips = 12;

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { centerColumn[0], centerColumn[1] });
            centerColumn[1] += pipShift[1];
            if (pos % 3 != 0) {
                centerColumn[0] += pipShift[0];
            }

            if ((pos == 7) || (pos == 8)) {
                centerColumn[1]++;
            }
        }

        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { rightColumn[0], rightColumn[1] });
            rightColumn[1] += pipShift[1];
            if (pos % 3 != 0) {
                rightColumn[0] += pipShift[0];
            }

            if (pos == 8) {
                rightColumn[1] += pipShift[1] + 3;
                rightColumn[0] += pipShift[0];
            }
        }

        pips = 10;

        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { leftColumn[0], leftColumn[1] });
            leftColumn[1] += pipShift[1];
            if (pos % 3 != 0) {
                leftColumn[0] += pipShift[0];
            }
            if (pos == 6) {
                leftColumn[1] += pipShift[1] + 3;
                leftColumn[0] += pipShift[0];
            }
        }

        printArmorPoints(g2d, pipPlotter, totalArmor);

    }

    private void printRAArmor(Graphics2D g2d) {
        float[] rightColumn = { 548, 90 };
        float[] centerColumn = { 540, 84 };
        float[] leftColumn = { 533, 77 };
        float[] pipShift = { 1, 7 };

        int totalArmor = mech.getArmor(Mech.LOC_RARM);

        if (totalArmor < 1) {
            return;
        }
        Vector<float[]> pipPlotter = new Vector<float[]>(20);

        for (int pos = 1; pos <= 12; pos++) {
            pipPlotter.add(new float[] { centerColumn[0], centerColumn[1] });
            centerColumn[1] += pipShift[1];
            if (pos % 3 != 0) {
                centerColumn[0] += pipShift[0];
            }

            if ((pos == 7) || (pos == 8)) {
                centerColumn[1]++;
            }
        }

        for (int pos = 1; pos <= 12; pos++) {
            pipPlotter.add(new float[] { leftColumn[0], leftColumn[1] });
            leftColumn[1] += pipShift[1];
            if (pos % 3 != 0) {
                leftColumn[0] += pipShift[0];
            }

            if (pos == 8) {
                leftColumn[1] += pipShift[1] + 3;
                leftColumn[0] += pipShift[0];
            }
        }

        for (int pos = 1; pos <= 10; pos++) {
            pipPlotter.add(new float[] { rightColumn[0], rightColumn[1] });
            rightColumn[1] += pipShift[1];
            if (pos % 3 != 0) {
                rightColumn[0] += pipShift[0];
            }
            if (pos == 6) {
                rightColumn[1] += pipShift[1] + 3;
                rightColumn[0] += pipShift[0];
            }
        }
        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printLTArmor(Graphics2D g2d) {
        float[] topColumn = { 430, 88 };
        float[] middleColumn = { 445, 126 };
        float[] bottomColumn = { 437, 161 };
        float[] topPipShift = { 6, 7 };
        float[] middlePipShift = { 6, 7 };
        float[] bottomPipShift = { 6, 7 };

        int totalArmor = mech.getOArmor(Mech.LOC_LT);
        if (totalArmor < 1) {
            return;
        }

        int maxTopPips = 25;
        int maxMiddlePips = 10;
        int maxBottemPips = 7;

        int topPipsPerLine = 5;
        int middlePipsPerLine = 2;

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= maxTopPips; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            topColumn[0] += topPipShift[0];
            if (pos % topPipsPerLine == 0) {
                topColumn[1] += topPipShift[1];
                topPipShift[0] *= -1;
                topColumn[0] += topPipShift[0];
            }
        }

        for (int pos = 1; pos <= maxMiddlePips; pos++) {
            pipPlotter.add(new float[] { middleColumn[0], middleColumn[1] });
            middleColumn[0] += middlePipShift[0];
            if (pos % middlePipsPerLine == 0) {
                middleColumn[1] += middlePipShift[1];
                middleColumn[0] += 1;
                middlePipShift[0] *= -1;
                if (middlePipsPerLine > 1) {
                    middleColumn[0] += middlePipShift[0];
                }
            }
        }

        for (int pos = 1; pos <= maxBottemPips; pos++) {
            pipPlotter.add(new float[] { bottomColumn[0], bottomColumn[1] });
            bottomColumn[0] += bottomPipShift[0];
            if (pos % 4 == 0) {
                bottomColumn[1] += bottomPipShift[1];
                bottomColumn[0] += 1;
                bottomPipShift[0] *= -1;
                bottomColumn[0] += bottomPipShift[0];
            }
        }
        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printLTRArmor(Graphics2D g2d) {
        float[] topColumn = { 437, 312 };
        float[] pipShift = { 5, 5 };
        int pipsPerLine = 5;

        int totalArmor = mech.getOArmor(Mech.LOC_LT, true);

        if (totalArmor < 1) {
            return;
        }

        Vector<float[]> pipPlotter = new Vector<float[]>(20);

        for (int pos = 1; pos <= 35; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];
            if (pos % pipsPerLine == 0) {
                topColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                if (pos >= 30) {
                    topColumn[0] += pipShift[0];
                } else {
                    topColumn[0] += pipShift[0] * 2;
                }
            }
        }

        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printRTArmor(Graphics2D g2d) {
        float[] topColumn = { 520, 88 };
        float[] middleColumn = { 504, 126 };
        float[] bottomColumn = { 512, 161 };
        float[] topPipShift = { 6, 7 };
        float[] middlePipShift = { 6, 7 };
        float[] bottomPipShift = { 6, 7 };

        int totalArmor = mech.getOArmor(Mech.LOC_RT);

        if (totalArmor < 1) {
            return;
        }

        Vector<float[]> pipPlotter = new Vector<float[]>(20);

        int maxTopPips = 25;
        int maxMiddlePips = 10;
        int maxBottemPips = 7;

        int topPipsPerLine = 5;
        int middlePipsPerLine = 2;

        for (int pos = 1; pos <= maxTopPips; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            topColumn[0] -= topPipShift[0];
            if (pos % topPipsPerLine == 0) {
                topColumn[1] += topPipShift[1];
                topPipShift[0] *= -1;
                topColumn[0] -= topPipShift[0];
            }
        }

        for (int pos = 1; pos <= maxMiddlePips; pos++) {
            pipPlotter.add(new float[] { middleColumn[0], middleColumn[1] });
            middleColumn[0] -= middlePipShift[0];
            if (pos % middlePipsPerLine == 0) {
                middleColumn[1] += middlePipShift[1];
                middleColumn[0] -= 1;
                middlePipShift[0] *= -1;
                if (middlePipsPerLine > 1) {
                    middleColumn[0] -= middlePipShift[0];
                }
            }
        }

        for (int pos = 1; pos <= maxBottemPips; pos++) {
            pipPlotter.add(new float[] { bottomColumn[0], bottomColumn[1] });
            bottomColumn[0] -= bottomPipShift[0];
            if (pos % 4 == 0) {
                bottomColumn[1] += bottomPipShift[1];
                bottomColumn[0] -= 1;
                bottomPipShift[0] *= -1;
                bottomColumn[0] -= bottomPipShift[0];
            }
        }

        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printRTRArmor(Graphics2D g2d) {
        float[] topColumn = { 514, 312 };
        float[] pipShift = { 5, 5 };

        int totalArmor = mech.getOArmor(Mech.LOC_RT, true);

        if (totalArmor < 1) {
            return;
        }

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 35; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            topColumn[0] -= pipShift[0];
            if (pos % 5 == 0) {
                topColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                if (pos >= 30) {
                    topColumn[0] -= pipShift[0];
                } else {
                    topColumn[0] -= pipShift[0] * 2;
                }
            }
        }

        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printCTArmor(Graphics2D g2d) {
        float[] topColumn = { 464, 105 };
        float[] middleColumn = { 481, 172 };
        float[] bottomColumn = { 475, 185 };
        float[] pipShift = { 6, 6 };

        int totalArmor = mech.getOArmor(Mech.LOC_CT);

        if (totalArmor < 1) {
            return;
        }

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 55; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];
            if (pos % 5 == 0) {
                topColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                topColumn[0] += pipShift[0];
                if (pos >= 30) {
                    pipShift[0] -= .1;
                }
            }
        }

        for (int pos = 1; pos <= 6; pos++) {
            pipPlotter.add(new float[] { middleColumn[0], middleColumn[1] });
            middleColumn[0] += pipShift[0];
            if (pos % 3 == 0) {
                middleColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                middleColumn[0] += pipShift[0];
            }

        }

        pipPlotter.add(new float[] { bottomColumn[0], bottomColumn[1] });

        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printHeadArmor(Graphics2D g2d) {
        Font font = new Font("Arial", Font.PLAIN, 7);
        g2d.setFont(font);

        Dimension fillCircle = new Dimension(4, 4);

        if (mech.getArmor(Mech.LOC_HEAD) >= 1) {
            g2d.setColor(Color.white);
            g2d.fillOval(476, 68, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 475, 73);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 2) {
            g2d.setColor(Color.white);
            g2d.fillOval(473, 74, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 472, 79);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 3) {
            g2d.setColor(Color.white);
            g2d.fillOval(479, 74, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 478, 79);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 4) {
            g2d.setColor(Color.white);
            g2d.fillOval(470, 79, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 469, 84);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 5) {
            g2d.setColor(Color.white);
            g2d.fillOval(476, 79, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 475, 84);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 6) {
            g2d.setColor(Color.white);
            g2d.fillOval(482, 79, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 481, 84);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 7) {
            g2d.setColor(Color.white);
            g2d.fillOval(470, 85, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 469, 90);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 8) {
            g2d.setColor(Color.white);
            g2d.fillOval(476, 85, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 475, 90);
        }

        if (mech.getArmor(Mech.LOC_HEAD) >= 9) {
            g2d.setColor(Color.white);
            g2d.fillOval(482, 85, fillCircle.width, fillCircle.height);
            g2d.setColor(Color.black);
            ImageHelper.drawArmorPip(g2d, 481, 90);
        }
        g2d.setColor(Color.black);
    }

    private void printCTRArmor(Graphics2D g2d) {
        float[] topColumn = new float[] { 470, 301 };
        float[] pipShift = new float[] { 5f, 5f };

        int totalArmor = mech.getOArmor(Mech.LOC_CT, true);
        if (totalArmor < 1) {
            return;
        }

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 56; pos++) {
            pipPlotter.add(new float[] { topColumn[0], topColumn[1] });
            topColumn[0] += pipShift[0];
            if (pos % 4 == 0) {
                topColumn[1] += pipShift[1];
                pipShift[0] *= -1;
                topColumn[0] += pipShift[0] * 2;
            }
        }

        printArmorPoints(g2d, pipPlotter, totalArmor);
    }

    private void printLAStruct(Graphics2D g2d) {
        float[] column = { 419, 413 };
        float[] pipShift = { 4, 4 };

        int totalArmor = mech.getInternal(Mech.LOC_LARM);

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 16; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
            pipShift[0] *= -1;
            column[0] += pipShift[0];

            if (pos % 4 == 0) {
                column[0] -= 2;
            }

        }

        column[1] += pipShift[1];
        pipPlotter.add(new float[] { column[0], column[1] });

        printISPoints(g2d, pipPlotter, totalArmor);
    }

    private void printLLStruct(Graphics2D g2d) {
        float[] column = { 441, 475 };
        float[] pipShift = { 4, 4 };

        int totalArmor = mech.getInternal(Mech.LOC_LLEG);

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 18; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
            column[0] += pipShift[0];
            pipShift[0] *= -1;

            if (pos % 4 == 0) {
                column[0] -= 3;
            }

        }

        for (int pos = 1; pos <= 2; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1] + 2;
        }

        column[1] -= 3;
        column[0] -= pipShift[0] + 1;
        pipPlotter.add(new float[] { column[0], column[1] });

        printISPoints(g2d, pipPlotter, totalArmor);
    }

    private void printRLStruct(Graphics2D g2d) {
        float[] column = { 484, 475 };
        float[] pipShift = { 4, 4 };

        int totalArmor = mech.getInternal(Mech.LOC_RLEG);

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 18; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
            column[0] -= pipShift[0];
            pipShift[0] *= -1;

            if (pos % 4 == 0) {
                column[0] += 3;
            }

        }

        for (int pos = 1; pos <= 2; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1] + 2;
        }

        column[1] -= 3;
        column[0] += pipShift[0] + 1;
        pipPlotter.add(new float[] { column[0], column[1] });
        printISPoints(g2d, pipPlotter, totalArmor);
    }

    private void printRAStruct(Graphics2D g2d) {
        float[] column = { 506, 413 };
        float[] pipShift = { 4, 4 };

        int totalArmor = mech.getInternal(Mech.LOC_RARM);

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 16; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
            pipShift[0] *= -1;
            column[0] -= pipShift[0];

            if (pos % 4 == 0) {
                column[0] += 2;
            }

        }

        column[1] += pipShift[1];
        pipPlotter.add(new float[] { column[0], column[1] });

        printISPoints(g2d, pipPlotter, totalArmor);
    }

    private void printLTStruct(Graphics2D g2d) {
        float[] column = { 435, 415 };
        float[] pipShift = { 5, 5 };

        int totalArmor = mech.getInternal(Mech.LOC_LT);

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= 12; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[0] += pipShift[0];

            if (pos % 3 == 0) {
                column[1] += pipShift[1];
                pipShift[0] *= -1;
                column[0] += pipShift[0];
            }

        }

        column[0] += pipShift[0] * 2;
        for (int pos = 1; pos <= 2; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
        }

        column[0] += pipShift[0] / 2;
        for (int pos = 1; pos <= 2; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
        }

        column[0] += pipShift[0] / 2;
        pipPlotter.add(new float[] { column[0], column[1] });
        column[1] += pipShift[1];

        pipShift[0] *= -1;
        column[1] += pipShift[1] / 2;
        for (int pos = 1; pos <= 4; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[0] += pipShift[0];

            if (pos % 2 == 0) {
                pipShift[0] *= -1;
                column[1] += pipShift[1];
            }
        }

        printISPoints(g2d, pipPlotter, totalArmor);

    }

    private void printRTStruct(Graphics2D g2d) {
        float[] column = { 480f, 415 };
        float[] pipShift = { 5, 5 };

        int totalArmor = mech.getInternal(Mech.LOC_LT);

        int pips = 12;
        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[0] += pipShift[0];

            if (pos % 3 == 0) {
                column[1] += pipShift[1];
                pipShift[0] *= -1;
                column[0] += pipShift[0];
            }

        }

        pips = 2;

        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
        }

        pips = 2;

        column[0] -= pipShift[0] / 2;
        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
        }

        pips = 1;

        column[0] -= pipShift[0] / 2;
        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[1] += pipShift[1];
        }

        pips = 4;

        column[1] += pipShift[1] / 2;
        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[0] += pipShift[0];

            if (pos % 2 == 0) {
                pipShift[0] *= -1;
                column[1] += pipShift[1];
            }
        }

        printISPoints(g2d, pipPlotter, totalArmor);


    }

    private void printCTStruct(Graphics2D g2d) {
        float[] column = { 457, 423 };
        float[] pipShift = { 5, 5 };

        int totalArmor = mech.getInternal(Mech.LOC_CT);

        int pips = 27;

        Vector<float[]> pipPlotter = new Vector<float[]>(20);
        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[0] += pipShift[0];

            if (pos % 3 == 0) {
                column[1] += pipShift[1];
                pipShift[0] *= -1;
                column[0] += pipShift[0];
            }

        }

        pips = 4;

        column[1] += pipShift[1];
        column[0] += pipShift[0] / 2;

        for (int pos = 1; pos <= pips; pos++) {
            pipPlotter.add(new float[] { column[0], column[1] });
            column[0] += pipShift[0];
            if (pos % 2 == 0) {
                column[1] += pipShift[1];
                pipShift[0] *= -1;
                column[0] += pipShift[0];
            }
        }

        printISPoints(g2d, pipPlotter, totalArmor);

    }

    private void printHeadStruct(Graphics2D g2d) {
        ImageHelper.drawISPip(g2d, 463, 403);
        ImageHelper.drawISPip(g2d, 460, 410);
        ImageHelper.drawISPip(g2d, 466, 410);
    }

    private void setCritConnection(Mounted m, int startx, int starty, int endx, int endy, Graphics2D g2d) {
        if (m == null) {
            printCritConnection(g2d, startMountx, startMounty, endMountx, endMounty);
            startingMount = null;
            startMountx = startx;
            startMounty = starty;
            endMountx = endx;
            endMounty = endy;
        } else if ((startingMount == null) && (UnitUtil.getCritsUsed(mech, m.getType()) > 1)) {
            startingMount = m;
            startMountx = startx;
            startMounty = starty;
            endMountx = endx;
            endMounty = endy;
        } else if (!m.equals(startingMount)) {
            printCritConnection(g2d, startMountx, startMounty, endMountx, endMounty);
            if (UnitUtil.getCritsUsed(mech, m.getType()) > 1) {
                startingMount = m;
            } else {
                startingMount = null;
            }
            startMountx = startx;
            startMounty = starty;
            endMountx = endx;
            endMounty = endy;
        } else if (m.equals(startingMount)) {
            endMounty = endy;
        }

    }

    private void printCritConnection(Graphics2D g2d, int startx, int starty, int endx, int endy) {
        if (starty == endy) {
            return;
        }

        g2d.drawLine(startx - 1, starty - 6, startx - 4, starty - 6);
        g2d.drawLine(startx - 4, starty - 6, endx - 4, endy);
        g2d.drawLine(endx - 1, endy, endx - 4, endy);
    }

    /**
     * Print the critcals for a Mek in the specific location
     *
     * @param g2d
     *            The 2d Graphics object use to print
     * @param location
     *            Current location of the Mek
     * @param lineStart
     *            Where to start printing x
     * @param linePoint
     *            Where to Start printing y
     * @param lineFeed
     *            How much to move down to the next line.
     */
    private void printLocationCriticals(Graphics2D g2d, int location, int lineStart, int linePoint, int lineFeed) {
        Font font;
        for (int slot = 0; slot < mech.getNumberOfCriticals(location); slot++) {
            font = UnitUtil.deriveFont(true, 7.0f);
            g2d.setFont(font);
            CriticalSlot cs = mech.getCritical(location, slot);

            if (cs == null) {
                font = UnitUtil.deriveFont(7.0f);
                g2d.setFont(font);
                g2d.drawString("Roll Again", lineStart, linePoint);
                setCritConnection(null, lineStart, linePoint, lineStart, linePoint, g2d);
            } else if (cs.getType() == CriticalSlot.TYPE_SYSTEM) {
                if (cs.getIndex() == Mech.SYSTEM_ENGINE) {
                    String engineName = "Fusion Engine";
                    if (mech.isPrimitive()) {
                        engineName = "Primitive Fusion Engine";
                    }
                    switch (mech.getEngine().getEngineType()) {
                    case Engine.COMBUSTION_ENGINE:
                        engineName = "I.C.E.";
                        if (mech.isPrimitive()) {
                            engineName = "Primitive I.C.E";
                        }
                        break;
                    case Engine.LIGHT_ENGINE:
                        engineName = "Light Fusion Engine";
                        break;
                    case Engine.XL_ENGINE:
                        engineName = "XL Fusion Engine";
                        break;
                    case Engine.XXL_ENGINE:
                        engineName = "XXL Fusion Engine";
                        break;
                    case Engine.COMPACT_ENGINE:
                        engineName = "Compact Fusion Engine";
                        break;
                    default:
                        break;
                    }

                    if (cs.isArmored()) {
                        engineName = "O " + engineName;
                    }

                    g2d.drawString(engineName, lineStart, linePoint);
                } else {
                    String critName = mech.getSystemName(cs.getIndex());

                    if (critName.indexOf("Standard") > -1) {
                        critName = critName.replace("Standard ", "");
                    }

                    if (cs.isArmored()) {
                        critName = "O " + critName;
                    }


                    if (((cs.getIndex() >= Mech.ACTUATOR_UPPER_ARM) && (cs.getIndex() <= Mech.ACTUATOR_HAND)) || ((cs.getIndex() >= Mech.ACTUATOR_UPPER_LEG) && (cs.getIndex() <= Mech.ACTUATOR_FOOT))) {
                        critName += " Actuator";
                    }
                    g2d.drawString(critName, lineStart, linePoint);
                }
                setCritConnection(null, lineStart, linePoint, lineStart, linePoint, g2d);
            } else if (cs.getType() == CriticalSlot.TYPE_EQUIPMENT) {
                Mounted m = cs.getMount();

                setCritConnection(m, lineStart, linePoint, lineStart, linePoint, g2d);

                StringBuffer critName = new StringBuffer(UnitUtil.getCritName(mech, m.getType()));

                if (m.isArmored()) {
                    critName.insert(0, "O ");
                }

                if (UnitUtil.isTSM(m.getType())) {
                    critName.setLength(0);
                    critName.append("Triple-Strength Myomer");
                }

                if (m.isRearMounted()) {
                    critName.append("(R)");
                } else if (m.getType() instanceof MiscType && (m.getType().hasFlag(MiscType.F_MODULAR_ARMOR))) {
                    critName.append("[OOOOOOOOOO]");
                } else if ((m.getType() instanceof AmmoType) && (((AmmoType) m.getType()).getAmmoType() != AmmoType.T_COOLANT_POD)) {
                    AmmoType ammo = (AmmoType) m.getType();

                    critName = new StringBuffer("Ammo (");
                    // Remove Text (Clan) from the name
                    critName.append(ammo.getShortName().replace('(', '.').replace(')', '.').replaceAll(".Clan.", "").replaceAll("[0-9]", "").trim());
                    // Remove any additional Ammo text.
                    if (critName.toString().endsWith("Ammo")) {
                        critName.setLength(critName.length() - 5);
                        critName.trimToSize();
                    }

                    // Remove Capable with the name
                    if (critName.indexOf("-capable") > -1) {
                        int startPos = critName.indexOf("-capable");
                        critName.delete(startPos, startPos + "-capable".length());
                        critName.trimToSize();
                    }

                    // Trim trailing spaces.
                    while (critName.charAt(critName.length() - 1) == ' ') {
                        critName.setLength(critName.length() - 1);
                    }
                    critName.trimToSize();
                    critName.append(") ");
                    critName.append(ammo.getShots());

                }

                g2d.setFont(UnitUtil.getNewFont(g2d, critName.toString(), m.getType().isHittable(), 86, 7.0f));

                if ((m.getType() instanceof MiscType) && m.getType().hasFlag(MiscType.F_C3I)) {
                    ImageHelper.printC3iName(g2d, lineStart, linePoint, font, m.isArmored());
                } else if ((m.getType() instanceof MiscType) && m.getType().hasFlag(MiscType.F_C3S)) {
                    ImageHelper.printC3sName(g2d, lineStart, linePoint, font, m.isArmored());
                } else if ((m.getType() instanceof WeaponType) && m.getType().hasFlag(WeaponType.F_C3M)) {
                    ImageHelper.printC3mName(g2d, lineStart, linePoint, font, m.isArmored());
                } else {
                    g2d.drawString(critName.toString(), lineStart, linePoint);
                }
            }
            linePoint += lineFeed;

            if ((slot > 0) && (slot % 2 == 0)) {
                linePoint++;
            }

            if (slot == 5) {
                linePoint += lineFeed / 2;
            }

        }
        setCritConnection(null, lineStart, linePoint, lineStart, linePoint, g2d);

    }

    private void printMekImage(Graphics2D g2d, Image img) {

        int width = Math.min(148, img.getWidth(null));
        int height = Math.min(200, img.getHeight(null));
        int drawingX = 235 + ((148 - width) / 2);
        int drawingY = 172 + ((200 - height) / 2);
        g2d.drawImage(img, drawingX, drawingY, width, height, Color.BLACK, null);

    }

    private void printLACase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_LARM)) {
            return;
        }

        int lineStart = 98;
        int linePoint = 398;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printRACase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_RARM)) {
            return;
        }

        int lineStart = 342;
        int linePoint = 398;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printLLCase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_LLEG)) {
            return;
        }

        int lineStart = 93;
        int linePoint = 671;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printLTCase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_LT)) {
            return;
        }

        int lineStart = 104;
        int linePoint = 534;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printHeadCase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_HEAD)) {
            return;
        }

        int lineStart = 196;
        int linePoint = 388;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printRTCase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_RT)) {
            return;
        }

        int lineStart = 348;
        int linePoint = 534;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printRLCase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_RLEG)) {
            return;
        }

        int lineStart = 338;
        int linePoint = 671;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printCTCase(Graphics2D g2d) {

        if (!UnitUtil.hasAmmo(mech, Mech.LOC_CT)) {
            return;
        }

        int lineStart = 236;
        int linePoint = 458;

        g2d.setFont(UnitUtil.deriveFont(7.0f));
        g2d.drawString("(CASE)", lineStart, linePoint);
    }

    private void printArmorPoints(Graphics2D g2d, Vector<float[]> pipPoints, float totalArmor) {
        pipPoints.trimToSize();
        float pipSpace = (float) pipPoints.size() / totalArmor;
        for (float pos = 0; pos < pipPoints.size(); pos += pipSpace) {
            int currentPip = (int) pos;
            ImageHelper.drawArmorPip(g2d, pipPoints.get(currentPip)[0], pipPoints.get(currentPip)[1]);
            if (--totalArmor <= 0) {
                return;
            }
        }
    }

    private void printISPoints(Graphics2D g2d, Vector<float[]> pipPoints, float totalArmor) {
        pipPoints.trimToSize();
        float pipSpace = (float) pipPoints.size() / totalArmor;
        for (float pos = 0; pos < pipPoints.size(); pos += pipSpace) {
            int currentPip = (int) pos;
            ImageHelper.drawISPip(g2d, pipPoints.get(currentPip)[0], pipPoints.get(currentPip)[1]);
            if (--totalArmor <= 0) {
                return;
            }
        }
    }

}