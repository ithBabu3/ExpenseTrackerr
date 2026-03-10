package com.expensetracker.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.expensetracker.R;
import com.expensetracker.adapters.ExpenseAdapter;
import com.expensetracker.database.DataManager;
import com.expensetracker.models.Expense;
import com.expensetracker.utils.Categories;
import com.google.android.material.textfield.TextInputEditText;

import java.util.*;

public class ExpensesFragment extends Fragment {

    private DataManager dataManager;
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private TextInputEditText etSearch;
    private Spinner spinnerFilter;
    private TextView tvEmpty;
    private List<Expense> allExpenses = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataManager = DataManager.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.rv_expenses);
        etSearch = view.findViewById(R.id.et_search);
        spinnerFilter = view.findViewById(R.id.spinner_filter);
        tvEmpty = view.findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Category filter
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        categories.addAll(Categories.ALL);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(catAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { applyFilter(); }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadExpenses();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadExpenses();
    }

    private void loadExpenses() {
        allExpenses = new ArrayList<>(dataManager.getAllExpenses());
        allExpenses.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        applyFilter();
    }

    private void applyFilter() {
        String search = etSearch.getText() != null ?
            etSearch.getText().toString().toLowerCase() : "";
        String category = (String) spinnerFilter.getSelectedItem();
        boolean allCats = "All Categories".equals(category);

        List<Expense> filtered = new ArrayList<>();
        for (Expense e : allExpenses) {
            boolean matchCat = allCats || e.getCategory().equals(category);
            boolean matchSearch = search.isEmpty()
                || e.getTitle().toLowerCase().contains(search)
                || e.getCategory().toLowerCase().contains(search)
                || (e.getNote() != null && e.getNote().toLowerCase().contains(search));
            if (matchCat && matchSearch) filtered.add(e);
        }

        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        if (adapter == null) {
            adapter = new ExpenseAdapter(getContext(), filtered, dataManager, this::loadExpenses);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(filtered);
        }
    }
}
