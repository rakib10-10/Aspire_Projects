package com.rakib.to_do_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // Views
    private MaterialButtonToggleGroup toggleGroupStatus;
    private RecyclerView recyclerTasks;
    private TextView txtEmptyState;
    private LinearLayout layoutEmptyState; // New container for empty state
    private TextView tvUserNameGreeting;
    private SearchView searchTaskCategory;
    private ImageButton btnSort;

    // Data
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private TaskManager taskManager;
    private DatabaseHelper dbHelper;

    // State
    private String currentTab = "all";
    private String currentSearchQuery = "";
    private String currentSortBy = "date";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        taskManager = TaskManager.getInstance(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        initializeViews(view);
        loadUserName();
        setupTaskAdapter();
        setupTabListener(); // New consolidated listener
        setupSearchAndSort();

        loadTasks();

        return view;
    }

    private void initializeViews(View view) {
        toggleGroupStatus = view.findViewById(R.id.toggleGroupStatus);
        recyclerTasks = view.findViewById(R.id.recyclerTasks);
        txtEmptyState = view.findViewById(R.id.txtEmptyState);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState); // Make sure to add ID in XML

        searchTaskCategory = view.findViewById(R.id.searchTaskCategory);
        btnSort = view.findViewById(R.id.btnSort);
        tvUserNameGreeting = view.findViewById(R.id.tvUserNameGreeting);
    }

    private void loadUserName() {
        // Use session manager preferably, or fallback to DB
        SessionManager session = new SessionManager(requireContext());
        String name = session.getUserName();
        if (name.equals("User")) { // Fallback if session empty
            UserProfile profile = dbHelper.getUserProfile();
            if (profile != null) name = profile.getName();
        }

        if (name != null && !name.isEmpty()) {
            String firstName = name.split(" ")[0];
            tvUserNameGreeting.setText(firstName + "!");
        }
    }

    private void setupTaskAdapter() {
        // ... (Keep your existing adapter setup code exactly as is) ...
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskEdit(int position) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) openEditTaskDialog(task);
            }

            @Override
            public void onTaskDelete(int position) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    ReminderManager reminderManager = new ReminderManager(requireContext());
                    reminderManager.cancelReminder(task);
                    taskManager.removeTask(task);
                    allTasks.remove(task);
                    filterAndSortTasks();
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTaskStatusChange(int position, String newStatus) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    task.setStatus(newStatus);
                    taskManager.updateTask(task);
                    filterAndSortTasks();
                    String statusText = newStatus.equals("running") ? "In Progress" : "Completed";
                    Toast.makeText(getContext(), "Task marked as " + statusText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(taskAdapter);
    }

    // --- REPLACED: setupClickListeners with setupTabListener ---
    private void setupTabListener() {
        toggleGroupStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnMyTasks) {
                    currentTab = "all";
                } else if (checkedId == R.id.btnInProgress) {
                    currentTab = "running";
                } else if (checkedId == R.id.btnCompleted) {
                    currentTab = "completed";
                }
                filterAndSortTasks();
            }
        });
    }

    private void setupSearchAndSort() {
        searchTaskCategory.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                filterAndSortTasks();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterAndSortTasks();
                return true;
            }
        });

        btnSort.setOnClickListener(this::showSortMenu);
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add(0, 1, 0, "Sort by Date");
        popup.getMenu().add(0, 2, 0, "Sort by Priority");
        // popup.getMenu().add(0, 3, 0, "Sort by Category"); // Optional

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            String newSort = currentSortBy;

            if (itemId == 1) newSort = "date";
            else if (itemId == 2) newSort = "priority";

            if (!currentSortBy.equals(newSort)) {
                currentSortBy = newSort;
                filterAndSortTasks();
            }
            return true;
        });
        popup.show();
    }

    private void loadTasks() {
        allTasks = taskManager.getAllTasks();
        filterAndSortTasks();
    }

    private void filterAndSortTasks() {
        List<Task> filtered = new ArrayList<>();
        String status = currentTab;
        String query = currentSearchQuery.toLowerCase(Locale.getDefault());

        // 1. Filter by Status
        for (Task task : allTasks) {
            boolean statusMatches = status.equals("all") ||
                    (task.getStatus() != null && task.getStatus().equals(status));
            if (statusMatches) {
                filtered.add(task);
            }
        }

        // 2. Filter by Search (Category)
        if (!query.isEmpty()) {
            List<Task> searchFiltered = new ArrayList<>();
            for (Task task : filtered) {
                if (task.getCategory() != null &&
                        task.getCategory().toLowerCase(Locale.getDefault()).contains(query)) {
                    searchFiltered.add(task);
                }
            }
            filtered = searchFiltered;
        }

        // 3. Sort & Update Adapter
        if (taskAdapter != null) {
            taskAdapter.updateList(filtered);
            taskAdapter.sortList(currentSortBy);
        }

        // 4. Update Empty State Visibility
        updateEmptyState(filtered.isEmpty(), status, query);
    }

    private void updateEmptyState(boolean isEmpty, String status, String query) {
        if (isEmpty) {
            recyclerTasks.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE); // Show layout container

            if (!query.isEmpty()) {
                txtEmptyState.setText("No categories match \"" + query + "\"");
            } else {
                switch (status) {
                    case "all": txtEmptyState.setText("No tasks yet"); break;
                    case "running": txtEmptyState.setText("No tasks in progress"); break;
                    case "completed": txtEmptyState.setText("No completed tasks"); break;
                }
            }
        } else {
            recyclerTasks.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void openEditTaskDialog(Task task) {
        AddTaskDialog dialog = new AddTaskDialog();
        Bundle args = new Bundle();
        // ... (Keep existing argument passing logic) ...
        args.putLong("task_id", task.getId());
        args.putString("title", task.getTitle());
        args.putString("description", task.getDescription());
        args.putString("date", task.getDate());
        args.putString("startTime", task.getStartTime());
        args.putString("endTime", task.getEndTime());
        args.putString("category", task.getCategory());
        args.putString("status", task.getStatus());
        args.putString("priority", task.getPriority());
        args.putString("color_tag", task.getColorTag());
        dialog.setArguments(args);

        dialog.setOnTaskCreatedListener((t, d, dt, st, et, c, s, p) -> loadTasks());
        dialog.show(getParentFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
        loadUserName();
    }
}