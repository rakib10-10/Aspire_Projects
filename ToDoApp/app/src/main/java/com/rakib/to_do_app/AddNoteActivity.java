package com.rakib.to_do_app;

import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.graphics.Typeface;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteContent;
    private TextView textDateTime;
    private Note alreadyAvailableNote;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager; // 1. Add SessionManager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this); // 2. Initialize SessionManager

        inputNoteTitle = findViewById(R.id.inputNoteTitle);
        inputNoteContent = findViewById(R.id.inputNoteContent);
        textDateTime = findViewById(R.id.textDateTime);

        // Formatting Buttons
        findViewById(R.id.formatBold).setOnClickListener(v -> applyFormat("bold"));
        findViewById(R.id.formatItalic).setOnClickListener(v -> applyFormat("italic"));
        findViewById(R.id.formatUnderline).setOnClickListener(v -> applyFormat("underline"));

        findViewById(R.id.imageBack).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.imageSave).setOnClickListener(v -> saveNote());

        textDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        if (getIntent().hasExtra("note_id")) {
            long noteId = getIntent().getLongExtra("note_id", -1);
            loadNote(noteId);
        }
    }

    private void loadNote(long id) {
        // 3. Get Current User Email
        String userEmail = sessionManager.getUserEmail();

        // 4. Pass email to getAllNotes
        for(Note n : dbHelper.getAllNotes(userEmail)) {
            if(n.getId() == id) {
                alreadyAvailableNote = n;
                inputNoteTitle.setText(n.getTitle());
                // Load HTML content
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    inputNoteContent.setText(Html.fromHtml(n.getContent(), Html.FROM_HTML_MODE_COMPACT));
                } else {
                    inputNoteContent.setText(Html.fromHtml(n.getContent()));
                }
                textDateTime.setText(n.getDateTime());
                break;
            }
        }
    }

    private void applyFormat(String type) {
        int start = inputNoteContent.getSelectionStart();
        int end = inputNoteContent.getSelectionEnd();

        if (start < end) {
            Spannable spannable = inputNoteContent.getText();
            if (type.equals("bold")) {
                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type.equals("italic")) {
                spannable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (type.equals("underline")) {
                spannable.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        } else {
            Toast.makeText(this, "Select text to format", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveNote() {
        String title = inputNoteTitle.getText().toString().trim();

        // Convert Spanned text (with formatting) to HTML string for storage
        String content;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            content = Html.toHtml(inputNoteContent.getText(), Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
        } else {
            content = Html.toHtml(inputNoteContent.getText());
        }
        String dateTime = textDateTime.getText().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setDateTime(dateTime);
        note.setColor("#333333");

        // 5. Get User Email
        String userEmail = sessionManager.getUserEmail();

        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
            dbHelper.updateNote(note); // Update logic remains the same (ID is unique)
            Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
        } else {
            // 6. Pass Email to addNote
            dbHelper.addNote(note, userEmail);
            Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}