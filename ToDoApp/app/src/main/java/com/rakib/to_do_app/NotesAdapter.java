package com.rakib.to_do_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private Context context;
    private OnNoteListener onNoteListener; // Interface for clicks

    // Constructor accepts the listener
    public NotesAdapter(List<Note> notes, Context context, OnNoteListener onNoteListener) {
        this.notes = notes;
        this.context = context;
        this.onNoteListener = onNoteListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.textTitle.setText(note.getTitle());
        holder.textDateTime.setText(note.getDateTime());

        // Parse HTML content for preview
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.textContent.setText(Html.fromHtml(note.getContent(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.textContent.setText(Html.fromHtml(note.getContent()));
        }

        // --- Handle Card Click (Open Note) ---
        holder.layoutNote.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddNoteActivity.class);
            intent.putExtra("note_id", note.getId());
            context.startActivity(intent);
        });

        // --- Handle Delete Click ---
        holder.imageDelete.setOnClickListener(v -> {
            if (onNoteListener != null) {
                onNoteListener.onDeleteClick(note, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    // ViewHolder Class
    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textContent, textDateTime;
        ImageView imageDelete;
        View layoutNote;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textNoteTitle);
            textContent = itemView.findViewById(R.id.textNoteContent);
            textDateTime = itemView.findViewById(R.id.textNoteDate);
            imageDelete = itemView.findViewById(R.id.imageDelete); // The new delete button
            layoutNote = itemView.findViewById(R.id.noteCard);
        }
    }

    // Interface for callback
    public interface OnNoteListener {
        void onDeleteClick(Note note, int position);
    }
}