package io.ffem.collect.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TableRow;
import android.widget.TextView;

import org.odk.collect.android.R;

public class RowView extends TableRow {
    private final TextView textPrimary;
    private final TextView textSecondary;

    public RowView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            inflater.inflate(R.layout.row_view, this, true);
        }

        TableRow tableRow = (TableRow) getChildAt(0);
        textPrimary = (TextView) tableRow.getChildAt(0);
        textSecondary = (TextView) tableRow.getChildAt(1);
    }

    public RowView(Context context) {
        this(context, null);
    }

    public void setPrimaryText(String s) {
        textPrimary.setText(s);
    }

    public void setSecondaryText(String s) {
        textSecondary.setText(s);
    }
}
