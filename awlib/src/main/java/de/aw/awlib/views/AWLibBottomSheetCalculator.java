/*
 * MonMa: Eine freie Android-App fuer Verwaltung privater Finanzen
 *
 * Copyright [2015] [Alexander Winkler, 23730 Neustadt/Germany]
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, see <http://www.gnu.org/licenses/>.
 */
package de.aw.awlib.views;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import de.aw.awlib.R;
import de.aw.awlib.bottomsheet.ExpandedBottomSheetDialogFragment;

/**
 * Zeigt einen Rechner als BottomSheet
 */
public class AWLibBottomSheetCalculator extends ExpandedBottomSheetDialogFragment {
    private static final int layout = R.layout.awlib_calculatorsheet;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dlg = super.onCreateDialog(savedInstanceState);
        dlg.setContentView(layout);
        return dlg;
    }
}