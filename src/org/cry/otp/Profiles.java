package org.cry.otp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Profiles extends Activity {
    private Cursor profilesCursor;
    private DBAdapter db;
    private ListView profilesList;
    private SharedPreferences preferences;
    private int count;
    private static final int MENU_ADD = 1;
    private static final int CONTEXT_DELETE = 0;
    private static final int CONTEXT_EDIT = 1;
    private static final int CONTEXT_SECRET = 2;
    private static int DIALOG_OTP_TYPES = 0;
    public static String EDITING_KEY = "Editing";

    @Override
    protected void onResume() {
        super.onResume();
        db = new DBAdapter(this);
        db.open();
        Cursor cursor = db.getAllProfiles();
        if (cursor.getCount() == 0) {
            cursor.close();
            db.close();
            showDialog(DIALOG_OTP_TYPES);
        }
        else {
            cursor.close();
            db.close();
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            setContentView(R.xml.profiles);
            createList();
        }
    }

    private DialogInterface.OnClickListener otpTypeClickListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            final Intent intent = new Intent(Profiles.this, ProfileSetup.class);
            if (which == Home.OTP_TYPE_MOTP) {
                intent.putExtra(DBAdapter.KEY_OTP_TYPE, Home.OTP_TYPE_MOTP);
                startActivity(intent);
                dismissDialog(DIALOG_OTP_TYPES);
            }
            else if (which == Home.OTP_TYPE_HOTP) {
                intent.putExtra(DBAdapter.KEY_OTP_TYPE, Home.OTP_TYPE_HOTP);
                startActivity(intent);
                dismissDialog(DIALOG_OTP_TYPES);
            }
            else {
                // TOTP
                intent.putExtra(DBAdapter.KEY_OTP_TYPE, Home.OTP_TYPE_TOTP);
                startActivity(intent);
                dismissDialog(DIALOG_OTP_TYPES);
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        if (id == DIALOG_OTP_TYPES) {
            final CharSequence[] items = getResources().getStringArray(
                    R.array.OTPTypes);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.otp_type_prompt);
            builder.setSingleChoiceItems(items, -1, otpTypeClickListener);
            dialog = builder.create();
        }
        else {
            dialog = super.onCreateDialog(id);
        }
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_ADD, 0, R.string.add_info).setIcon(
                R.drawable.ic_menu_add);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD:
                showDialog(DIALOG_OTP_TYPES);
                return true;
        }
        return false;
    }

    private void createList() {
        db.open();
        profilesCursor = db.getAllProfiles();
        count = profilesCursor.getCount();
        profilesCursor.close();
        db.close();
        profilesList = (ListView) findViewById(R.id.profilesList);
        profilesList.setAdapter(new ProfilesAdapter(getApplicationContext()));
        profilesList.setOnItemClickListener(profilesGridListener);
        profilesList
                .setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
                    public void onCreateContextMenu(ContextMenu menu, View v,
                            ContextMenuInfo menuInfo) {
                        menu.setHeaderTitle(R.string.manage_profile_title);
                        menu.add(0, CONTEXT_DELETE, 0, R.string.delete);
                        menu.add(0, CONTEXT_EDIT, 0, R.string.edit_profile);
                        menu.add(0, CONTEXT_SECRET, 0, R.string.get_secret);
                    }
                });
    }

    @Override
    public boolean onContextItemSelected(MenuItem aItem) {
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem
                .getMenuInfo();
        db.open();
        Cursor c = db.getAllProfiles();
        c.moveToFirst();
        for (int x = 0; x < menuInfo.position; x++) {
            c.moveToNext();
        }
        int rowId = c.getInt(c.getColumnIndexOrThrow(DBAdapter.KEY_ROWID));
        String profName = c.getString(c
                .getColumnIndexOrThrow(DBAdapter.KEY_PROF_NAME));
        String seed = c.getString(c.getColumnIndexOrThrow(DBAdapter.KEY_SEED));
        int otpType = c.getInt(c.getColumnIndexOrThrow(DBAdapter.KEY_OTP_TYPE));
        int digits = c.getInt(c.getColumnIndexOrThrow(DBAdapter.KEY_DIGITS));
        String zone = c.getString(c
                .getColumnIndexOrThrow(DBAdapter.KEY_TIME_ZONE));
        int timeInverval = c.getInt(c
                .getColumnIndexOrThrow(DBAdapter.KEY_TIME_INTERVAL));
        c.close();
        db.close();
        switch (aItem.getItemId()) {
            case CONTEXT_DELETE:
                preferences = PreferenceManager
                        .getDefaultSharedPreferences(this);
                db.open();
                db.deleteProfile(rowId);
                profilesCursor = db.getAllProfiles();
                count = profilesCursor.getCount();
                profilesCursor.close();
                db.close();

                if (count == 0) {
                    db.open();
                    db.reCreate();
                    db.close();
                    Intent intent = new Intent(Profiles.this, Profiles.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    profilesList.setAdapter(new ProfilesAdapter(this));
                }
                return true;
            case CONTEXT_EDIT:
                final Intent intent = new Intent(Profiles.this,
                        ProfileSetup.class);
                intent.putExtra(EDITING_KEY, true);
                intent.putExtra(DBAdapter.KEY_ROWID, rowId);
                intent.putExtra(DBAdapter.KEY_PROF_NAME, profName);
                intent.putExtra(DBAdapter.KEY_SEED, seed);
                intent.putExtra(DBAdapter.KEY_OTP_TYPE, otpType);
                intent.putExtra(DBAdapter.KEY_DIGITS, digits);
                intent.putExtra(DBAdapter.KEY_TIME_ZONE, zone);
                intent.putExtra(DBAdapter.KEY_TIME_INTERVAL, timeInverval);
                startActivity(intent);
                return true;
            case CONTEXT_SECRET:
                Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(profName);

                if (otpType == Home.OTP_TYPE_MOTP) {
                    String secret = new MD5(seed).asHex().substring(0, 16);
                    builder.setMessage(getString(R.string.secret) + ":  "
                            + secret);
                }
                else {
                    String secret = seed;
                    builder.setMessage(getString(R.string.secret) + ":  "
                            + secret);
                }

                builder.setPositiveButton(R.string.ok, null);
                builder.show();
                return true;
        }
        return false;
    }

    private OnItemClickListener profilesGridListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position,
                long id) {
            db.open();
            profilesCursor = db.getAllProfiles();
            profilesCursor.moveToPosition(position);
            SharedPreferences.Editor ed = preferences.edit();
            int column = profilesCursor
                    .getColumnIndexOrThrow(DBAdapter.KEY_PROF_NAME);
            ed.putString(DBAdapter.KEY_PROF_NAME,
                    profilesCursor.getString(column));
            column = profilesCursor.getColumnIndexOrThrow(DBAdapter.KEY_SEED);
            ed.putString(DBAdapter.KEY_SEED, profilesCursor.getString(column));
            column = profilesCursor
                    .getColumnIndexOrThrow(DBAdapter.KEY_OTP_TYPE);
            ed.putInt(DBAdapter.KEY_OTP_TYPE, profilesCursor.getInt(column));
            column = profilesCursor.getColumnIndexOrThrow(DBAdapter.KEY_COUNT);
            ed.putInt(DBAdapter.KEY_COUNT, profilesCursor.getInt(column));
            column = profilesCursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWID);
            ed.putInt(DBAdapter.KEY_ROWID, profilesCursor.getInt(column));
            column = profilesCursor.getColumnIndexOrThrow(DBAdapter.KEY_DIGITS);
            ed.putInt(DBAdapter.KEY_DIGITS, profilesCursor.getInt(column));
            column = profilesCursor
                    .getColumnIndexOrThrow(DBAdapter.KEY_TIME_ZONE);
            ed.putString(DBAdapter.KEY_TIME_ZONE,
                    profilesCursor.getString(column));
            column = profilesCursor
                    .getColumnIndexOrThrow(DBAdapter.KEY_TIME_INTERVAL);
            ed.putInt(DBAdapter.KEY_TIME_INTERVAL,
                    profilesCursor.getInt(column));
            ed.commit();
            profilesCursor.close();
            db.close();
            Intent intent = new Intent(Profiles.this, Home.class);
            startActivity(intent);
        }
    };

    public class ProfilesAdapter extends BaseAdapter {
        private Context vContext;

        public ProfilesAdapter(Context c) {
            vContext = c;
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            db.open();
            profilesCursor = db.getAllProfiles();
            TextView tv = new TextView(vContext.getApplicationContext());
            String id = null;
            if (convertView == null) {
                int col_index = profilesCursor
                        .getColumnIndexOrThrow(DBAdapter.KEY_PROF_NAME);
                profilesCursor.moveToPosition(position);
                id = profilesCursor.getString(col_index);
                profilesCursor.close();
                db.close();
                tv.setTextSize(24);
                tv.setText(id);
            }
            else {
                profilesCursor.close();
                db.close();
                tv = (TextView) convertView;
            }
            return tv;
        }
    }
}
