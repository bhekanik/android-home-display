package com.bhekanik.homedisplay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

final class DisplaySettingsDialog {
    private DisplaySettingsDialog() {}

    static void show(Activity activity, SharedPreferences preferences, Runnable saved) {
        LinearLayout contents = new LinearLayout(activity);
        contents.setOrientation(LinearLayout.VERTICAL);
        int padding = Math.round(24 * activity.getResources().getDisplayMetrics().density);
        contents.setPadding(padding, padding / 2, padding, padding / 2);
        TextView label = new TextView(activity);
        label.setText("Dashboard address");
        contents.addView(label);
        EditText address = new EditText(activity);
        address.setSingleLine(true);
        address.setHint("https://your-dashboard.example/");
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        address.setText(preferences.getString("dashboard_url", activity.getString(R.string.default_dashboard_url)));
        contents.addView(address);
        CheckBox night = new CheckBox(activity);
        night.setText("Dim from 22:00 to 07:00 (tablet's local time)");
        night.setChecked(preferences.getBoolean("night_dim", true));
        contents.addView(night);
        TextView hint = new TextView(activity);
        hint.setText("The dashboard stays visible at low brightness overnight. Touch it to brighten it for two minutes. Hold Volume Up to open these settings.");
        contents.addView(hint);
        Button systemSettings = new Button(activity);
        systemSettings.setText("Android settings");
        systemSettings.setOnClickListener(view -> activity.startActivity(new Intent(Settings.ACTION_SETTINGS)));
        contents.addView(systemSettings);
        Button homeSettings = new Button(activity);
        homeSettings.setText("Choose Home app");
        homeSettings.setOnClickListener(view -> activity.startActivity(new Intent(Settings.ACTION_HOME_SETTINGS)));
        contents.addView(homeSettings);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.app_name).setView(contents)
                .setNegativeButton("Cancel", null).setPositiveButton("Save", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                String validated = DashboardUrl.validate(address.getText().toString());
                preferences.edit().putString("dashboard_url", validated)
                        .putBoolean("night_dim", night.isChecked()).apply();
                dialog.dismiss();
                saved.run();
            } catch (IllegalArgumentException exception) {
                address.setError(exception.getMessage());
            }
        }));
        dialog.show();
    }
}
