package com.rakib.to_do_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
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
    private LinearLayout layoutEmptyState;
    private TextView tvUserNameGreeting;
    private SearchView searchTaskCategory;
    private ImageButton btnSort;

    // Data
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private TaskManager taskManager;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager; // Added SessionManager

    // State
    private String currentTab = "all";
    private String currentSearchQuery = "";
    private String currentSortBy = "date";

    // --- 1. Broadcast Receiver for Background Updates ---
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.rakib.to_do_app.UPDATE_UI".equals(intent.getAction())) {
                loadTasks(); // Auto-refresh when ReminderReceiver changes status
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        taskManager = TaskManager.getInstance(requireContext());
        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext()); // Initialize

        initializeViews(view);
        loadUserName();
        setupTaskAdapter();
        setupTabListener();
        setupSearchAndSort();

        loadTasks();

        return view;
    }

    // --- 2. Register/Unregister Receiver ---
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("com.rakib.to_do_app.UPDATE_UI");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireActivity().registerReceiver(updateReceiver, filter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            requireActivity().unregisterReceiver(updateReceiver);
        } catch (Exception e) {
            // Receiver might not be registered
        }
    }

    private void initializeViews(View view) {
        toggleGroupStatus = view.findViewById(R.id.toggleGroupStatus);
        recyclerTasks = view.findViewById(R.id.recyclerTasks);
        txtEmptyState = view.findViewById(R.id.txtEmptyState);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        searchTaskCategory = view.findViewById(R.id.searchTaskCategory);
        btnSort = view.findViewById(R.id.btnSort);
        tvUserNameGreeting = view.findViewById(R.id.tvUserNameGreeting);
    }

    private void loadUserName() {
        // 3. Fix: Use correct method to get user details
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (name.equals("User") || name.isEmpty()) {
            // Use getUserDetails(email) instead of generic getUserProfile()
            UserProfile profile = dbHelper.getUserDetails(email);
            if (profile != null && profile.getName() != null) {
                name = profile.getName();
            }
        }

        if (name != null && !name.isEmpty()) {
            String firstName = name.split(" ")[0];
            tvUserNameGreeting.setText("Hi, " + firstName + "!");
        }
    }

    private void setupTaskAdapter() {
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
                    // No need to manually remove from allTasks, loadTasks() will refresh everything
                    loadTasks();
                    Toast.makeText(getContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTaskStatusChange(int position, String newStatus) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    task.setStatus(newStatus);
                    taskManager.updateTask(task);
                    loadTasks(); // Refresh to update list order/filtering
                    String statusText = newStatus.equals("running") ? "In Progress" : "Completed";
                    Toast.makeText(getContext(), "Task marked as " + statusText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(taskAdapter);
    }

    private void setupTabListener() {
        toggleGroupStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnMyTasks) {
                    currentTab = "all";
                } else if (checkedId == R.id.btnInProgress) {
                    currentTab = "running";
                } else if (checkedId == R.id.btnCompleted) {
                    currentTab = "completed";
                } else if (checkedId == R.id.btnMissed) {
                    // Handle the new button
                    currentTab = "unfinished";
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
        // 4. Fix: FORCE REFRESH from Database
        // This ensures changes from background services (ReminderReceiver) are seen
        taskManager.refreshTasks();

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
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);

            if (!query.isEmpty()) {
                txtEmptyState.setText("No categories match \"" + query + "\"");
            } else {
                switch (status) {
                    case "all": txtEmptyState.setText("No tasks yet"); break;
                    case "running": txtEmptyState.setText("No tasks in progress"); break;
                    case "completed": txtEmptyState.setText("No completed tasks"); break;
                    case "unfinished": txtEmptyState.setText("Great job! No missed tasks."); break; // New Message
                }
            }
        } else {
            recyclerTasks.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void openEditTaskDialog(Task task) {
        AddTaskDialog dialog = new AddTaskDialog();
        Bundle args = new Bundle();
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