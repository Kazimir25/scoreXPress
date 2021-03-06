package fr.cm.scorexpress.ihm.editor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import fr.cm.common.widget.MyToolkit;
import fr.cm.common.widget.StandardToolKit;
import fr.cm.common.widget.button.ButtonAdapter;
import fr.cm.common.widget.button.ButtonModel;
import fr.cm.common.widget.composite.AbstractCompositeBuilder;
import fr.cm.common.widget.composite.CommonCompositeBuilder;
import fr.cm.common.widget.composite.CompositeBuilder;
import fr.cm.common.widget.table.TableBuilder;
import fr.cm.common.widget.table.TableModel;
import fr.cm.common.widget.text.TextModel;
import fr.cm.common.workbench.WorkbenchUtils;
import fr.cm.scorexpress.core.AutoResizeColumn;
import fr.cm.scorexpress.core.model.ColTable;
import fr.cm.scorexpress.core.model.ObjResultat;
import fr.cm.scorexpress.core.model.impl.DateUtils;
import fr.cm.scorexpress.core.model.impl.ObjStep;
import fr.cm.scorexpress.ihm.application.ImageReg;
import fr.cm.scorexpress.ihm.application.ScoreXPressPlugin;
import fr.cm.scorexpress.ihm.editor.input.CommonEditorInput;
import fr.cm.scorexpress.ihm.editor.input.EtapeEditorInput;
import fr.cm.scorexpress.ihm.editor.input.ResultatEditorInput;
import fr.cm.scorexpress.ihm.editor.input.StepEditorInput;
import fr.cm.scorexpress.ihm.print.IPrintable;
import fr.cm.scorexpress.model.StepModel;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import static fr.cm.common.widget.composite.CompositeBuilders.createCompositeBuilder;
import static fr.cm.common.workbench.WorkbenchUtils.defineCopyViewSiteAction;
import static fr.cm.scorexpress.ihm.application.ImageReg.getImg;
import static fr.cm.scorexpress.ihm.application.ScoreXPressPlugin.*;
import static fr.cm.scorexpress.ihm.editor.ChronosEditor.CHRONOS_EDITOR_ID;
import static fr.cm.scorexpress.ihm.editor.PenaliteEditor.PENALITY_EDITOR_ID;
import static fr.cm.scorexpress.ihm.editor.ResultatEtapeEditor.LaunchEditorAction.*;
import static fr.cm.scorexpress.ihm.editor.StepEditor.STEP_EDITOR_ID;
import static fr.cm.scorexpress.ihm.editor.i18n.Message.i18n;
import static fr.cm.scorexpress.ihm.editor.i18n.Messages.*;
import static fr.cm.scorexpress.ihm.print.PrintPreview.openPrintPreview;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.eclipse.jface.viewers.StyledCellLabelProvider.COLORS_ON_SELECTION;
import static org.eclipse.swt.layout.GridData.*;

public class ResultatEtapeEditor extends EditorPart implements IPrintable, IAutoAjustColumnEditor {
    public static final String             RESULTAT_ETAPE_EDITOR_ID = "fr.cm.scorexpress.editor.ResultatEtapeEditor";
    private             Image              toggleImage              = null;
    private             Image              refreshImage             = null;
    private             Image              configEtapeImage         = null;
    private             Image              configPenaliteImage      = null;
    private             Image              configChronosImage       = null;
    private             ResultatEtapeModel model                    = null;
    private             Table              table                    = null;
    private             CompositeBuilder   builder;
    private             CompositeBuilder   compositeBuilder;

    @Override
    public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
        if (!(input instanceof ResultatEditorInput)) {
            throw new PartInitException(ResultatEtapeEditor_Invalid_Input_Doit_etre_une_etape);
        }
        model = ((CommonEditorInput<ResultatEtapeModel>) input).getModel();
        setSite(site);
        setInput(input);
        model.getConfigEtapeButtonModel().addWidgetListener(createStepEditorAction(this, model.getStepModel(), model));
        model.getConfigPenalityButtonModel().addWidgetListener(createPenalityEditorAction(this, model));
        model.getChronosButtonModel().addWidgetListener(createChronosEditorAction(this, model));
        setPartName(model.getLabel());
        model.updateCalcul();
        if (model.getMode() == 1) {
            setTitleImage(ImageReg.getImg(ScoreXPressPlugin.IMG_RED_RESULT));
        } else if (model.getMode() == 2) {
            setTitleImage(ImageReg.getImg(ScoreXPressPlugin.IMG_GREEN_RESULT));
        }
    }

    @Override
    public void createPartControl(final Composite parent) {
        final MyToolkit toolkit = new StandardToolKit();
        toggleImage = getImg(IMG_TOGGLE);
        refreshImage = getImg(IMG_REFRESH);
        configEtapeImage = getImg(IMG_ETAPE);
        configPenaliteImage = getImg(IMG_PENALITY);
        configChronosImage = getImg(IMG_BALISE);
        parent.setLayout(new FillLayout());
        defineCopyViewSiteAction(parent.getDisplay(), getEditorSite().getActionBars());
        compositeBuilder = createCompositeBuilder(toolkit, parent, SWT.NONE).withLayout(new GridLayout());
        createTitle(compositeBuilder);
        createMenuBar(compositeBuilder);
        table = createTable(compositeBuilder);
        createFootBar(compositeBuilder);
    }

    private void createTitle(final CommonCompositeBuilder<Composite, CompositeBuilder> builder) {
        builder.addLabel(model.getTitleLabelModel(), SWT.NONE)
                .withFont(new Font(Display.getDefault(), "Tahoma", 14, SWT.NORMAL))
                .withLayoutData(new GridData(VERTICAL_ALIGN_CENTER | GRAB_HORIZONTAL | HORIZONTAL_ALIGN_CENTER));
    }

    private void createMenuBar(final AbstractCompositeBuilder<CompositeBuilder> parent) {
        final CompositeBuilder builder =
                parent.addComposite(SWT.NONE).withLayout(new GridLayout(8, false))
                        .withLayoutData(new GridData(GRAB_HORIZONTAL | FILL_HORIZONTAL));
        final GridData gridDataCategorieCombo = new GridData();
        gridDataCategorieCombo.widthHint = 200;
        gridDataCategorieCombo.minimumWidth = 200;
        builder.addButton(model.getCategoryButtonModel(), SWT.CHECK).withImage(toggleImage);
        builder.addStaticLabel(SWT.NONE).withText(ResultatEtapeEditor_Tri);
        builder.addCombo(model.getCategoriesComboboxModel(), SWT.BORDER).withLayoutData(gridDataCategorieCombo);
        builder.addLabel(model.getInfoLabel(), SWT.BOLD)
                .withLayoutData(new GridData(GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
        builder.addButton(model.getConfigEtapeButtonModel(), SWT.NONE).withImage(configEtapeImage)
                .withToolTip(ResultatEtapeEditor_Afficher_la_configuration_de_l_etape)
                .withLayoutData(new GridData(HORIZONTAL_ALIGN_END));
        builder.addButton(model.getConfigPenalityButtonModel(), SWT.NONE).withImage(configPenaliteImage)
                .withToolTip(ResultatEtapeEditor_Afficher_la_configuration_des_penalitees);
        builder.addButton(model.getChronosButtonModel(), SWT.NONE).withImage(configChronosImage)
                .withToolTip(ResultatEtapeEditor_Afficher_les_chronos);
        builder.addButton(model.getRefreshButtonModel(), SWT.NONE).withImage(refreshImage)
                .withToolTip(ResultatEtapeEditor_Actualiser_les_calculs);
    }

    private Table createTable(final CommonCompositeBuilder<Composite, CompositeBuilder> builder) {
        final TableModel<ObjResultat> tableModel = model.getTableResultatModel();
        final TableBuilder<ObjResultat> tableBuilder =
                builder.addTable(tableModel, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | COLORS_ON_SELECTION)
                        .withLine(true).selectionProvider(getSite());
        tableBuilder.withLayoutData(new GridData(FILL_BOTH | GRAB_HORIZONTAL | GRAB_VERTICAL));
        for (final ColTable colTable : model.getColumnConfig()) {
            tableBuilder.addColumn(colTable.getChamp(), colTable.getLib(), colTable.getAlign())
                    .withWidth(colTable.getWidth()).movable(true).withToolTipText(colTable.getLib())
                    .withRenderer(new ResultatEtapeColumnRenderer(colTable, model));
        }
        tableBuilder.withLayoutData(new GridData(FILL_BOTH | GRAB_HORIZONTAL | GRAB_VERTICAL)).withHeader(true);

        //        ViewColumnViewerToolTipSupport.enableFor(tableBuilder.getViewer());
        getSite().setSelectionProvider(tableBuilder.getViewer());
        return tableBuilder.getTable();
    }

    private void createFootBar(final AbstractCompositeBuilder<CompositeBuilder> builder) {
        final CompositeBuilder footBar =
                builder.addComposite(SWT.NONE).withLayoutData(new GridData(FILL_HORIZONTAL | HORIZONTAL_ALIGN_FILL))
                        .withLayout(new GridLayout(3, false));
        footBar.addStaticLabel(SWT.NONE).withText(ResultatEtapeEditor_Info);
        final GridData          layoutData         = new GridData(300, 20);
        final TextModel<String> printInfoTextModel = model.getPrintInfoTextModel();
        footBar.addText(printInfoTextModel, SWT.BORDER).withLayoutData(layoutData);
        final ButtonModel dateTimeModel = new ButtonModel(i18n("ResultatEditor.footbar.addDateTime"));
        footBar.addButton(dateTimeModel, SWT.NONE);
        dateTimeModel.addWidgetListener(new ButtonAdapter() {
            @Override
            public void click() {
                printInfoTextModel.setText(printInfoTextModel.getText() + "${dateTime}");
            }
        });

        footBar.addStaticLabel(SWT.NONE).withText(i18n("Result.search"));
        footBar.addText(this.model.getSearchTextModel(), SWT.BORDER | SWT.SINGLE).withLayoutData(new GridData(300, 20));
        footBar.addCheckbox(this.model.getFilterBySearchModel(), SWT.NONE).withLayoutData(new GridData(100, 20));
    }

    public ResultatEtapeModel getModel() {
        return model;
    }

    @Override
    public void dispose() {
        compositeBuilder.dispose();
        model.dispose();
        super.dispose();
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        model.getStep().setInfo(ObjStep.VAR_TITLE_PRINT, model.getPrintInfoTextModel().getText());
        setDirty(false);
    }

    @Override
    public boolean isDirty() {
        return model.isDirty();
    }

    public void setDirty(final boolean dirtyState) {
        model.setDirty(dirtyState);
        firePropertyChange(PROP_DIRTY);
    }

    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void print() {
        try {
            final AbstractList<String> titles = new ArrayList<String>();
            final ObjStep              etape  = model.getStep();
            final String               title  = etape.getManif().getNom();
            String                     title2 = ResultatEtapeEditor_Classement + ' ' + etape.getLib();
            if (model.getCategoriesComboboxModel().getText() != null) {
                title2 += " - " + model.getCategoriesComboboxModel().getText();
            }
            titles.add(title);
            titles.add(title2);
            if (!model.getPrintInfoTextModel().getText().equals(EMPTY)) {
                final String   printInfo = model.getPrintInfoTextModel().getText();
                final Calendar calendar  = Calendar.getInstance();
                calendar.set(Calendar.SECOND, 0);
                titles.add(printInfo.replace("${dateTime}", DateUtils.showDate(calendar.getTime())));
            }
            final String[] titlesStr = new String[titles.size()];
            int            i         = 0;
            for (Iterator<String> iter = titles.iterator(); iter.hasNext(); i++) {
                titlesStr[i] = iter.next();
            }
            final String type;
            switch (model.getMode()) {
                case 1:
                    type = " (Details)";
                    break;
                case 2:
                    type = " (Inter)";
                    break;
                default:
                    type = "";
            }
            for (int col = 0; col < table.getColumns().length; col++) {
                int size = 0;
                for (final TableItem item : table.getItems()) {
                    final int textLenght = item.getText(col).length();
                    size = textLenght > size ? textLenght : size;
                }
                if (size == 0) {
                    table.getColumn(col).setWidth(0);
                }
            }

            final DateFormat sdf = new SimpleDateFormat("yyMMdd");
            openPrintPreview(table, titlesStr, ResultatEtapeEditor_RESULTATS_PRINT_TEXT + etape.getLib() + type + ' ' +
                                               sdf.format(new Date()));
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    @Override
    public AutoResizeColumn getAutoResizeContext() {
        return model.getAutoResizeContext();
    }

    public static class LaunchEditorAction extends ButtonAdapter {
        private final String              id;
        private final IEditorInput        input;
        private final ResultatEtapeEditor editor;

        LaunchEditorAction(final ResultatEtapeEditor editor, final IEditorInput input, final String id) {
            this.editor = editor;
            this.id = id;
            this.input = input;
        }

        static LaunchEditorAction createStepEditorAction(final ResultatEtapeEditor editor, final StepModel stepModel, final ResultatEtapeModel model) {
            return new LaunchEditorAction(editor, new StepEditorInput(new StepEditorModel(stepModel)), STEP_EDITOR_ID);
        }

        static LaunchEditorAction createChronosEditorAction(final ResultatEtapeEditor editor, final ResultatEtapeModel model) {
            return new LaunchEditorAction(editor, new EtapeEditorInput(model.getStep(), CHRONOS_EDITOR_ID,
                                                                       model.getAutoResizeContext()),
                                          CHRONOS_EDITOR_ID);
        }

        static LaunchEditorAction createPenalityEditorAction(final ResultatEtapeEditor editor, final ResultatEtapeModel model) {
            return new LaunchEditorAction(editor, new EtapeEditorInput(model.getStep(), CHRONOS_EDITOR_ID,
                                                                       model.getAutoResizeContext()),
                                          PENALITY_EDITOR_ID);
        }

        @Override
        public void click() {
            WorkbenchUtils.openEditor(editor, input, id);
        }
    }

}
