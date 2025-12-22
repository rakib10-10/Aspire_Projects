package com.rakib.to_do_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class NotesFragment extends Fragment implements NotesAdapter.OnNoteListener {

    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private DatabaseHelper dbHelper;
    private List<Note> noteList;
    private SessionManager sessionManager; // 1. Add SessionManager

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext()); // 2. Initialize Session

        notesRecyclerView = view.findViewById(R.id.notesRecyclerView);
        FloatingActionButton fabAddNote = view.findViewById(R.id.fabAddNote);

        // Staggered Layout for "Notes" feel (2 columns)
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        fabAddNote.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddNoteActivity.class));
        });

        loadNotes();
        return view;
    }

    private void loadNotes() {
        // 3. Get User Email
        String userEmail = sessionManager.getUserEmail();

        // 4. Load notes specific to this user
        noteList = dbHelper.getAllNotes(userEmail);

        // 5. Pass 'requireContext()' to the adapter (Fixed constructor)
        notesAdapter = new NotesAdapter(noteList, requireContext(), this);
        notesRecyclerView.setAdapter(notesAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes(); // Refresh list when returning from Add/Edit screen
    }

    // --- IMPLEMENT DELETE LOGIC ---
    @Override
    public void onDeleteClick(Note note, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // 1. Delete from Database
                    dbHelper.deleteNote(note.getId());

                    // 2. Remove from List and Update UI
                    noteList.remove(position);
                    notesAdapter.notifyItemRemoved(position);
                    notesAdapter.notifyItemRangeChanged(position, noteList.size());

                    Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}