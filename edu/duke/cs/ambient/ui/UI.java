/*
 * Created on Jul 14, 2005
 */
package edu.duke.cs.ambient.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;

/**
 * @author Marcin Dobosz
 */
public class UI {

    private static class TextSetter implements Runnable {
        private String text;

        private Control c;

        public TextSetter(Control c, String t) {
            this.c = c;
            text = t;
        }

        public void run() {
            if (!c.isDisposed()) {
                if (c instanceof Text)
                    ((Text) c).setText(text);
                if (c instanceof Label)
                    ((Label) c).setText(text);
            }
        }
    }

    private static class ProgressSetter implements Runnable {
        private ProgressBar bar;

        private int p;

        public ProgressSetter(ProgressBar bar, int p) {
            this.bar = bar;
            this.p = p;
        }

        public void run() {
            if (!bar.isDisposed()) {
                bar.setSelection(p);
            }
        }
    }

    public static void asyncSetText(Display d, Control c, String text) {
        d.asyncExec(new TextSetter(c, text));
    }

    public static void asyncSetProgress(Display d, ProgressBar bar, int p) {
        d.asyncExec(new ProgressSetter(bar, p));
    }

    public static GridData createData(int style, int hSpan) {
        return createData(style, hSpan, 1);
    }

    public static GridData createData(int style, int hSpan, int vSpan) {
        GridData result = new GridData(style);
        result.horizontalSpan = hSpan;
        result.verticalSpan = vSpan;
        return result;
    }

    public static Label createLabel(Composite parent, int style, String text,
            int hSpan) {
        Label result = new Label(parent, style);
        result.setLayoutData(createData(SWT.NONE, hSpan));
        if (text != null)
            result.setText(text);
        return result;
    }

    public static Label createLabel(Composite parent, int style, String text) {
        return createLabel(parent, style, text, 1);
    }

    public static Label createSimpleLabel(Composite parent, String text) {
        return createLabel(parent, SWT.NONE, text);
    }

    public static Label createSeparator(Composite parent) {
        int hSpan = ((GridLayout) parent.getLayout()).numColumns;
        return createSeparator(parent, hSpan);
    }

    public static Label createSeparator(Composite parent, int hSpan) {
        Label result = createLabel(parent, SWT.HORIZONTAL | SWT.SEPARATOR, null);
        result.setLayoutData(createData(GridData.HORIZONTAL_ALIGN_FILL, hSpan));
        return result;
    }
}
