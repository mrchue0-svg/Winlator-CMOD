package com.ryl2.remastered;

import com.winlator.cmod.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.database.Cursor;
import android.provider.OpenableColumns;
import com.github.junrar.Junrar;
import java.io.*;
import java.util.zip.*;

public class Ryl2LauncherActivity extends Activity {

    void prepareRyl2Containers() {
        com.winlator.cmod.container.ContainerManager manager =
            new com.winlator.cmod.container.ContainerManager(this);

        try {
            org.json.JSONObject high = new org.json.JSONObject();
            high.put("name", "RYL2 HIGH");
            high.put("screenSize", "1280x720");
            high.put("drives", "F:/storage/emulated/0/Download;D:/storage/emulated/0/Android/data/com.ryl2remastered.winlator/files/RYL2Client");

            org.json.JSONObject low = new org.json.JSONObject();
            low.put("name", "RYL2 LOW");
            low.put("screenSize", "800x600");
            low.put("drives", "F:/storage/emulated/0/Download;D:/storage/emulated/0/Android/data/com.ryl2remastered.winlator/files/RYL2Client");

            java.io.File imageFsRoot =
                new java.io.File(getFilesDir(), "imagefs");

            com.winlator.cmod.xenvironment.ImageFs imageFs =
                com.winlator.cmod.xenvironment.ImageFs.find(imageFsRoot);

            if (imageFs == null) {
                android.util.Log.e("RYL2",
                    "ImageFS belum tersedia. Container ditangguhkan.");
                return;
            }

            android.util.Log.i("RYL2",
                "ImageFS tersedia untuk penciptaan container.");

            com.winlator.cmod.contents.ContentsManager contentsManager =
                new com.winlator.cmod.contents.ContentsManager(this);

            manager.createContainerAsync(high, contentsManager, container -> {
                if (container == null) {
                    android.util.Log.e("RYL2", "HIGH container failed");
                    return;
                }

                manager.createContainerAsync(low, contentsManager, lowContainer -> {
                    if (lowContainer == null) {
                        android.util.Log.e("RYL2", "LOW container failed");
                        return;
                    }

                    prefs.edit()
                        .putBoolean("ryl2_auto_install_done", true)
                        .apply();

                    android.util.Log.i("RYL2",
                        "HIGH and LOW containers created");

                    runOnUiThread(() ->
                        status.setText("RYL2 INSTALLATION COMPLETE")
                    );
                });
            });
        } catch (org.json.JSONException e) {
            android.util.Log.e("RYL2", "Container setup failed", e);
        }
    }



    void installBundledRyl2() {
        new Thread(() -> {
            try {
                File base = new File(getExternalFilesDir(null), "RYL2Client");
                if (!base.exists() && !base.mkdirs())
                    throw new IOException("Cannot create game folder");

                File archive = new File(
                    getCacheDir(), "REMASTERED.rar"
                );

                try (InputStream in = getAssets().open("ryl2/REMASTERED.rar");
                     FileOutputStream out = new FileOutputStream(archive)) {
                    byte[] buffer = new byte[65536];
                    int n;
                    while ((n = in.read(buffer)) != -1)
                        out.write(buffer, 0, n);
                }

                com.github.junrar.Junrar.extract(archive, base);
                archive.delete();

                runOnUiThread(() -> {
                    prefs.edit()
                        .putString("client_status", "EXTRACTED")
                        .apply();
                    prepareRyl2Containers();
                    status.setText("CLIENT STATUS: EXTRACTED");
                    new AlertDialog.Builder(this)
                        .setTitle("RYL2 CLIENT INSTALLED")
                        .setMessage("Client berjaya diekstrak.")
                        .setPositiveButton("OK", null)
                        .show();
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                    new AlertDialog.Builder(this)
                        .setTitle("INSTALLATION ERROR")
                        .setMessage(e.toString())
                        .setPositiveButton("OK", null)
                        .show()
                );
            }
        }).start();
    }


    LinearLayout root;
    TextView status;
    int gold = Color.rgb(235,190,91);
    int dark = Color.rgb(5,10,22);
    static final int PICK_CLIENT = 100;
    SharedPreferences prefs;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("ryl2_settings", MODE_PRIVATE);

        if (!prefs.getBoolean("ryl2_auto_install_done", false)) {
            // Installation is marked complete only after extraction succeeds.
            installBundledRyl2();
        }

        getWindow().setStatusBarColor(dark);
        getWindow().setNavigationBarColor(dark);
        showHome();
    }

    GradientDrawable panel() {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.argb(220,5,13,29));
        d.setCornerRadius(18);
        d.setStroke(2,gold);
        return d;
    }

    TextView makeText(String s,int size,int color) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setTypeface(Typeface.SERIF,Typeface.BOLD);
        return t;
    }

    void showHome() {
        FrameLayout frame = new FrameLayout(this);
        ImageView bg = new ImageView(this);
        bg.setImageResource(R.drawable.launcher_bg);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        frame.addView(bg,new FrameLayout.LayoutParams(-1,-1));
        View shade = new View(this);
        shade.setBackgroundColor(Color.argb(115,0,5,20));
        frame.addView(shade,new FrameLayout.LayoutParams(-1,-1));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(25,18,25,28);
        scroll.addView(root);
        frame.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
        setContentView(frame);

        root.addView(makeText("RYL2",42,gold));
        root.addView(makeText("REMASTERED MOBILE",19,Color.WHITE));
        root.addView(makeText("⚔ BUILD YOUR OWN PATH ⚔",12,gold));

        addSectionTitle("HIGH PROFILE  |  ADRENO");
        addButton("⚔ START GAME (HIGH) ⚔",() -> startProfile("HIGH"));
        addButton("✦ CLIENT SETUP (HIGH) ✦",() -> launchClientSetup("HIGH"));
        addButton("⚙ CONTAINER SETUP (HIGH) ⚙",() -> containerSetup("HIGH"));

        addSectionTitle("LOW PROFILE  |  MALI");
        addButton("⚔ START GAME (LOW) ⚔",() -> startProfile("LOW"));
        addButton("✦ CLIENT SETUP (LOW) ✦",() -> launchClientSetup("LOW"));
        addButton("⚙ CONTAINER SETUP (LOW) ⚙",() -> containerSetup("LOW"));

        status = makeText("CLIENT STATUS: "+
            prefs.getString("client_status","NOT INSTALLED"),14,gold);
        status.setBackground(panel()); status.setPadding(12,12,12,12);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2);
        sp.setMargins(0,18,0,14); root.addView(status,sp);

        addSectionTitle("ADVANCED WINLATOR SETTINGS");
        addButton("🎮 INPUT CONTROL", () -> openRyl2InputControls());
        addButton("⚡ PERFORMANCE",() -> settingsCategory("Performance",
            new String[]{"Box64 preset: Performance","Box64 preset: Balanced","Box64 preset: Compatibility","CPU / memory options"}));
        addButton("🎮 GRAPHICS",() -> settingsCategory("Graphics",
            new String[]{"Graphics driver","DX Wrapper","Resolution","VSync / frame options"}));
        addButton("🖥 DISPLAY",() -> settingsCategory("Display",
            new String[]{"Screen resolution","DPI scaling","Fullscreen","Aspect ratio"}));
        addButton("🖱 MOUSE & TOUCH",() -> settingsCategory("Mouse & Touch",
            new String[]{"Mouse sensitivity","Swipe speed","Relative mouse","Touch response"}));
        addButton("♫ AUDIO",() -> settingsCategory("Audio",
            new String[]{"Audio driver","Audio buffer","Sound output"}));
        addButton("⚙ SYSTEM & RUNTIME",() -> settingsCategory("System & Runtime",
            new String[]{"Wine version","Windows version","Runtime dependencies","Environment options"}));
        addButton("SAVE CUSTOM PROFILE",() -> saveProfile());
        root.addView(makeText("POWERED BY WINLATOR / CMOD",10,Color.WHITE));
    }

    void addSectionTitle(String title) {
        TextView t = makeText(title,14,gold);
        t.setPadding(0,22,0,5); root.addView(t);
    }

    void addButton(String label,Runnable action) {
        TextView b = makeText(label,14,gold);
        b.setBackground(panel()); b.setPadding(12,16,12,16);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,6,0,6); root.addView(b,p);
        b.setOnClickListener(v -> action.run());
    }

    static final String CMOD_PACKAGE = "com.ryl2remastered.winlator";
    static final String CMOD_ACTIVITY = "com.winlator.cmod.XServerDisplayActivity";

    String profileSummary(String profile) {
        if ("HIGH".equals(profile)) return "GPU: Qualcomm Adreno\nDriver: Turnip\nDX wrapper: DXVK + D7VK\nResolution sasaran: 1024 x 768\nWine: Proton 9.0\nEmulator: Box64\n\nProfil cadangan launcher; belum mengubah tetapan CMOD secara automatik.";
        return "GPU: MediaTek Mali\nDriver: System / Mali (bukan Turnip)\nDX wrapper: WineD3D (baseline)\nResolution sasaran: 800 x 600\nWine: Proton 9.0\nEmulator: Box64\n\nProfil permulaan; keserasian perlu diuji pada peranti Mali sebenar.";
    }


void startProfile(String profile) {
    int id = "HIGH".equals(profile) ? 1 : 2;
    prefs.edit().putString("selected_profile", profile).apply();
    launchCmodContainer(profile, id);
}

    void askContainerId(String profile) {
        EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("ID container CMOD");
        new android.app.AlertDialog.Builder(this)
            .setTitle("SAMBUNG CONTAINER "+profile)
            .setMessage("Masukkan ID container CMOD untuk profil "+profile+".")
            .setView(input)
            .setPositiveButton("SAVE & OPEN CMOD",(d,w) -> {
                try {
                    int id = Integer.parseInt(input.getText().toString().trim());
                    if (id <= 0) throw new NumberFormatException();
                    prefs.edit().putInt(profile+"_container_id",id).apply();
                    launchCmodContainer(profile,id);
                } catch (NumberFormatException e) {
                    message("ID TIDAK SAH","Masukkan nombor ID container yang sah.");
                }
            }).setNegativeButton("CANCEL",null).show();
    }


void launchClientSetup(String profile) {
    int id = "HIGH".equals(profile) ? 1 : 2;
    openRyl2Shortcut(id, "ClientSetup");
}


void launchCmodContainer(String profile, int id) {
    openRyl2Shortcut(id, "Login");
}

void openRyl2Shortcut(int id, String shortcutName) {
    Intent intent = new Intent();
    intent.setClassName(
        getPackageName(),
        "com.winlator.cmod.XServerDisplayActivity"
    );
    intent.putExtra("container_id", id);
    intent.putExtra("shortcut_name", shortcutName);
        intent.putExtra("ryl2_input_profile", "RYL");
    try {
        startActivity(intent);
    } catch (Exception e) {
        message("RYL2 LAUNCH ERROR", e.toString());
    }
}


void containerSetup(String profile) {
    // RYL2_CONTAINER_SETUP_V2
    int id = "HIGH".equalsIgnoreCase(profile) ? 1 : 2;

    android.content.Intent intent =
        new android.content.Intent();

    intent.setClassName(
        getPackageName(),
        "com.winlator.cmod.MainActivity"
    );

    intent.putExtra("ryl2_setup_container_id", id);

    try {
        startActivity(intent);
    } catch (Exception e) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("RYL2 CONTAINER SETUP")
            .setMessage("Winlator CMOD tidak dapat dibuka.")
            .setPositiveButton("OK", null)
            .show();
    }
}


    void settingsCategory(String category,String[] options) {
        String key = "setting_"+category.replace(" ","_");
        String current = prefs.getString(key,"Belum ditetapkan");
        new android.app.AlertDialog.Builder(this)
            .setTitle(category)
            .setMessage("Pilihan semasa: "+current+"\n\nPilih tetapan untuk disimpan:")
            .setItems(options,(dialog,which) -> {
                prefs.edit().putString(key,options[which]).apply();
                message(category+" SAVED",options[which]+
                    "\n\nTetapan disimpan dalam launcher. Integrasi terus ke CMOD akan dibuat dalam fasa seterusnya.");
            }).setNegativeButton("CLOSE",null).show();
    }

    void saveProfile() {
        String selected = prefs.getString("selected_profile","HIGH");
        prefs.edit().putString("saved_profile",selected).apply();
        message("PROFILE SAVED","Profil "+selected+
            " disimpan bersama pilihan Advanced Settings dalam launcher.");
    }

    void message(String title,String msg) {
        new android.app.AlertDialog.Builder(this).setTitle(title)
            .setMessage(msg).setPositiveButton("OK",null).show();
    }

    void pickClient(String profile) {
        prefs.edit().putString("client_setup_profile",profile).apply();
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*"); i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,PICK_CLIENT);
    }

    String getFileName(Uri uri) {
        String name = "client";
        Cursor c = getContentResolver().query(uri,null,null,null,null);
        if(c != null) {
            if(c.moveToFirst()) {
                int n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(n >= 0) name = c.getString(n);
            }
            c.close();
        }
        return name;
    }

    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request != PICK_CLIENT || result != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        if(uri == null) return;
        String name = getFileName(uri).toLowerCase();
        if(name.endsWith(".rar") || name.endsWith(".zip")) extractSelected(uri,name);
        else message("INVALID FILE","Pilih fail RAR atau ZIP sahaja.");
    }

    void extractSelected(Uri uri,String name) {
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("RYL2 CLIENT SETUP");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(100); progress.setProgress(0);
        progress.setMessage("Menyalin fail client...");
        progress.setCancelable(false); progress.show();
        status.setText("CLIENT STATUS: COPYING...");

        new Thread(() -> {
            String error = null; boolean success = false;
            File temp = new File(getCacheDir(),"selected_client_archive");
            try {
                long total = -1;
                try (Cursor c = getContentResolver().query(uri,
                    new String[]{OpenableColumns.SIZE},null,null,null)) {
                    if(c != null && c.moveToFirst()) {
                        int col = c.getColumnIndex(OpenableColumns.SIZE);
                        if(col >= 0 && !c.isNull(col)) total = c.getLong(col);
                    }
                }
                try (InputStream in = getContentResolver().openInputStream(uri);
                     FileOutputStream out = new FileOutputStream(temp)) {
                    if(in == null) throw new IOException("Tidak dapat membaca fail asal.");
                    byte[] buffer = new byte[1024*1024];
                    long copied = 0,lastUpdate = 0; int count;
                    while((count = in.read(buffer)) != -1) {
                        out.write(buffer,0,count); copied += count;
                        if(total > 0 && copied-lastUpdate >= 2*1024*1024) {
                            final int percent = (int)Math.min(99,(copied*100)/total);
                            final long mb = copied/(1024*1024);
                            runOnUiThread(() -> {
                                progress.setIndeterminate(false);
                                progress.setProgress(percent);
                                progress.setMessage("Menyalin client: "+percent+"% ("+mb+" MB)");
                            });
                            lastUpdate = copied;
                        }
                    }
                }
                File base = new File(getExternalFilesDir(null),"RYL2Client");
                if(!base.exists() && !base.mkdirs())
                    throw new IOException("Tidak dapat membuat folder client.");
                runOnUiThread(() -> {
                    progress.setIndeterminate(true);
                    progress.setMessage("Extracting "+name+"\nSila tunggu, jangan tutup aplikasi.");
                    status.setText("CLIENT STATUS: EXTRACTING...");
                });
                if(name.endsWith(".rar")) Junrar.extract(temp,base);
                else extractZip(temp,base);
                success = true;
            } catch(Exception e) {
                error = e.getMessage();
                if(error == null) error = e.getClass().getSimpleName();
            } finally {
                if(temp.exists()) temp.delete();
            }
            final boolean ok = success; final String err = error;
            runOnUiThread(() -> {
                progress.dismiss();
                if(ok) {
                    status.setText("CLIENT STATUS: EXTRACTED");
                    prefs.edit().putString("client_status","EXTRACTED").apply();
                    message("CLIENT SETUP COMPLETE",
                        "Arkib berjaya diekstrak ke folder RYL2Client.\n\nClient belum disalin ke ruang peribadi CMOD.");
                } else {
                    status.setText("CLIENT STATUS: EXTRACTION FAILED");
                    message("EXTRACTION ERROR",err == null ? "Ralat tidak diketahui." : err);
                }
            });
        }).start();
    }

    void extractZip(File zipFile,File dir) throws Exception {
        String base = dir.getCanonicalPath()+File.separator;
        try(ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry; byte[] buffer = new byte[8192];
            while((entry = zip.getNextEntry()) != null) {
                File out = new File(dir,entry.getName());
                if(!out.getCanonicalPath().startsWith(base))
                    throw new SecurityException("ZIP path tidak dibenarkan.");
                if(entry.isDirectory()) {
                    if(!out.mkdirs() && !out.isDirectory())
                        throw new IOException("Gagal membuat folder.");
                } else {
                    File parent = out.getParentFile();
                    if(parent != null) parent.mkdirs();
                    try(FileOutputStream stream = new FileOutputStream(out)) {
                        int count;
                        while((count = zip.read(buffer)) != -1)
                            stream.write(buffer,0,count);
                    }
                }
                zip.closeEntry();
            }
        }
    }

    // RYL2_INPUT_LAUNCHER_V1
    void openRyl2InputControls() {
        android.content.Intent intent =
            new android.content.Intent();

        intent.setClassName(
            getPackageName(),
            "com.winlator.cmod.MainActivity"
        );

        intent.putExtra("ryl2_open_input_controls", true);
        intent.putExtra("ryl2_input_profile_id", 0);

        try {
            startActivity(intent);
        } catch (Exception e) {
            new android.app.AlertDialog.Builder(this)
                .setTitle("RYL2 INPUT CONTROLS")
                .setMessage("Winlator CMOD tidak dapat dibuka.")
                .setPositiveButton("OK", null)
                .show();
        }
    }

}
