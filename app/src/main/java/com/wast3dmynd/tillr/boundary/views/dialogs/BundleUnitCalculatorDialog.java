package com.wast3dmynd.tillr.boundary.views.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wast3dmynd.tillr.R;

public class BundleUnitCalculatorDialog extends AlertDialog.Builder {

    private int bundles;
    private int unitsPerBundle;
    private int units =0;


    //views
    private TextInputEditText units_display, bundles_edit, units_per_bundle_edit;
    private Button calculate_units_button;


    public BundleUnitCalculatorDialog(final Context context, final DialogListener listener) {
        super(context);

        //inflate layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_bundle_unit_calculator, null, false);
        setView(dialogView);
        final AlertDialog.Builder builder = this;

        //initialize views
        units_display = dialogView.findViewById(R.id.units_display);
        bundles_edit = dialogView.findViewById(R.id.bundles_edit);
        units_per_bundle_edit = dialogView.findViewById(R.id.units_per_bundle_edit);
        calculate_units_button = dialogView.findViewById(R.id.calculate_units_button);

        //set action views
        calculate_units_button.setOnClickListener(new View.OnClickListener() {
            private boolean areBundlesValid() {
                //region bundles validation
                String bundlesString = bundles_edit.getText().toString();
                String warningMessage = "Please provide bundles!";
                if (bundlesString.isEmpty()) {
                    showToastMessage(warningMessage);
                    return false;
                }

                try {
                    bundles = Integer.valueOf(bundlesString);
                    if (bundles < 0) {
                        showToastMessage(warningMessage);
                        return false;
                    }
                } catch (Exception e) {
                    showToastMessage(warningMessage);
                    return false;
                }
                //endregion

                return true;
            }

            private boolean areUnitsPerBundleValid() {
                //region unitsPerBundle validation
                String unitsPerBundleString = units_per_bundle_edit.getText().toString();
                String warningMessage = "Please provide \'Units Per Bundle\'!";
                if (unitsPerBundleString.isEmpty()) {
                    showToastMessage(warningMessage);
                    return false;
                }

                try {
                    unitsPerBundle = Integer.valueOf(unitsPerBundleString);
                    if (unitsPerBundle < 0) {
                        showToastMessage(warningMessage);
                        return false;
                    }
                } catch (Exception e) {
                    showToastMessage(warningMessage);
                    return false;
                }
                //endregion
                return true;
            }

            private void showToastMessage(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            private void calculateUnits() {
                //calculate units
                units = bundles * unitsPerBundle;
            }

            private void displayUnits() {
                //display units
                StringBuilder unitsBuilder = new StringBuilder(String.valueOf(units));
                unitsBuilder.append(" unit");
                unitsBuilder.append(units > 1 ? "s" : "");
                units_display.setText(unitsBuilder.toString());
            }

            @Override
            public void onClick(View view) {

                if (!areBundlesValid()) {
                    return;
                }
                if (!areUnitsPerBundleValid()) {
                    return;
                }

                calculateUnits();

                displayUnits();
            }


        });

        setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {

            private boolean isListenerValid() {
                if (listener == null) return false;
                else return true;
            }

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (!isListenerValid()) return;

                listener.onUnitsCalculated(units);

            }
        });

        //show this dialog
        builder.create().show();
    }

    public interface DialogListener {
        void onUnitsCalculated(int units);
    }
}
