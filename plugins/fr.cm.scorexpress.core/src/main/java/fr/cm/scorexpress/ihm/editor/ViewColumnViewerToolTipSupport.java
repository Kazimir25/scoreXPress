package fr.cm.scorexpress.ihm.editor;

import java.util.ArrayList;
import java.util.Collection;

import fr.cm.common.widget.MyToolkit;
import fr.cm.common.widget.StandardToolKit;
import fr.cm.common.widget.button.ButtonListener;
import fr.cm.common.widget.button.ButtonModel;
import fr.cm.common.widget.composite.CompositeBuilder;
import fr.cm.common.widget.label.LabelModel;
import fr.cm.scorexpress.core.model.AbstractGetInfo;
import fr.cm.scorexpress.core.model.ObjResultat;
import fr.cm.scorexpress.core.model.impl.Date2;
import fr.cm.scorexpress.ihm.print.ImagePrintUtils;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

import static fr.cm.common.widget.composite.CompositeBuilders.createCompositeBuilder;
import static fr.cm.scorexpress.ihm.editor.i18n.Message.i18n;

public class ViewColumnViewerToolTipSupport extends ColumnViewerToolTipSupport {

    public static final  Color RED           = new Color(Display.getCurrent(), 255, 0, 0);
    public static final  Color BLACK         = new Color(Display.getCurrent(), 0, 0, 0);
    public static final  Color GREEN         = new Color(Display.getCurrent(), 69, 181, 21);
    public static final  Color BLUE          = new Color(Display.getCurrent(), 0, 0, 255);
    private static final Font  FONT          = new Font(Display.getCurrent(), new FontData("Courier", 10, SWT.NORMAL));
    private static final Font  BOLD          = new Font(Display.getCurrent(), new FontData("Courier", 10, SWT.BOLD));
    private static final Font  ITALIC        = new Font(Display.getCurrent(), new FontData("Courier", 10, SWT.ITALIC));
    public static final  Color WHITE         = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    public static final  int   MAX_LINE_SIZE = 60;

    protected ViewColumnViewerToolTipSupport(ColumnViewer viewer, int style, boolean manualActivation) {
        super(viewer, style, manualActivation);
        setHideDelay(2000);
        setHideOnMouseDown(false);
    }

    @Override
    protected Composite createViewerToolTipContentArea(Event event, ViewerCell cell, final Composite parent) {
        final ObjResultat resultat = (ObjResultat) ((ViewerCell) getToolTipArea(event)).getElement();
        return updateResultat(parent, resultat);
    }

    public static Composite updateResultat(Composite parent, ObjResultat resultat) {
        final MyToolkit        toolkit = new StandardToolKit();
        final CompositeBuilder root    = createCompositeBuilder(toolkit, parent, SWT.NONE);
        root.withLayout(new RowLayout(SWT.VERTICAL));
        root.withBackground(WHITE);
        final CompositeBuilder builder = root.addComposite(SWT.NONE);
        builder.withBackground(WHITE);
        builder.withLayout(new GridLayout(2, false));

        builder.addLabel(new LabelModel(resultat.getDossard().getNum() + ". " + resultat.getDossard().getInfoStr("S.FIRSTNAME"))).withFont(BOLD).withBackground(WHITE);
        builder.addLabel(new LabelModel("")).withBackground(WHITE);

        addInfo(i18n("Result.tooltip.finalTime"), ObjResultat.VAR_RESULTAT_TEMPS, resultat, builder, BLUE, FONT, FONT);
        String extra = "";
        if (!resultat.getTempsArretChronoResultat().isNull()) {
            extra = " = " + resultat.getInfo(ObjResultat.VAR_TEMPSPARCOURS) + " " + resultat.getTempsArretChronoResultat();
        }
        addInfoDate(i18n("Result.tooltip.chronoTime"), ObjResultat.VAR_TEMPS_CHRONO, resultat, builder, BLACK, extra);
        addInfoDate(i18n("Result.tooltip.chronoMini"), ObjResultat.VAR_TEMPS_CHRONO_MINI, resultat, builder, BLACK);
        addInfoDate(i18n("Result.tooltip.bonusTime"), ObjResultat.VAR_BONIFICATION, resultat, builder, GREEN);
        addInfoDate(i18n("Result.tooltip.otherPenalityTime"), ObjResultat.VAR_PENALITE_AUTRE, resultat, builder, RED);
        addInfoDate(i18n("Result.tooltip.penalityTime"), ObjResultat.VAR_PENALITE_BALISE, resultat, builder, RED);
        addInfo(i18n("Result.tooltip.nbBalise"), ObjResultat.VAR_NB_BALISE, resultat, builder, BLUE, FONT, FONT);
        addInfo(i18n("Result.tooltip.nbPenaliteBalise"), ObjResultat.VAR_NB_PENALITE, resultat, builder, RED, FONT, FONT);
        addInfo(i18n("Result.tooltip.nbBaliseBonus"), ObjResultat.VAR_NB_BALISE_BONUS, resultat, builder, GREEN, FONT, FONT);
        addInfo(i18n("Result.tooltip.missingBalise"), ObjResultat.VAR_RESULTAT_BALISES_PENALITES, resultat, builder, RED, FONT, FONT);
        addInfo(i18n("Result.tooltip.baliseBonus"), ObjResultat.VAR_RESULTAT_BALISESBONUS, resultat, builder, GREEN, FONT, FONT);
        addInfo(i18n("Result.tooltip.baliseDisordered"), ObjResultat.VAR_RESULTAT_BALISE_DISORDERED, resultat, builder, RED, FONT, FONT);
        addInfo(i18n("Result.tooltip.missingBaliseOption"), ObjResultat.VAR_RESULTAT_BALISES_OPTIONS, resultat, builder, BLACK, FONT, ITALIC);
        addInfo(i18n("Result.tooltip.baliseList"), ObjResultat.VAR_RESULTAT_BALISES_OK, resultat, builder, BLACK, FONT, FONT);

        final ButtonModel capture = new ButtonModel("Imprimer");
        final Composite   control = root.getControl();
        capture.addWidgetListener(new ButtonListener() {
            boolean printed = false;

            @Override
            public void click() {
                if (!printed) {
                    printed = true;
                    ImagePrintUtils.printControl(builder.getControl());
                }
            }

            @Override
            public void onActivate() {

            }
        });

        root.addButton(capture, SWT.PUSH);
        control.pack();
        return control;
    }

    private static void addInfoDate(final String label, final String info, final AbstractGetInfo resultat, final CompositeBuilder composite, final Color color, final String... extra) {
        final Date2 date = (Date2) resultat.getInfo(info);
        if (date != null && !date.isNull()) {
            composite.addLabel(new LabelModel(label), SWT.NONE).withFont(FONT).withBackground(WHITE);
            if (extra.length > 0) {
                final CompositeBuilder extrComp = composite.addComposite(SWT.NONE).withBackground(WHITE);
                extrComp.withLayout(new GridLayout(2, false));
                extrComp.addLabel(new LabelModel(date + "")).withForeground(color).withFont(FONT).withBackground(WHITE);
                extrComp.addLabel(new LabelModel(extra[0] + "")).withForeground(BLUE).withFont(FONT).withBackground(WHITE);
            } else {
                composite.addLabel(new LabelModel(date + ""), SWT.NONE).withFont(FONT).withForeground(color).withBackground(WHITE);
            }
        }
    }

    private static void addInfo(final String label, final String attribute, final AbstractGetInfo resultat, final CompositeBuilder composite, final Color color, Font font, Font fontValue) {
        final String        element = resultat.getInfoStr(attribute);
        final StringBuilder builder = new StringBuilder();
        if (!element.isEmpty()) {
            Collection<String> lines = new ArrayList<String>();
            if (element.length() > MAX_LINE_SIZE) {
                int length = 0;
                for (final String split : element.split("]")) {
                    length += split.length() + 1;
                    if (length < MAX_LINE_SIZE) {
                        builder.append(split).append(']');
                    } else {
                        lines.add(builder.toString());
                        builder.setLength(0);
                        builder.append(split).append(']');
                    }
                    length = builder.length();
                }
                if (length > 0) {
                    lines.add(builder.toString());
                }

            } else {
                lines.add(element);
            }
            boolean start = false;
            for (final String line : lines) {
                if (!start) {
                    start = true;
                    composite.addLabel(new LabelModel(label), SWT.NONE).withFont(font).withBackground(WHITE);
                } else {
                    composite.addLabel(new LabelModel(""), SWT.NONE).withFont(font).withBackground(WHITE);
                }
                composite.addLabel(new LabelModel(line), SWT.NONE).withForeground(color).withBackground(WHITE).withFont(fontValue);
            }
        }
    }

    public static final void enableFor(final ColumnViewer viewer) {
        new ViewColumnViewerToolTipSupport(viewer, org.eclipse.jface.window.ToolTip.NO_RECREATE, false);
    }
}
