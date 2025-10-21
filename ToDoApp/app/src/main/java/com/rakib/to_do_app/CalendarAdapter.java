package com.rakib.to_do_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<CalendarDay> calendarDays;
    private OnDateSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnDateSelectedListener {
        void onDateSelected(CalendarDay day);
    }

    public CalendarAdapter(List<CalendarDay> calendarDays, OnDateSelectedListener listener) {
        this.calendarDays = calendarDays;
        this.listener = listener;
    }

    public void updateCalendarDays(List<CalendarDay> calendarDays) {
        this.calendarDays = calendarDays;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        if (position != -1) notifyItemChanged(position);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = calendarDays.get(position);
        if (day == null) {
            holder.bindEmpty();
        } else {
            holder.bind(day, position == selectedPosition);
        }
    }

    @Override
    public int getItemCount() {
        return calendarDays != null ? calendarDays.size() : 0;
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView txtDayNumber;
        View indicator1, indicator2, indicator3;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDayNumber = itemView.findViewById(R.id.txtDayNumber);
            indicator1 = itemView.findViewById(R.id.indicator1);
            indicator2 = itemView.findViewById(R.id.indicator2);
            indicator3 = itemView.findViewById(R.id.indicator3);
        }

        void bind(CalendarDay day, boolean isSelected) {
            txtDayNumber.setText(String.valueOf(day.getDay()));
            txtDayNumber.setVisibility(View.VISIBLE);

            // Set background based on selection state
            if (isSelected) {
                txtDayNumber.setBackgroundResource(R.drawable.bg_calendar_day_selected);
                txtDayNumber.setTextColor(itemView.getContext().getColor(android.R.color.white));
            } else if (day.isToday()) {
                txtDayNumber.setBackgroundResource(R.drawable.bg_calendar_day_today);
                txtDayNumber.setTextColor(itemView.getContext().getColor(android.R.color.black));
            } else {
                txtDayNumber.setBackgroundResource(android.R.color.transparent);
                txtDayNumber.setTextColor(itemView.getContext().getColor(android.R.color.black));
            }

            // Show/hide task indicators
            int taskCount = day.getTaskCount();
            indicator1.setVisibility(taskCount > 0 ? View.VISIBLE : View.GONE);
            indicator2.setVisibility(taskCount > 1 ? View.VISIBLE : View.GONE);
            indicator3.setVisibility(taskCount > 2 ? View.VISIBLE : View.GONE);

            // Set opacity for days from other months
            txtDayNumber.setAlpha(day.isCurrentMonth() ? 1.0f : 0.3f);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    setSelectedPosition(getAdapterPosition());
                    listener.onDateSelected(day);
                }
            });
        }

        void bindEmpty() {
            txtDayNumber.setVisibility(View.INVISIBLE);
            indicator1.setVisibility(View.GONE);
            indicator2.setVisibility(View.GONE);
            indicator3.setVisibility(View.GONE);
            itemView.setOnClickListener(null);
        }
    }
}