package com.rakib.to_do_app;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private OnNoteListener listener;

    public interface OnNoteListener {
        void onNoteClick(Note note);
    }

    public NotesAdapter(List<Note> notes, OnNoteListener listener) {
        this.notes = notes;
        this.listener = listener;
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

        // Render HTML for preview
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            holder.textContent.setText(Html.fromHtml(note.getContent(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            holder.textContent.setText(Html.fromHtml(note.getContent()));
        }

        holder.textDateTime.setText(note.getDateTime());

        holder.noteCard.setOnClickListener(v -> listener.onNoteClick(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textContent, textDateTime;
        CardView noteCard;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textNoteTitle);
            textContent = itemView.findViewById(R.id.textNoteContent);
            textDateTime = itemView.findViewById(R.id.textNoteDate);
            noteCard = itemView.findViewById(R.id.noteCard);
        }
    }
}