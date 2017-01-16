package com.example.android.simplenote;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditorActivity extends AppCompatActivity {

    private String mAction;
    private EditText mEditor;
    private String mNoteFilter;
    private String mOldNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEditor = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE);

        if(uri == null) {
            mAction = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));
        } else {
            mAction = Intent.ACTION_EDIT;
            mNoteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor = getContentResolver().query(uri, DBOpenHelper.ALL_COLUMNS, mNoteFilter, null, null);
            cursor.moveToFirst();
            mOldNote = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            mEditor.setText(mOldNote);
            mEditor.requestFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(mAction.equals(Intent.ACTION_EDIT)) {
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
        }

        return true;
    }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                mNoteFilter, null);
        Toast.makeText(this, R.string.note_deleted,
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing() {
        String newText = mEditor.getText().toString().trim();

        switch(mAction) {
            case Intent.ACTION_INSERT:
                if(newText.length() == 0) {
                    setResult(RESULT_CANCELED);
                } else {
                    insertNote(newText);
                }
                break;
            case Intent.ACTION_EDIT:
                if(newText.length() == 0) {
                    deleteNote();
                } else if(newText.equals(mOldNote)) {
                    setResult(RESULT_CANCELED);
                } else {
                    updateNote(newText);
                }
        }
        finish();
    }

    private void updateNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, mNoteFilter, null);
        Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }
}
