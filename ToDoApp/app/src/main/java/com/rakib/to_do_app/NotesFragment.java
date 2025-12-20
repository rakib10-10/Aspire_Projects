package com.rakib.to_do_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        dbHelper = new DatabaseHelper(requireContext());
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
        noteList = dbHelper.getAllNotes();
        notesAdapter = new NotesAdapter(noteList, this);
        notesRecyclerView.setAdapter(notesAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes(); // Refresh list when returning from Add/Edit screen
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(getContext(), AddNoteActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }
}